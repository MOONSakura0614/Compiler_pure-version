package llvm;

import errors.ErrorHandler;
import frontend.parser.Parser;
import frontend.parser.syntaxUnit.*;
import frontend.symbol.FuncSymbol;
import frontend.symbol.Symbol;
import frontend.symbol.SymbolTable;
import frontend.symbol.SymbolType;
import frontend.visitor.Visitor;
import llvm.type.IRFunctionType;
import llvm.type.IRIntType;
import llvm.value.IRFunction;
import llvm.value.IRGlobalVar;
import llvm.value.IRValue;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: LLVM IR中间代码生成器
 * @date 2024/11/13 17:07
 */
public class IRGenerator {
    private static IRGenerator irGenerator; // 单例模式
    private CompUnit ast; // 语法树的根节点 compUnit
    private static Parser parser;
    private static IRBuilder builder;
//    private SymbolTable symbolTable; // SSA
    public static SymbolTable cur_ir_symTable;
    // 记录的应该是当前的符号表——curSymTable
    private IRModule irModule;
    private IRValue irValue;
    public static ArrayList<IRGlobalVar> globalVars;
    public static ArrayList<IRFunction> functions;
//    public static int cur_ir_symTable_scope;
//    public static SymbolTable constSymTable;
//    每个符号表中的不同符号代表有标记常量和变量==>不用每个作用域两个表
    public static Boolean llvm_ir_gen = Boolean.FALSE;
    public static Boolean globalVar_gen = Boolean.FALSE;

    static {
        parser = Parser.getInstance();
        builder = IRBuilder.getInstance();
        globalVars = new ArrayList<>();
        functions = new ArrayList<>();
//        constSymTable = new SymbolTable();
        // 常量表没有fatherTable，scope默认为0，但是不太影响(?
        // 常量表是都能用？？
//        cur_ir_symTable_scope = 0; // 最外层是全局
    }

    private IRGenerator() {
//        symbolTable = new SymbolTable();
//        globalVars = new ArrayList<>();
//        functions = new ArrayList<>();
    }

    public static IRGenerator getInstance() {
        if (irGenerator == null) {
            irGenerator = new IRGenerator();
        }

        return irGenerator;
    }

    public IRModule getIrModule() {
        if (irModule == null)
            irModule = IRModule.getInstance();

        return irModule;
    }

    // 维护IR阶段的符号表
    public SymbolTable newIRSymTable() {
        SymbolTable fatherTable = cur_ir_symTable;
        SymbolTable newTable = new SymbolTable();
        if (fatherTable != null) { // 不管进不进if，是null就是null（父表
            // 在CompUnit节点（根节点）被遍历之前，curTable为null
            newTable.setFatherTable(fatherTable);
        }
        // 和语义分析不同，不用按照scope规则输出;同理，也无需维护TableList
        cur_ir_symTable = newTable;
        return newTable;
    }

    public void exitCurScope() {
        if (cur_ir_symTable == null)
            return;
        cur_ir_symTable = cur_ir_symTable.getFatherTable();
    }

    public void generateIR() {
        llvm_ir_gen = Boolean.TRUE;

        if (parser == null)
            parser = Parser.getInstance();
        ast = parser.getAst();
        if (irModule == null)
            irModule = IRModule.getInstance();

        if (ast == null || irModule == null) {
            return; // 出错
        }

        visitCompUnit(ast);

        globalVar_gen = Boolean.TRUE; // 用于build value（判断什么时候创建的value需要加入全局变量

        // 全局变量：GlobalVariable
        for (Decl decl: ast.getDeclList()) {
            visitDecl(decl);
            // 过程中构建的GlobalVar直接加入IRGenerator的list中（最后再统一给IRModule的成员赋值）
        }
        // 结束分析后，加入全局变量（关于printf的格式串可以考虑遍历完下面的所有函数的BasicBlock之后在最后面GlobalVars保存）

        globalVar_gen = Boolean.FALSE; // 注意下面之后，除了str，其他不在加入GlobalVars

        // lib外部静态链接的IO函数需要在哪输出:提前在前面的IOUtils里的函数就写入(具体见IrModule的printIR函数)
        // GlobalValue：自定义函数
        for (FuncDef funcDef: ast.getFuncDefList()) {
            visitFuncDef(funcDef);
        }
        
        if (ast.getMainFuncDef() != null) {
            visitMainFuncDef(ast.getMainFuncDef());
        }

        // 设置IRModule的成员变量
        irModule.setGlobalVarList(globalVars);
        irModule.setFunctionList(functions);

        llvm_ir_gen = Boolean.FALSE;
    }

