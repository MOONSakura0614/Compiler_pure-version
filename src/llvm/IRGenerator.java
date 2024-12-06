package llvm;

import frontend.lexer.LexType;
import frontend.parser.Parser;
import frontend.parser.syntaxUnit.*;
import frontend.symbol.Symbol;
import frontend.symbol.SymbolTable;
import llvm.type.*;
import llvm.value.*;
import llvm.value.constVar.IRConstInt;
import llvm.value.instruction.BinaryInst;
import llvm.value.instruction.IcmpInst;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;
import llvm.value.instruction.memory.AllocaInst;
import llvm.value.instruction.memory.LoadInst;
import llvm.value.instruction.memory.StoreInst;
import llvm.value.instruction.terminator.BrInst;
import llvm.value.instruction.terminator.CallInst;
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
        newIRSymTable(); // 没有跳转不用newBB
        ArrayList<BlockItem> blockItem_list = block.getBlockItem_list();
        for (BlockItem blockItem: blockItem_list) {
            visitBlockItem(blockItem);
        }
        /*BlockItem tmpBlockItem;
        for (int i = 0; i < blockItem_list.size(); i++) {
            tmpBlockItem = blockItem_list.get(i);
            curBlockItem_index = i;
            curBlock = block;
            if (tmpBlockItem.getIRGen()) {
                continue; // 已经遍历过
            }
            visitBlockItem(tmpBlockItem);
        }*/
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
                // TODO: 2024/11/29 Exp普通的(非const，非global)应该都不能乱求值；：谨防除零陷阱
