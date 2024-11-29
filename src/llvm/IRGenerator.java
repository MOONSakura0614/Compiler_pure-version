package llvm;

import frontend.lexer.LexType;
import frontend.parser.Parser;
import frontend.parser.syntaxUnit.*;
import frontend.symbol.Symbol;
import frontend.symbol.SymbolTable;
import llvm.type.*;
import llvm.value.*;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.memory.AllocaInst;
import llvm.value.instruction.memory.LoadInst;
import llvm.value.instruction.memory.StoreInst;
import llvm.value.instruction.terminator.RetInst;

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
    private LexType varDefType = LexType.INTTK;
    public static ArrayList<IRGlobalVar> globalVars;
    public static ArrayList<IRFunction> functions;
    public static IRFunction cur_func; // 目前处于的函数
    public static IRBasicBlock cur_basicBlock; // 目前处于的基本块
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
        Symbol symbol = IOLib.GETCHAR8.getIoFuncSym();
        // 插入库函数符号
        symbolTable.insertSymbol(symbol);
        symbol.setRetType(IRIntType.intType);

        symbol = IOLib.GETINT32.getIoFuncSym();
        symbolTable.insertSymbol(IOLib.GETINT32.getIoFuncSym());
        symbol.setRetType(IRIntType.intType);

        symbol = IOLib.PUT_STR.getIoFuncSym();
        symbolTable.insertSymbol(IOLib.PUT_STR.getIoFuncSym());
        symbol.setRetType(IRVoidType.voidType);

        symbol = IOLib.PUT_INT_32.getIoFuncSym();
        symbolTable.insertSymbol(IOLib.PUT_INT_32.getIoFuncSym());
        symbol.setRetType(IRVoidType.voidType);

        symbol = IOLib.PUT_CH.getIoFuncSym();
        symbolTable.insertSymbol(IOLib.PUT_CH.getIoFuncSym());
        symbol.setRetType(IRVoidType.voidType);
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
        if (globalVar_gen) {
            varDecl.insertSymbol(cur_ir_symTable);
            return;
        }
        // 局部变量声明：主要是instruction使用
        varDefType = varDecl.getVarType();
        for (VarDef varDef: varDecl.getVarDefs()) {
            builder.buildVarLocal(varDefType, varDef);
        }
    }

    // 注意一个decl中可能有好多def --> 在def中进行value生成
    public void visitConstDecl(ConstDecl constDecl) {
        // 插入符号表
        if (globalVar_gen) {
            constDecl.insertSymbol(cur_ir_symTable);
            return;
        }
        // 下面是局部变量的const：需要自己逐个完成constDef的instruction
        varDefType = constDecl.getVarType(); // 传给下面value定义和symbol插入
        // 构建ConstValue--在上面的insert过程添加
        for (ConstDef constDef: constDecl.getConstDefs()) {
            builder.buildConstLocal(varDefType, constDef);
        }
    }

    private void visitMainFuncDef(MainFuncDef mainFuncDef) {
        // 构建一个function（但是名字是main）
        IRFunction mainFunc = builder.buildIRMainFunc();
        functions.add(mainFunc);
        cur_func = mainFunc;
//        cur_ir_symTable.insertSymbol(new FuncSymbol()); // main标识不加符号表了
        newBasicBlock(); // todo 在函数体开始遍历时 new成一个基本块，其实范围不准确，因为需要在跳转前面是一个基本块
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
            if (decl == null)
                return;
//            if (decl != null)
//                decl.insertSymbol(cur_ir_symTable);
            // 全局才加GlobalVar
            // 下面是局部变量声明
            visitDecl(decl);
            AllocaInst localVarDef = builder.buildLocalVar();
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
                // LVal '=' Exp ';' 赋值指令 ———— 有可能需要修改符号表和对应的value的值
                // TODO: 2024/11/26 数组未实现
                LVal lVal = stmt.getlVal();
                // 注意下面的方法不能使用！！！因为是在语义分析的Visitor中的符号表！不是IR的！
//                Symbol lVal_sym = lVal.getIdentSymbol();
                Symbol lVal_sym = cur_ir_symTable.findInCurSymTable(lVal.getIdentName());
                Exp exp = stmt.getExp();
                int val = exp.getIntValue();
                lVal_sym.setIntValue(val); // 下面符号改变的value不需要重复声明（只要对应语句
//                Symbol symbol = cur_ir_symTable.findInCurSymTable(lVal.)

                // 生成对应的赋值一系列操作的指令语句
                irValue = builder.buildExp(exp);
                IRValue lVal_irValue = lVal_sym.irValue; // alloca语句
                // 只要store到对应位置就行
                builder.buildStoreInst(irValue, lVal_irValue);
//                builder.buildAssignInsts(lVal_irValue, irValue);
            }
            case 2 -> {
                // [Exp] ';' 纯运算，不知道可不可以完全舍弃不翻译<--不可以！，因为最后到UnaryExp这步的时候，可能会退出函数调用!
                if (stmt.getExp() != null)
                    builder.buildExp(stmt.getExp());
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
                builder.buildRetInst(stmt);
            }
            case 8, 9 -> {
                // LVal '=' 'getint''('')'';'
                // LVal '=' 'getchar''('')'';'
                // 输入函数调用 和 赋值 指令
                irValue = builder.buildCallInst(stmt.getIOLibName());
                IRValue lValIrValue = builder.buildLVal(stmt.getlVal());
                builder.buildStoreInst(irValue, lValIrValue);
            }
            case 10 -> {
                // printf 输出函数调用
                // 输出函数都是void，不需要返回值
                builder.buildCallInst(stmt);
            }
        }
    }

    private void visitFuncDef(FuncDef funcDef) {
        // 先把函数名加入外层符号表
        funcDef.insertSymbol(cur_ir_symTable);

        newIRSymTable();
        ArrayList<IRType> arg_types = visitFuncFParams(funcDef.getFuncFParams());
        IRType ret_type = IRVoidType.voidType;
        switch (funcDef.getFuncType()) {
            case INTTK -> {
                ret_type = IRIntType.intType;
            }
            case CHARTK -> {
                ret_type = IRCharType.charType;
            }
            case VOIDTK -> {
//                ret_type = IRVoidType.voidType; // 其实这里可以省略啦
            }
        }
        IRFunction irFunction = new IRFunction(funcDef.getFuncName(), ret_type, arg_types);
        cur_func = irFunction;

        // 添加函数对应的IRValue
        Symbol symbol = cur_ir_symTable.findInCurSymTable(funcDef.getFuncName());
        symbol.setIrValue(cur_func);

        FuncFParams fParams = funcDef.getFuncFParams();
        if (fParams != null) {
            ArrayList<String> names = fParams.getIdentNames();
            ArrayList<IRArgument> arguments = cur_func.getIrArguments_list();
            // 没有像下面那个函数一样用getArgsFromFParams(FuncFParams)方法，所以args没有ident_name，要人为加上
            for (int i = 0; i < arg_types.size(); i++) {
                cur_func.getArgByIndex(i).setIdent_name(names.get(i));
            }
        }

        visitBlockInFunc(funcDef.getBlock()); // InFunc表明此时无需新建符号表和基本块
        functions.add(irFunction);
        if (ret_type instanceof IRVoidType && !(irFunction.getLastInst() instanceof RetInst)) {
            // 其他类型会显示return，但是void有可能没有
            Instruction inst = new RetInst();
//            System.out.println("in IR Generator:visitFuncDef(FuncDef funcDef), 缺少void ret");
            cur_basicBlock.addInst(inst);
        }
    }

    private void visitFuncDef_FArgsAfterF(FuncDef funcDef) {
        // 先把函数名加入外层符号表
        funcDef.insertSymbol(cur_ir_symTable);
        newIRSymTable();
//        ArrayList<IRType> arg_types = visitFuncFParams(funcDef.getFuncFParams());
        IRType ret_type = IRVoidType.voidType;
        switch (funcDef.getFuncType()) {
            case INTTK -> {
                ret_type = IRIntType.intType;
            }
            case CHARTK -> {
                ret_type = IRCharType.charType;
            }
            case VOIDTK -> {
//                ret_type = IRVoidType.voidType; // 其实这里可以省略啦
            }
        }
//        IRFunction irFunction = new IRFunction(funcDef.getFuncName(), ret_type, arg_types);
        IRFunction irFunction = new IRFunction(funcDef.getFuncName(), ret_type);
        cur_func = irFunction;
        getArgsFromFParams(funcDef.getFuncFParams());
//        ArrayList<IRArgument> args = getArgsFromFParams(funcDef.getFuncFParams());
        // TODO: 2024/11/26 每次使用都load？下面暂时在IRValue中保留标识符（indent_name），不是寄存器（name） => 便于在符号表中找symbol和对应值（后序使用的寄存器）
        // TODO: 2024/11/26 还是说在符号表中维持reg_name，但是指导书不认可?？
        // 就形参而言，arg的name需要保留，但是如果arg有ident_name，可从symbolTable中更新最后load的reg_name
        /* 符号表的存储格式同学们可以自己设计，下面给出符号表的简略示例，同学们在实验中可以根据自己需要自行设计。其中需要注意作用域与符号表的对应关系，以及必要信息的保存。 */
        // 函数形参加载
//        irFunction.addFParamsInst();
//        IRBasicBlock basicBlock = new IRBasicBlock(irFunction);
//        cur_basicBlock = basicBlock;
//        cur_func = irFunction;
//        for (IRArgument argument: args) {
//            // 先alloc，再store，
//        }
        visitBlockInFunc(funcDef.getBlock()); // InFunc表明此时无需新建符号表和基本块
        functions.add(irFunction);
        if (ret_type instanceof IRVoidType && !(irFunction.getLastInst() instanceof RetInst)) {
            // 其他类型会显示return，但是void有可能没有
            Instruction inst = new RetInst();
            cur_basicBlock.addInst(inst);
        }
    }

    public void newBasicBlock() {
        cur_basicBlock = new IRBasicBlock(cur_func);
//        cur_func.addReg_num(); // 这个应该是因为基本块的label占了，所以加1，没有一定要求（SSA不重复即可）
        cur_func.addBasicBlock(cur_basicBlock);
    }

    private void visitBlockInFunc(Block block) { // 已经新建符号表并加入形参，但是形参还需要alloca和load
        if (cur_func == null)
            return;
        newBasicBlock();
        // 在进入InFunc的BB函数前就处理完形参（因为懒得传FParams）
        // 反驳上一行，通过irFunc的成员变量也可以
        AllocaInst allocaInst;
        StoreInst storeInst;
        LoadInst loadInst;
        // 处理局部变量命名：reg_num（在func中）
        for (IRArgument argument: cur_func.getIrArguments_list()) {
            builder.buildFuncArgInsts(argument);
        }
        // 处理真正的语句
        ArrayList<BlockItem> blockItem_list = block.getBlockItem_list();
        for (BlockItem blockItem: blockItem_list) {
            visitBlockItem(blockItem);
        }
//        cur_func.addBasicBlock(cur_basicBlock); // 在newBasicBlock()方法中已经添加了
        exitCurScope();
    }

    private ArrayList<IRType> visitFuncFParams(FuncFParams funcFParams) {
        ArrayList<IRType> types = new ArrayList<>();
        if (funcFParams == null) {
            // 没有参数
            return types;
        }
        funcFParams.insertSymbol(cur_ir_symTable); // 插入符号表，方便下面取值
        // 构造IRTypes
        ArrayList<LexType> lexTypes = funcFParams.getArgTypes();
        for (LexType lexType: lexTypes) {
            switch (lexType) {
                // TODO: 2024/11/26 没有考虑函数参数是数组的情况
                case INTTK -> {
                    types.add(IRIntType.intType);
                }
                case CHARTK -> {
                    types.add(IRCharType.charType);
                }
            }
        }
        return types;
    }

    public ArrayList<IRArgument> getArgsFromFParams(FuncFParams funcFParams) {
        if (funcFParams.getFParamsDetail().isEmpty())
            return null;
        ArrayList<IRType> types = new ArrayList<>();
        ArrayList<IRArgument> arguments = new ArrayList<>();
        // 构造IRTypes
        LexType lexType;
        IRType irType = IRCharType.charType;
        Symbol symbol;
        IRArgument argument;
        for (FuncFParam fParam: funcFParams.getFParamsDetail()) {
            lexType = fParam.getVarType();
            switch (lexType) {
                // TODO: 2024/11/26 没有考虑函数参数是数组的情况
                case INTTK -> {
                    types.add(IRIntType.intType);
                    irType = IRIntType.intType;
                }
                case CHARTK -> {
                    types.add(IRCharType.charType);
                    irType = IRCharType.charType;
                }
            }
            fParam.insertSymbol(cur_ir_symTable);
            symbol = fParam.getSymbol(cur_ir_symTable); // 依次插入
            // 把生成的Args返回 -->  不在irFunc初始化创建的时候赋值Arg
            argument = new IRArgument(irType, "%" + cur_func.getLocalValRegNum()); // 一开始就给arg加入reg_num
            symbol.setIrValue(argument);
            argument.setIdent_name(symbol.getIdentName());
            // zy:test
            argument.printArg();
        }
        cur_func.setIrArguments_list(arguments); // 在alloc的时候再记录ident_name？--> 符号表
        return arguments;
    }

    public static void setLlvm_ir_gen(Boolean llvm_ir_gen) {
        IRGenerator.llvm_ir_gen = llvm_ir_gen;
    }
}