    // 下面是遍历语法树
    private void visitCompUnit(CompUnit compUnit) {
        // 构造初始的符号表
        SymbolTable symbolTable = newIRSymTable();
        // 插入库函数符号
        symbolTable.insertSymbol(IOLib.GETCHAR8.getIoFuncSym());
        symbolTable.insertSymbol(IOLib.GETINT32.getIoFuncSym());
        symbolTable.insertSymbol(IOLib.PUT_STR.getIoFuncSym());
        symbolTable.insertSymbol(IOLib.PUT_INT_32.getIoFuncSym());
        symbolTable.insertSymbol(IOLib.PUT_CH.getIoFuncSym());
        // 库函数无需插入IRModule中的FuncList，只要查询表能查到就行
        // IR输出的时候，单独在初始化ir.txt的时候就在最前面加完了库函数的声明
        // 下面的全局变量和函数就正常visit？
    }

    // 注意区分全局和局部的Decl
    public void visitDecl(Decl decl) {
        if (decl.getIsConst()) {
            if (decl.getConstDecl() == null)
                return;
            visitConstDecl(decl.getConstDecl());
        } else {
            if (decl.getVarDecl() == null)
                return;
            visitVarDecl(decl.getVarDecl());
        }
    }

    private void visitVarDecl(VarDecl varDecl) {
        varDecl.insertSymbol(cur_ir_symTable);
    }

    // 注意一个decl中可能有好多def --> 在def中进行value生成
    public void visitConstDecl(ConstDecl constDecl) {
        // 插入符号表
        constDecl.insertSymbol(cur_ir_symTable);
        // 构建ConstValue--在上面的insert过程添加
    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
        // 构建一个function（但是名字是main）
        IRFunction mainFunc = builder.buildIRMainFunc();
//        cur_ir_symTable.insertSymbol(new FuncSymbol()); // main标识不加符号表了
        if (mainFuncDef.getBlock() != null)
            visitBlock(mainFuncDef.getBlock());
    }

    private void visitBlock(Block block) {
        newIRSymTable();
        ArrayList<BlockItem> blockItem_list = block.getBlockItem_list();
        for (BlockItem blockItem: blockItem_list) {
            visitBlockItem(blockItem);
        }
        exitCurScope();
    }

    private void visitBlockItem(BlockItem blockItem) {
        if (blockItem.getIsDecl()) {
            Decl decl = blockItem.getDecl();
            if (decl != null)
                decl.insertSymbol(cur_ir_symTable);
        } else {
            Stmt stmt = blockItem.getStmt();
            if (stmt != null)
                visitStmt(stmt);
        }
    }

    private void visitStmt(Stmt stmt) {
        Integer chosen_plan = stmt.getChosen_plan();
        // 遇到block是新的作用域，其他需要检查符号调用
        switch (chosen_plan) {
            case 1 -> {
                // LVal '=' Exp ';' 赋值指令
            }
            case 2 -> {
                // [Exp] ';' 纯运算，不知道可不可以完全舍弃不翻译
            }
            case 3 -> {
                // Block
                if (stmt.getBlock() != null) {
                    visitBlock(stmt.getBlock());
                }
            }
            case 4 -> {
                // todo:跳转 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            }
            case 5 -> {
                // todo:循环 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            }
            case 6 -> {
                // 'break' ';' | 'continue' ';'
            }
            case 7 -> {
                // 'return' [Exp] ';' ret指令
            }
            case 8, 9 -> {
                // LVal '=' 'getint''('')'';'
                // LVal '=' 'getchar''('')'';'
                // 输入函数调用 和 赋值 指令
            }
            case 10 -> {
                // printf 输出函数调用
            }
        }
    }

    private void visitFuncDef(FuncDef funcDef) {
        // 先把函数名加入外层符号表
        funcDef.insertSymbol(cur_ir_symTable);
        newIRSymTable();
    }

    public static void setLlvm_ir_gen(Boolean llvm_ir_gen) {
        IRGenerator.llvm_ir_gen = llvm_ir_gen;
    }
}