//                int val = exp.getIntValue();
//                lVal_sym.setIntValue(val); // 下面符号改变的value不需要重复声明（只要对应语句
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
                // 跳转 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                visitBranch(stmt);
                /*IRBasicBlock condBlock = cur_basicBlock; // 添加if中cond和br语句的基本块
                IRBasicBlock trueBlock; // cond条件成立时
                IRBasicBlock falseBlock;
                IRBasicBlock finalBlock;

                ArrayList<IRBasicBlock> condBlockList = new ArrayList<>(); // 为了回填跳转实现短路求值！——感觉只有Cond推出LOrExp
                condBlockList.add(cur_basicBlock);
                // 先遍历LAndExp(LAndExp对应的BB需要加为真直接跳trueBlock，因为是||衔接的)，但LAndExp内部的EqExp则需要跳到下一个Cond


                BinaryInst condInst = visitCond(stmt.getCond());
                // TODO: 2024/12/6 如果cond构造的时候new了新的Block注意此时condBlock要改变，一定是最后一次icmpInst添加到那个block
                condBlock = cur_basicBlock;
                // visitCond的时候把生成icmpInst的过程语句都添加到之前对应的基本块中:只剩br语句没存（以及br构造需要condInst）
                // TODO: 2024/12/5 注意cond的短路求值也要支持跳转，所以上面的block不能设成全局的！会混淆
                if (stmt.getHasElse()) {
                     */
                    /*basicBlock;
                     if (...) {
                        trueBlock;
                        ...
                        trueEndBlock;
                     } else {
                        falseBlock;
                        ...
                        falseEndBlock;
                     }
                     finalBlock;*/
                    /*
                    // br语句和ret语句下面是新的基本块，跳转语句是一个基本块的终止
                    // icmp和br语句都归于当前的基本块（还未新建基本块）
//                    BinaryInst condInst = visitCond(stmt.getCond());
                    // 需要存下当前的block:即上文的condBlock
                    newBasicBlock(); // cur_bb替换成新的
                    trueBlock = cur_basicBlock; // 得到label
                    visitStmt(stmt.getIfStmt());
                    newBasicBlock();
                    falseBlock = cur_basicBlock;
                    visitStmt(stmt.getElseStmt());

                    // 将跳转语句添加回之前保存的condBlock中
                    builder.buildBrInst(condBlock, trueBlock, falseBlock, condInst);

                    // 添加跳转到finalBlock的跳转语句：对trueBlock（防止执行FalseBlock）【FalseBlock也需要添加吗？】
                    // 如果不想几百年后再回填跳转语句，只能马上生成finalBlock，但是就要稍微改之前的逻辑！
                    // TODO: 2024/12/5 之前是Block中每个BlockItem遍历，好处是Block是固定的
                    //  一个Func内只有一个全局Block，只是BlockItem可能也是一个Block罢了
                    // TODO: 2024/12/5 要不就用SlotTracker重新按每个虚拟寄存器出现的次数print（或者print之前统一调用重命名程序）
                    //  这里buildBrInst的时候就留个cond和空的%没有数字的label？——但是不知道具体跳转到哪里？？
                    // TODO: 2024/12/5 重构Block语法树结点，BlockItem里加一个是否构建过llvm？
                    // 或者下面直接new一个新的，之后退出去
                    // 1.继续在当前的Block未完待续的BlockItem_list中走下去
                    // 2.退回到上一个Block
                    // TODO: 2024/12/5 有个问题，如果有多个分支，可能有多个结束的基本块，那么是不是ret void在最后一次new出来的BB里面也很合理
                    //  【但是如果if-else的里面自带ret也很难办！！！_多生成的是死代码单纯不执行就ret了，还是ret后有br会被lli判断错误？】
                    newBasicBlock();
                    finalBlock = cur_basicBlock; // 当前的最新bb
                    builder.buildBrInst(trueBlock, finalBlock);
                    builder.buildBrInst(falseBlock, finalBlock);
                    // 注意如果是blockItemList里的最后一个，很可能又进入外层的Block中的新的里面又new一个？？
                    // 好像也不是那么愁：BB和B定义不同，不是{}一个Block就是一个BB，之前没有跳转和循环的时候都是一个函数体只有一个BB，B是用来new新的符号表的
                } else {
                     */
                    /*basicBlock;
                     if (...) {
                        trueBlock;
                     }
                     finalBlock;*/
                    /*
                    newBasicBlock();
                    trueBlock = cur_basicBlock;
                    visitStmt(stmt.getIfStmt());

                    newBasicBlock();
                    finalBlock = cur_basicBlock;
                    builder.buildBrInst(condBlock, trueBlock, finalBlock, condInst);
                }*/
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

    /* 单独处理分支语句 */
    public void visitBranch(Stmt stmt) {
        // 为了Cond相关
        // 处理Cond
        Cond cond = stmt.getCond();
        LOrExp lOrExp = cond.getlOrExp(); // 由||连接的
        IRBasicBlock curBlock = cur_basicBlock; // 当前块，需要加第一个icmp（顺序），以及后面补充的第一条br（回填）的块
        IRBasicBlock trueBlock; // label为真跳转到的基本块
        IRBasicBlock falseBlock;
        if (lOrExp.isOrLAndExpsEmpty()) {
            // 如果只由一个lAndExp（由&&连接）组成
            LAndExp lAndExp = lOrExp.getlAndExp();
            ArrayList<IRBasicBlock> lAndBB; // 基本块的最后一条是条件br结束，然后取最后一条指令set为假跳转到的
            lAndBB = visitLAndExp(lAndExp);
//            curBlock = cur_basicBlock; // 不知道上面lAndExp的分析过程是否new过基本块
            // (在EqExp1 && EqExp2 && ... 之间会互相转:假的话要直接转到falseBlock（这是没有||的情况，不然转到||后的下一个LAndExp）

            // 处理完仅有的一个lAndExp
//            newBasicBlock(); // 在处理lAndExp的最后已经newBB了
            trueBlock = cur_basicBlock;
            // 分析if-stmt
            visitStmt(stmt.getIfStmt()); // 构建true要跳转到BB（在visitLAndExp时已经加上trueBlock了）

            // 如果有else
            visitElseAndNextBlockItem(stmt, trueBlock, lAndBB);

            // 结束Else之后会有总的结束块（这里唯一问题是对于void函数[只有ret void会被检查加回去]，
            // 如果if，else中都有ret语句，那ret后这俩基本块还加了无条件跳转到结束块的，
            // 结束块没指令被加上ret void，会不会lli出错）
        } else {
            // 有||连接的多个lOrExp
            LAndExp lAndExp = lOrExp.getlAndExp();
            ArrayList<IRBasicBlock> lAndBB;
            lAndBB = visitLAndExp(lAndExp); // 第一个LAndExp-->true跳到下个&&后的eqExp，最后一个eqExp为真则跳ifBlock

            // if一定有对应的ifStmt，即使为空
            trueBlock = cur_basicBlock;
            visitStmt(stmt.getIfStmt()); // 此时最后一条EqExp成立时跳转到就是真的
            // llvm ir虚拟寄存器的number没有说按出现顺序严格递增，只要不重复就行

            // 处理||后面的多个LAndExps
//            ArrayList<ArrayList<IRBasicBlock>> lOrBB_lAndBBs = new ArrayList<>();
            // 每个元素是lAndBBs,然后lAndBB中的最后一个为真要跳if，为假跳下一个lOrExp（前面的为假也跳下一个lOrExp，没有下一个就跳else或者final）
//            lOrBB_lAndBBs.add(lAndBB);
//            ArrayList<IRBasicBlock> tmpLAndBB = lAndBB;
            for (LAndExp lAnd: lOrExp.getOrLAndExps()) {
                // 上面newBB用于ifStmt了，所以进入新的LAndExp之前要再次newBB
                newBasicBlock();
//                for (IRBasicBlock block: tmpLAndBB) { // -->or的短路求值逻辑在下面的visitAfterOr里面设置trueBlock体现的
                for (IRBasicBlock block: lAndBB) {
                    refillLogicalBlock(block, cur_basicBlock, true); // set前一个lAndExp为假时需要跳转到的block
                }
                // 保证lAndBB中最后一条br指令的trueBlock都是正确设置的，只需改造falseBlock的跳转即可
                lAndBB = visitLAndExpAfterOr(lAnd, trueBlock);
            }
            // 最后一组的每个block的falseBlock还没有正确设置（&&中遇到false要直接跳转，因为没有新的||，所以只能跳转到else或者final）
            visitElseAndNextBlockItem(stmt, trueBlock, lAndBB);
        }
    }

    /* refill lAndExp */
    private void visitElseAndNextBlockItem(Stmt stmt, IRBasicBlock trueBlock, ArrayList<IRBasicBlock> lAndBB) {
        IRBasicBlock falseBlock;
        if (stmt.getHasElse()) {
            newBasicBlock();
            falseBlock = cur_basicBlock;
            visitStmt(stmt.getElseStmt());
            for (IRBasicBlock block: lAndBB) {
                refillLogicalBlock(block, falseBlock, true);
            }
            newBasicBlock();
            builder.buildBrInst(trueBlock, cur_basicBlock);
            builder.buildBrInst(falseBlock, cur_basicBlock);
        } else {
            newBasicBlock();
            for (IRBasicBlock block: lAndBB) {
                refillLogicalBlock(block, cur_basicBlock, true); // 相当于直接跳出去了
            }
            builder.buildBrInst(trueBlock, cur_basicBlock); // 无条件跳转
        }
    }

    private ArrayList<IRBasicBlock> visitLAndExp(LAndExp lAndExp) {
        ArrayList<IRBasicBlock> lAndBB = new ArrayList<>();
        IRBasicBlock tmpBlock = cur_basicBlock;
        BinaryInst condInst;
        if (lAndExp.isAndEqExpsEmpty()) {
            // 没有&&
            condInst = visitEqExp(lAndExp.getEqExp());
            lAndBB.add(cur_basicBlock);
            // 注意刚建立lAndExp，对于第一个eqExp的指令建立都是在当前基本块的（没有跳转，分支和ret就不用newBB）
//            lAndBB.add(visitEqExp(lAndExp.getEqExp()));
            // 为真跳到if【或者是||后的】，为假跳到else【都会new一次block代表下一个判断语句或者是if】
            newBasicBlock(); // 给下面要跳转的
            builder.buildBrInst(tmpBlock, cur_basicBlock, true, condInst); // 最后一个EqExp所在的最后一条跳转语句
        } else {
            // 为真才跳到下一个EqExp（就是分析第二个开始的Exp之前都要newBB，第一个EqExp结束的时候就有br）
            condInst = visitEqExp(lAndExp.getEqExp());
            lAndBB.add(cur_basicBlock);
            // 此时是第一个EqExp，还未添加br语句，下面添加br语句，但是br中的falseBlock需要后面才能回填

            for (EqExp eqExp: lAndExp.getAndEqExps()) {
                newBasicBlock();
                builder.buildBrInst(tmpBlock, cur_basicBlock, true, condInst); // 还没给tmpB赋新值，术语上一个EqExp的BB
                tmpBlock = cur_basicBlock; // 记录本次EqExp所在的基本块
                condInst = visitEqExp(eqExp);
                lAndBB.add(cur_basicBlock);
            }
            newBasicBlock(); // 给下面要跳转的
            builder.buildBrInst(tmpBlock, cur_basicBlock, true, condInst); // 最后一个EqExp所在的最后一条跳转语句
        }
        return lAndBB; // 返回的基本块都是需要在最后的brInst指令重新setFalseBlock的
    }

    private ArrayList<IRBasicBlock> visitLAndExpAfterOr(LAndExp lAndExp, IRBasicBlock trueBlock) {
        ArrayList<IRBasicBlock> lAndBB = new ArrayList<>();
        IRBasicBlock tmpBlock = cur_basicBlock;
        BinaryInst condInst;
        if (lAndExp.isAndEqExpsEmpty()) {
            // 没有&&
            condInst = visitEqExp(lAndExp.getEqExp());
            lAndBB.add(cur_basicBlock);
            builder.buildBrInst(tmpBlock, trueBlock, true, condInst); // 最后一个EqExp所在的最后一条跳转语句
        } else {
            // 为真才跳到下一个EqExp（就是分析第二个开始的Exp之前都要newBB，第一个EqExp结束的时候就有br）
            condInst = visitEqExp(lAndExp.getEqExp());
            lAndBB.add(cur_basicBlock);
            // 此时是第一个EqExp，还未添加br语句，下面添加br语句，但是br中的falseBlock需要后面才能回填

            for (EqExp eqExp: lAndExp.getAndEqExps()) {
                newBasicBlock();
                builder.buildBrInst(tmpBlock, cur_basicBlock, true, condInst); // 还没给tmpB赋新值，术语上一个EqExp的BB
                tmpBlock = cur_basicBlock; // 记录本次EqExp所在的基本块
                condInst = visitEqExp(eqExp);
                lAndBB.add(cur_basicBlock);
            }
            builder.buildBrInst(tmpBlock, trueBlock, true, condInst); // 最后一个EqExp所在的最后一条跳转语句
        }
        return lAndBB; // 返回的基本块都是需要在最后的brInst指令重新setFalseBlock的
    }

    public void refillLogicalBlock(IRBasicBlock block, IRBasicBlock nextBlock, boolean isLAnd) {
        BrInst brInst = (BrInst) block.getLastInst(); // 基本块的结束位br语句的
        if (isLAnd) {
            brInst.setFalseBlock(nextBlock);
        } else {
            brInst.setTrueBlock(nextBlock);
        }
    }

    private BinaryInst visitEqExp(EqExp eqExp) {
        IRValue res;
        // 添加一系列指令，包括icmp（但是最后icmp的%num要返回，给br做condInst）
        if (eqExp.isEqOpRelExpsEmpty()) {
            // 如果只有一个RelExp组成
//            System.out.println("only 1 eqExp: eqExp didn't have RelExp??:" + (eqExp.getRelExp() == null)); // 全true，没有这种情况
            res = visitRelExp(eqExp.getRelExp());
//            System.out.println("only 1 eqExp: " + (res == null));
//            return visitRelExp(eqExp.getRelExp());
        } else {
            // 获取的首先是i1的需要conv
            IRValue tmp = visitRelExp(eqExp.getRelExp()); // 获取一个i1
//            System.out.println("multi eqExps: " + (tmp == null));
            IRValue icmp4rel;
            for (EqExp.EqOp_RelExp eqOp_relExp: eqExp.getEqOp_relExp_list()) {
                icmp4rel = visitRelExp(eqOp_relExp.getRelExp());
                tmp = builder.buildIcmpInst(tmp, eqOp_relExp.getEqOp(), icmp4rel);
            }
            res = tmp;
//            return (BinaryInst) tmp;
        }
        return dealWithConstIntInCond(res);
    }

    private BinaryInst dealWithConstIntInCond(IRValue cond) {
        IcmpInst icmpInst;
        // Add->Mul->Unary(CallInst函数调用；UnaryInst正负号开头；Primary[LVal左值，ConstInt])
//        if (cond instanceof IRConstInt) {
        if (!(cond instanceof BinaryInst)) {
            // 等于0为false
            /* test 为啥IcmpInst的left是null */
//            System.out.println("in dealWithConstIntInCond: " + (cond == null));
            icmpInst = (IcmpInst) builder.buildIcmpInst(cond, Operator.Ne, IRConstInt.zeroConstInt);
//            icmpInst = new IcmpInst(Operator.Ne, cond, IRConstInt.zeroConstInt);
//            cur_basicBlock.addInst(icmpInst);
            // 不知道会不会出现其他情况（AddExp buildInst出来不是BinaryInst的情况？
            return icmpInst;
        } /*else if (cond instanceof CallInst) {

        }*/
        return (BinaryInst) cond;
    }

    /*private BinaryInst visitRelExp(RelExp relExp) {
        if (relExp.isRelOpAddExpsEmpty()) {
            // 只有AddExp组成
            // 因为AddExp有可能得到常数结果，所以不能直接cast
//            return (BinaryInst) builder.buildAddExp4Rel(relExp.getAddExp());
            return (BinaryInst) builder.buildAddExp(relExp.getAddExp()); // 如何面对是IRConstInt的情况
        } else {
            // 有RelOp连接，需返回RelOp计算后结果
            IRValue tmp = builder.buildAddExp(relExp.getAddExp()); // 对于初始的AddExp
            for (RelExp.RelOp_AddExp relOp_addExp: relExp.getRelOp_addExp_list()) {
                tmp = builder.buildIcmpInst(tmp, relOp_addExp.getRelOp(), relOp_addExp.getAddExp());
            }
            return (BinaryInst) tmp;
        }
    }*/

    private IRValue visitRelExp(RelExp relExp) {
        if (relExp.isRelOpAddExpsEmpty()) {
            // 只有AddExp组成
            // 因为AddExp有可能得到常数结果，所以不能直接cast
//            return (BinaryInst) builder.buildAddExp4Rel(relExp.getAddExp());
//            return builder.buildAddExp(relExp.getAddExp()); // 如何面对是IRConstInt的情况
            IRValue irValue1 = builder.buildAddExp(relExp.getAddExp());
//            System.out.println("only 1 eqExp: visitRelExp??:" + (irValue1 == null));
            return irValue1;
        } else {
            // 有RelOp连接，需返回RelOp计算后结果
            IRValue tmp = builder.buildAddExp(relExp.getAddExp()); // 对于初始的AddExp
            for (RelExp.RelOp_AddExp relOp_addExp: relExp.getRelOp_addExp_list()) {
                tmp = builder.buildIcmpInst(tmp, relOp_addExp.getRelOp(), relOp_addExp.getAddExp());
            }
            return tmp;
        }
    }

    /*private IRBasicBlock visitEqExp(EqExp eqExp) {
        return cur_basicBlock;
    }
    private BinaryInst visitCond(Cond cond) {
        // 看是否需要短路求值（出现||或者&&）
        return builder.buildLOrExpInIf(cond.getlOrExp());
    }*/

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

    public static void newBasicBlock() { // 1206代码生成2：改为static方便builder调用
        cur_basicBlock = new IRBasicBlock(cur_func);
//        cur_func.addReg_num(); // 这个应该是因为基本块的label占了，所以加1，没有一定要求（SSA不重复即可）
        cur_func.addBasicBlock(cur_basicBlock);
    }

    // 为了跳转和循环
//    private Block curBlock;
//    private int curBlockItem_index; // 便于遍历下一个item块

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
        /*BlockItem tmpBlockItem;
        for (int i = 0; i < blockItem_list.size(); i++) {
            tmpBlockItem = blockItem_list.get(i);
            curBlock = block; // 当前genIR的block
            curBlockItem_index = i; // 代表最后一次遍历的BlockItem（如果if_else是所在的block中的最后一个？怎么去上一级？？）
            // TODO: 2024/12/5 无法解决跨都是Block的BlockItem的问题（block item list的边界）
            if (tmpBlockItem.getIRGen()) {
                continue; // 已经遍历过
            }
            visitBlockItem(tmpBlockItem);
        }*/
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
//            argument.printArg();
        }
        cur_func.setIrArguments_list(arguments); // 在alloc的时候再记录ident_name？--> 符号表
        return arguments;
    }

    public static void setLlvm_ir_gen(Boolean llvm_ir_gen) {
        IRGenerator.llvm_ir_gen = llvm_ir_gen;
    }
}
