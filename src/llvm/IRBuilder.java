package llvm;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.syntaxUnit.*;
import frontend.symbol.ConstSymbol;
import frontend.symbol.Symbol;
import llvm.type.IRCharType;
import llvm.type.IRFunctionType;
import llvm.type.IRIntType;
import llvm.type.IRType;
import llvm.value.IRArgument;
import llvm.value.IRFunction;
import llvm.value.IRGlobalVar;
import llvm.value.IRValue;
import llvm.value.constVar.IRConst;
import llvm.value.constVar.IRConstChar;
import llvm.value.constVar.IRConstInt;
import llvm.value.instruction.BinaryInst;
import llvm.value.instruction.ConvInst;
import llvm.value.instruction.Operator;
import llvm.value.instruction.UnaryInst;
import llvm.value.instruction.memory.AllocaInst;
import llvm.value.instruction.memory.LoadInst;
import llvm.value.instruction.memory.StoreInst;
import llvm.value.instruction.terminator.CallInst;
import llvm.value.instruction.terminator.RetInst;

import java.util.ArrayList;

import static llvm.IRGenerator.*;

/**
 * @author 郑悦
 * @Description: 用来构建LLVM IR的所有value
 * @date 2024/11/16 13:10
 */
public class IRBuilder {
    private static final IRBuilder builder = new IRBuilder();

    private IRBuilder() {}

    public static IRBuilder getInstance() {
        return builder;
    }

    /* GlobalVariable */
    // 没有初值时，需要用0初始化值
    public IRGlobalVar buildIRGlobalVar(IRValue value) {
        // 构建的是非常量？————这里常量如果和变量一样构造？
        return new IRGlobalVar(value);
    }

    public IRConst buildConst() {
        // 构建常量：可能返回int型，char型，或者array
        return null;
    }

    public IRConstInt buildConstInt(String name, int val) {
        IRConstInt irConstInt = new IRConstInt(name, val);
        return irConstInt;
    }

    public IRConstInt buildConstInt(int val) { // 局部的常量
        IRConstInt irConstInt = new IRConstInt(val);
        return irConstInt;
    }

    /* Function */
    public IRFunction buildIRFunction(String name, IRType ret, ArrayList<IRType> paramTypes) {
        return new IRFunction(name, ret, paramTypes);
    }

    public IRFunction buildIRMainFunc() {
        return new IRFunction("main", new IRFunctionType(IRIntType.intType, new ArrayList<>()));
    }

    public IRConstChar buildConstChar(String name, int val) {
        return new IRConstChar(name, val);
    }

    public IRValue buildChar(String name) { // 非const的char变量
        return new IRValue(IRCharType.charType, name);
    }

    public IRValue buildInt(String name) { // 非const的char变量
        return new IRValue(IRIntType.intType, name);
    }

    /* Instruction */
    AllocaInst allocaInst;
    StoreInst storeInst;
    LoadInst loadInst;
    IRValue value;
    Symbol symbol;
    String ident_name;

    public void buildFuncArgInsts(IRArgument argument) { // 构建和函数形参有关的指令：至少包括alloca和store
        // alloca:申请内存地址
        allocaInst = new AllocaInst(argument.getIrType());
        // 给符号表里的对应形参ident存上上面获得的内存地址
        String ident_name = argument.getIdent_name();
        allocaInst.setIdent_name(ident_name);
        allocaInst.setName("%" + cur_func.getLocalValRegNum());
        Symbol symbol = cur_ir_symTable.findInCurSymTable(ident_name);
        if (symbol != null) {
            System.out.println(allocaInst);
            symbol.setPointerReg(allocaInst.getName());
            symbol.setIrValue(allocaInst); // 形参对应的irValue
        }
        cur_basicBlock.addInst(allocaInst);

        // store:存入对应值(store arg对应的reg 到 alloca对应的内存指针reg)
        storeInst = new StoreInst(argument, allocaInst);
        cur_basicBlock.addInst(storeInst);
        // 注意随用随load，在要用到这个值（查询调用的ident_name之类的再生成load指令）
    }

    public AllocaInst buildLocalVar() {
        return null;
    }

