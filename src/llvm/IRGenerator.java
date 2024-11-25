package llvm;

import frontend.parser.Parser;
import frontend.parser.syntaxUnit.*;
import frontend.symbol.SymbolTable;
import frontend.visitor.Visitor;
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

    static {
        parser = Parser.getInstance();
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
        if (parser == null)
            parser = Parser.getInstance();
        ast = parser.getAst();
        if (irModule == null)
            irModule = IRModule.getInstance();

        if (ast == null || irModule == null) {
            return; // 出错
        }

        visitCompUnit(ast);

        // 全局变量：GlobalVariable
        for (Decl decl: ast.getDeclList()) {
            visitDecl(decl);
            // 过程中构建的GlobalVar直接加入IRGenerator的list中（最后再统一给IRModule的成员赋值）
        }
        // 结束分析后，加入全局变量（关于printf的格式串可以考虑遍历完下面的所有函数的BasicBlock之后在最后面GlobalVars保存）

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
//        varDecl.generateIR();
    }

    // 注意一个decl中可能有好多def --> 在def中进行value生成
    public void visitConstDecl(ConstDecl constDecl) {
        // 插入符号表
        constDecl.insertSymbol(cur_ir_symTable);
        // 构建ConstValue--在上面的insert过程添加

    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
    }

    private void visitFuncDef(FuncDef funcDef) {
    }

    public static void setLlvm_ir_gen(Boolean llvm_ir_gen) {
        IRGenerator.llvm_ir_gen = llvm_ir_gen;
    }
}