    public void buildConstLocal(LexType varDefType, ConstDef constDef) {
        // TODO: 2024/11/26 没完成数组
        ident_name = constDef.getIdentName();
        if (constDef.getIsArray()) {
            return;
        }
        IRType type = IRIntType.intType;
        // 插入符号表
        if (varDefType.equals(LexType.CHARTK)) {
            type = IRCharType.charType;
            constDef.insertCharSymbol(cur_ir_symTable);
        } else {
            constDef.insertSymbol(cur_ir_symTable);
        }
        // 构建IRValue
        // 局部变量是在函数内的
        int val = constDef.getConstInitVal().getIntValue();
        if (type.equals(IRCharType.charType)) {
            // char要模ascii码
            val %= 256;
        }
        // const一定有初始值
        value = new IRConst(type, "" + val, val);
//        value = new IRConst(type, "%" + cur_func.getLocalValRegNum(), val);
        value.setIdent_name(ident_name);
        symbol = cur_ir_symTable.findInCurSymTable(ident_name);
        // TODO: 2024/11/29 下面是优化准备的，直接常量不load，用值，但是现在代码生成一，所以暂时还是用alloca做局部变量符号的irValue
        //  Pointer的IRType也可以通过load再用
        symbol.setIrValue(value);
        symbol.setIntValue(val);
        // 完善指令:因为是常量，可以直接计算出constInit的值，所以就不用提前load赋值Exp中用到的变量
        allocaInst = new AllocaInst(value.getIrType());
        allocaInst.setIdent_name(ident_name);
        allocaInst.setName("%" + cur_func.getLocalValRegNum());
        symbol.setPointerReg(allocaInst.getName());

        symbol.setIrValue(allocaInst);

        cur_basicBlock.addInst(allocaInst);

        storeInst = new StoreInst(value, allocaInst);
        cur_basicBlock.addInst(storeInst);
    }

    // TODO: 2024/11/28 这里暂时没有区分局部变量，在initVal可以直接求取，和不能计算出需要store和load，这两种情况（统一当作一步步处理） 
    public void buildVarLocal(LexType varDefType, VarDef varDef) {
        // TODO: 2024/11/26 没完成数组
        ident_name = varDef.getIdentName();
        if (varDef.getIsArray()) {
            return;
        }
        IRType type = IRIntType.intType;
        // 插入符号表
        if (varDefType.equals(LexType.CHARTK)) {
            type = IRCharType.charType;
            varDef.insertCharSymbol(cur_ir_symTable);
        } else {
            varDef.insertSymbol(cur_ir_symTable);
        }
        // 构建IRValue
        symbol = cur_ir_symTable.findInCurSymTable(ident_name);
        // 完善指令:因为是常量，可以直接计算出constInit的值，所以就不用提前load赋值Exp中用到的变量
        allocaInst = new AllocaInst(type);
        allocaInst.setIdent_name(ident_name);
        allocaInst.setName("%" + cur_func.getLocalValRegNum());
        symbol.setIrValue(allocaInst); // 局部变量
        symbol.setPointerReg(allocaInst.getName());
        cur_basicBlock.addInst(allocaInst);

        // 注意普通的局部变量不一定有init
        if (varDef.getInitVal() != null) { // 如果有初始值
            // TODO: 2024/11/28 下面的赋值，没有完成数组的情况
            // 获取计算的最终结果
            value = buildInitVal(varDef.getInitVal()); // --> 获取最后存储结果的寄存器（load出来的）
            // 获取value过程的instructions已经存了
            if (type.equals(IRCharType.charType)) { // TODO: 2024/11/28 计算时全按照i32，先zext到32，然后最后根据结果需要什么选择是否trunc到8
                // 注意对res判断，进行类型转化（res是否类型为IRValue更好）——res就是initVal得出的IRValue结果，可能类型和type不同？
                convInst = buildConvInst(Operator.Trunc, value);
                cur_basicBlock.addInst(convInst); // 类型转化的在构造出Inst的函数内部就添加了（就是上一行的build中
                /*if (resValue == null) {
                    // 说明类型一致
                }*/
                value = convInst;
            }

            if (value == null)
                return;
            storeInst = new StoreInst(value, allocaInst);
            cur_basicBlock.addInst(storeInst);
        }
    }

    private ConvInst buildConvInst(Operator op, IRValue value) {
        return new ConvInst(op, value);
    }

    private IRValue buildInitVal(InitVal initVal) {
        // 获取最终结果的寄存器
        if (initVal.getArrayInit() || initVal.getStringInit()) {
            // 数组待实现
            return null;
        }
        // 进入Exp相关的运算的instruction构建
        if (initVal.getExp() == null)
            return null;
        return buildExp(initVal.getExp());
    }

    BinaryInst binaryInst;
    ConvInst convInst;
    Operator op;

    private IRValue buildExp(Exp exp) {
        if (exp.getAddExp() == null)
            return null;
        return buildAddExp(exp.getAddExp());
    }

    private IRValue buildAddExp(AddExp addExp) {
        MulExp mulExp = addExp.getMulExp();
        if (mulExp == null)
            return null; // 不可能发生的错误情况
        ArrayList<AddExp.AddOp_MulExp> addOp_mulExps = addExp.getAddOp_mulExp_list();
        // 应当递归构建：先把mul一直到最内部算明白，再往外
        // 最左推导
        if (addOp_mulExps.isEmpty()) {
            // 只有一个mulExp
            return buildMulExp(mulExp); // 此时MulExp产生的BInst已经加入block
        }
        // 逐条mul推导的irValue
        ArrayList<IRValue> values = new ArrayList<>();
        values.add(buildMulExp(mulExp));
        for (AddExp.AddOp_MulExp addOp_mulExp: addOp_mulExps) {
            values.add(buildMulExp(addOp_mulExp.getMulExp()));
        }
        AddExp.AddOp_MulExp addOp_mulExp;
        value = values.get(0); // 初始值
        // 计算出左右两边的mulExp之后，进行binaryInst的生成
        for (int i = 0; i < addOp_mulExps.size(); i++) {
            addOp_mulExp = addOp_mulExps.get(i);
            op = Operator.getOperator(addOp_mulExp.getAddOp_token());
            // 完成计算
            binaryInst = buildBinaryInst(op, value, values.get(i+1)); // 在函数内实现
            cur_basicBlock.addInst(binaryInst);
            value = binaryInst;
        }
        return value;
    }

    /* 二元运算的结果存储在一个新的虚拟寄存器中 */
    private BinaryInst buildBinaryInst(Operator op, IRValue irValue_left, IRValue irValue_right) {
        return new BinaryInst(op, "%" + cur_func.getLocalValRegNum(), irValue_left, irValue_right);
    }

    private IRValue buildMulExp(MulExp mulExp) {
        UnaryExp unaryExp = mulExp.getUnaryExp();
        if (unaryExp == null)
            return null;
        ArrayList<MulExp.MulOp_UnaryExp> mulOp_unaryExps = mulExp.getMulOp_unaryExp_list();
        if (mulOp_unaryExps.isEmpty()) {
            return buildUnaryExp(unaryExp);
        }
        // 逐条unaryExp推导的irValue
        ArrayList<IRValue> values = new ArrayList<>();
        values.add(buildUnaryExp(unaryExp));
        for (MulExp.MulOp_UnaryExp mulOp_unaryExp: mulOp_unaryExps) {
            values.add(buildUnaryExp(mulOp_unaryExp.getUnaryExp()));
        }
        MulExp.MulOp_UnaryExp mulOp_unaryExp;
        value = values.get(0); // 初始值
        // 计算出左右两边的mulExp之后，进行binaryInst的生成
        for (int i = 0; i < mulOp_unaryExps.size(); i++) {
            mulOp_unaryExp = mulOp_unaryExps.get(i);
            op = Operator.getOperator(mulOp_unaryExp.getMulOp_token());
            // 完成计算
            binaryInst = buildBinaryInst(op, value, values.get(i+1)); // 在函数内实现
            cur_basicBlock.addInst(binaryInst);
            value = binaryInst;
        }
        return value;
    }

    CallInst callInst;

    // UnaryExp → PrimaryExp → '(' Exp ')' | LVal | Number | Character
    // %2 = add i32 1, 2  -- 可以直接计算的Number和Character处理【不涉及虚拟寄存器】
    private IRValue buildUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.getIsPrimaryExp()) {
            PrimaryExp primaryExp = unaryExp.getPrimaryExp();
            return buildPrimaryExp(primaryExp);
        } else if (unaryExp.getIsOp()) {
            // UnaryOp只包括+ - !(!是单目）
            LexType lexType = unaryExp.getUnaryOp().getTokenType();
            IRValue unaryValue = buildUnaryExp(unaryExp.getUnaryExp());
            switch (lexType) {
                case PLUS, MINU -> {
                    return buildUnaryInst(unaryExp.getUnaryOp(), unaryValue); // 单目操作数（但双目操作符，所以左值补0）
                }
                case NOT -> { // 暂时还没写到跳转
//                    return buildUnaryInst();
                }
                default -> {
                    return null;
                }
            }
        } else if (unaryExp.getIsIdent()) {
            // call指令调用
        }
        return null;
    }

    private IRValue buildUnaryInst(Token unaryOp, IRValue unaryValue) {
        UnaryInst unaryInst = new UnaryInst(unaryOp, unaryValue);
        cur_basicBlock.addInst(unaryInst);
        return unaryInst;
    }

    // 常量是不是应该分离讨论：算了还是说把init中用到Number和Character的，和用ident的一样处理
    // TODO: 2024/11/28 应该在一开始识别常量，如果用常量init，那么alloc之后，直接store常值；；； 还是曲折一点，遇到常量先alloc，再store常量，再load，得到的新的虚拟寄存器的值再使用（store回去）
    private IRValue buildPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.getIsNumber()) {
            // 如果是数字，直接load
            return buildConstInt(primaryExp.getNumber().getIntValue());
//            return buildConstInt(Integer.parseInt(primaryExp.getNumber().getNumber_token().getTokenValue()));
        } else if (primaryExp.getIsParent()) {
            // 有括号，说明是(Exp)形式
            return buildExp(primaryExp.getExp());
        } else if (primaryExp.getIsCharacter()) {
            return buildConstInt(primaryExp.getCharacter().getIntValue());
        } else {
            // 左值部分：关于符号表取数（或者，取对应的虚拟寄存器号）
            return buildLVal(primaryExp.getlVal());
        }
    }

    // LVal → Ident ['[' Exp ']']
    private IRValue buildLVal(LVal lVal) {
        // 注意区分全局和局部变量
        // TODO: 2024/11/28 尚未实现getElement指令，无法操作数组
        symbol = cur_ir_symTable.findInCurSymTable(lVal.getIdentName());
        value = symbol.irValue;
        if (symbol.isConstSymbol()) {
            // 关于局部变量在符号表中对应的IRValue的设置:Const肯定有对应的值不用愁，直接用val
            return new IRConstInt(symbol.getIntValue()); // 初始值，i32
        } else {
            // 全局变量：直接用@
            if (value instanceof IRGlobalVar) {
                return value;
            } else {
                // 局部变量，先load，再返回load的值
                return buildLoadInst(value);
            }
        }
//        return value;
    }

    private IRValue buildLoadInst(IRValue value) {
        loadInst = new LoadInst(value);
        cur_basicBlock.addInst(loadInst);
        return loadInst;
    }

    RetInst retInst; // 返回语句
    public void buildRetInst(Stmt stmt) {
        if (stmt.getHasReturnExp()) {
            value = buildExp(stmt.getExp());
//            System.out.println(cur_func.getRetType());
//            System.out.println(value.getIrType());
            if (value == null)
                retInst = new RetInst();
            else {
                if (value instanceof IRGlobalVar) { // 全局变量存的是地址
                    // load pointer
                    loadInst = new LoadInst(value);
                    cur_basicBlock.addInst(loadInst);
                    value = loadInst;
//                    retInst = new RetInst(loadInst);
//                    cur_basicBlock.addInst(retInst);
//                    return;
                }
                if (!value.getIrType().equals(cur_func.getRetType())) {
//                    System.out.println("不匹配的ret");
                    if (value.getIrType().equals(IRIntType.intType)) {
                        convInst = new ConvInst(Operator.Trunc, value);
                        cur_basicBlock.addInst(convInst);
                    } else {
                        convInst = new ConvInst(Operator.Zext, value);
                        cur_basicBlock.addInst(convInst);
                    }
//                    System.out.println(convInst);
                    value = convInst;
                }
                retInst = new RetInst(value);
            }
        } else {
            // ret void
            retInst = new RetInst(); // 返回空
        }
        cur_basicBlock.addInst(retInst); // 每条ret语句也是属于自己对应的基本块的（函数可能被划分-->有多条return
//        System.out.println(retInst);
    }
}
