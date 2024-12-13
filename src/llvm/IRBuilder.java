package llvm;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.syntaxUnit.*;
import frontend.symbol.Symbol;
import llvm.type.*;
import llvm.value.*;
import llvm.value.constVar.*;
import llvm.value.instruction.*;
import llvm.value.instruction.memory.AllocaInst;
import llvm.value.instruction.memory.GEPInst;
import llvm.value.instruction.memory.LoadInst;
import llvm.value.instruction.memory.StoreInst;
import llvm.value.instruction.terminator.BrInst;
import llvm.value.instruction.terminator.CallInst;
import llvm.value.instruction.terminator.RetInst;

import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        /* todo: Array 处理函数形参是数组的情况 */
        /* void arrF(int a[]) {
            a[1] = 1;
        }
        define dso_local void @arrF(i32*){
            %2 = alloca i32*
            store i32* %0, i32** %2
            %3 = load i32*, i32** %2
            %4 = getelementptr i32, i32* %3, i32 1
            store i32 1, i32* %4
            ret void
        }*/
//        System.out.println(argument);

        // alloca:申请内存地址
        allocaInst = new AllocaInst(argument.getIrType());
        // 给符号表里的对应形参ident存上上面获得的内存地址
        String ident_name = argument.getIdent_name();
        allocaInst.setIdent_name(ident_name);
        allocaInst.setName("%" + cur_func.getLocalValRegNumName());
        Symbol symbol = cur_ir_symTable.findInCurSymTable(ident_name);
        if (symbol != null) {
//            System.out.println(allocaInst);
            symbol.setPointerReg(allocaInst.getName());
            symbol.setIrValue(allocaInst); // 形参对应的irValue
        }
        cur_basicBlock.addInst(allocaInst);
//        System.out.println(allocaInst);

        // store:存入对应值(store arg对应的reg 到 alloca对应的内存指针reg)
        /*storeInst = new StoreInst(argument, allocaInst);
        cur_basicBlock.addInst(storeInst);*/
        /* todo: Array 注意数组的store */
        buildStoreInst(argument, allocaInst);
        // 注意随用随load，在要用到这个值（查询调用的ident_name之类的再生成load指令）
        /* todo: Array 只有形参的array需要二次load，最后才是那个数组的指针（一个*不是**） */
        if (symbol != null && symbol.getIsArray()) {
            LoadInst tmpLoad = buildLoadInst(allocaInst);
            symbol.setPointerReg(tmpLoad.getName());
            symbol.setIrValue(tmpLoad);
        }
    }

    /*public AllocaInst buildLocalVar() {
        return null;
    }*/

    public void buildConstLocal(LexType varDefType, ConstDef constDef) {
        // TODO: 2024/11/26 没完成数组  --->  12.10改成数组单开，这个专门处理普通变量
        ident_name = constDef.getIdentName();
        /*if (constDef.getIsArray()) {
            return;
        }*/
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
        allocaInst.setName("%" + cur_func.getLocalValRegNumName());
        symbol.setPointerReg(allocaInst.getName());

        symbol.setIrValue(allocaInst);

        cur_basicBlock.addInst(allocaInst);

        storeInst = new StoreInst(value, allocaInst); // 因为是直接用数值，不是寄存器，所以不区分类型（不用进buildStore去类型转化
        cur_basicBlock.addInst(storeInst);
    }

    // TODO: 2024/11/28 这里暂时没有区分局部变量，在initVal可以直接求取，和不能计算出需要store和load，这两种情况（统一当作一步步处理） 
    public void buildVarLocal(LexType varDefType, VarDef varDef) {
        // TODO: 2024/11/26 没完成数组 --->  12.10改成数组单开，这个专门处理普通变量
        ident_name = varDef.getIdentName();
        /*if (varDef.getIsArray()) {
            return;
        }*/
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
        allocaInst.setName("%" + cur_func.getLocalValRegNumName());
        symbol.setIrValue(allocaInst); // 局部变量
        symbol.setPointerReg(allocaInst.getName());
        cur_basicBlock.addInst(allocaInst);

        // 注意普通的局部变量不一定有init
        if (varDef.getInitVal() != null) { // 如果有初始值
            // TODO: 2024/11/28 下面的赋值，没有完成数组的情况
            // 获取计算的最终结果
            value = buildInitVal(varDef.getInitVal()); // --> 获取最后存储结果的寄存器（load出来的）

            if (value == null)
                return;
            // 获取value过程的instructions已经存了
            /*if (type.equals(IRCharType.charType)) { // TODO: 2024/11/28 计算时全按照i32，先zext到32，然后最后根据结果需要什么选择是否trunc到8
                // 注意对res判断，进行类型转化（res是否类型为IRValue更好）——res就是initVal得出的IRValue结果，可能类型和type不同？
                convInst = buildConvInst(Operator.Trunc, value);
//                cur_basicBlock.addInst(convInst); // 类型转化的在构造出Inst的函数内部就添加了（就是上一行的build中
                *//*if (resValue == null) {
                    // 说明类型一致
                }*//*
                value = convInst;
            }
            storeInst = new StoreInst(value, allocaInst);
            cur_basicBlock.addInst(storeInst);*/
            buildStoreInst(value, allocaInst);
        }
    }

    public ConvInst buildConvInst(Operator op, IRValue value) {
        convInst = new ConvInst(op, value);
        cur_basicBlock.addInst(convInst);
        return convInst;
    }

    public IRValue buildInitVal(InitVal initVal) {
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

    public IRValue buildExp(Exp exp) {
        if (exp.getAddExp() == null)
            return null;
        return buildAddExp(exp.getAddExp());
    }

    public IRValue buildAddExp(AddExp addExp) {
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
    public BinaryInst buildBinaryInst(Operator op, IRValue irValue_left, IRValue irValue_right) {
        // 进行运算的时候都统一转化为i32，最后再按需要的结果转换类型
        if (irValue_left.getIrType() instanceof IRCharType) {
            irValue_left = buildConvInst(Operator.Zext, irValue_left);
        }
        if (irValue_right.getIrType() instanceof IRCharType) {
            irValue_right = buildConvInst(Operator.Zext, irValue_right);
        }
        return new BinaryInst(op, "%" + cur_func.getLocalValRegNumName(), irValue_left, irValue_right);
    }

    public IRValue buildMulExp(MulExp mulExp) {
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

    public CallInst buildCallInst(UnaryExp unaryExp) {
        // 获取当前IR符号表中的保存函数定义相关信息的符号
        symbol = cur_ir_symTable.findInCurSymTable(unaryExp.getIdent_token().getTokenValue());
        Symbol funcSym = symbol;
//        System.out.println(symbol.irValue);
        ArrayList<IRValue> rArgs = new ArrayList<>();
        if (unaryExp.getFuncRParams() != null) {
            // !!用公共变量就是不太好，在下面调用的过程中，symbol就被改了数据！！！
            rArgs = buildRealArgs(unaryExp.getFuncRParams());
        }
        // 针对原来的function中的类型要求对参数进行类型转换
        ArrayList<IRType> argTypes = ((IRFunction) funcSym.irValue).getArgTypes();
        IRValue arg;
        ArrayList<IRValue> refinedArgs = new ArrayList<>();
        for (int i = 0; i < rArgs.size(); i++) {
            arg = rArgs.get(i);
            if (!arg.getIrType().equals(argTypes.get(i))) {
                // 类型不同
                if (argTypes.get(i).equals(IRIntType.intType)) { // 说明形参需要的是i32
                    convInst = new ConvInst(Operator.Zext, arg);
                    cur_basicBlock.addInst(convInst);
                    arg = convInst;
                } else if (argTypes.get(i).equals(IRCharType.charType)) { // 需要说明是i8，否则会遇到数组基地址也强转成i8
                    convInst = new ConvInst(Operator.Trunc, arg);
                    cur_basicBlock.addInst(convInst);
                    arg = convInst;
                }
            }
            refinedArgs.add(arg);
        }
//        System.out.println(symbol.irValue);
//        callInst = new CallInst((IRFunction) funcSym.irValue, rArgs);
        callInst = new CallInst((IRFunction) funcSym.irValue, refinedArgs);
        cur_basicBlock.addInst(callInst);
        return callInst;
    }

    // 库函数调用
    // 输入
    public CallInst buildCallInst(String ioLibName) {
        // 获取当前IR符号表中的保存函数定义相关信息的符号
        symbol = cur_ir_symTable.findInCurSymTable(ioLibName);
        ArrayList<IRValue> rArgs = new ArrayList<>();
        callInst = new CallInst((IRFunction) symbol.irValue, rArgs);
//        System.out.println(symbol.irValue);
        cur_basicBlock.addInst(callInst);
        return callInst;
    }

    // 输出：针对print
    public void buildCallInst(Stmt stmt) {
        // 获取当前IR符号表中的保存函数定义相关信息的符号
        symbol = IOLib.PUT_CH.getIoFuncSym();
        IRFunction irFunction = (IRFunction) IOLib.PUT_CH.getIoFuncValue();

        String rawStr = stmt.getString_token().getTokenValue();
        String printStr; //  = stmt.getString_token().getTokenValue();
        // 注意这个string_token是包括前后的双引号的！需要去掉
        printStr = rawStr.substring(1, rawStr.length() - 1);

        ArrayList<IRValue> rArgs = new ArrayList<>();
        IRValue arg;
        ArrayList<Exp> exps = stmt.getPrintExps();
        // TODO: 2024/11/29 输出暂时用for循环逐字符输出，看后面要不要改成输出str(等完成数组)
        // 用逐字符输出方式，注意printf中格式串的拆解（占位符取出来，其他的输出），占位符部分输出PrintExp的内容

        if (!stmt.getHasPrintExp()) {
            // 直接逐个输出StringConst
            buildPutConstStrByChar(printStr);
            /*for (int i = 0; i < printStr.length(); i++) {
                arg = new IRConstInt(printStr.charAt(i));
                // 不用constChar因为，putchar也是要求i32做参数，用char还得zext
                callInst = new CallInst(irFunction, arg);
                cur_basicBlock.addInst(callInst);
            }*/
        } else {
            // 有占位符
            // 定义正则模式，匹配 %c 和 %d（大小写敏感）
            String pattern = "(%c|%d)";
            // 编译正则表达式
            Pattern compiledPattern = Pattern.compile(pattern);

            /* TODO：类型转换 有可能char数据用%d输出之类的 */
            Matcher matcher = compiledPattern.matcher(printStr);
            ArrayList<String> formatSpecifiers = new ArrayList<>();
            // 收集所有占位符
            while (matcher.find()) {
                formatSpecifiers.add(matcher.group());
            }

            // 使用 split 方法分割字符串
            String[] parts = compiledPattern.split(printStr);
            rArgs = buildPrintExps(exps);
            // 生成指令
            int i;
            // 占位符regex之间分开的空字符串也会记录在parts字符串数组中
            for (i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    buildPutConstStrByChar(parts[i]);
                }

                if (i < rArgs.size()) {
                    // 除了最后一条分割出的串，其他后面总是跟着一个占位符
                    // 最后一条字符串后面可能会还有一个（结尾的不计入）--> 只需通过exp的数量判断还有没有就行
//                    buildPutSingleVar(rArgs.get(i));
                    buildPutSingleVar(rArgs.get(i), formatSpecifiers.get(i));
                }
            }
            while (i < rArgs.size()) {
                // 除了最后一条分割出的串，其他后面总是跟着一个占位符
                // 最后一条字符串后面可能会还有一个（结尾的不计入）--> 只需通过exp的数量判断还有没有就行
//                buildPutSingleVar(rArgs.get(i));
                buildPutSingleVar(rArgs.get(i), formatSpecifiers.get(i));
                i++;
            }
        }
    }

    private void buildPutSingleVar(IRValue irValue, String s) {
        IRFunction irFunction = (IRFunction) IOLib.PUT_INT_32.getIoFuncValue();
        if (s.equals("%c")) {
            irFunction = (IRFunction) IOLib.PUT_CH.getIoFuncValue(); // 如果占位符是c打印字符；反之如上初始化的，打印数字
        }
        if (!(irValue.getIrType() instanceof IRIntType)) {
            irValue = buildConvInst(Operator.Zext, irValue); // 重载成i32的方便输出函数使用
//            irFunction = (IRFunction) IOLib.PUT_CH.getIoFuncValue(); // 要根据占位符类型，而不是变量类型
        }
        callInst = new CallInst(irFunction, irValue);
        cur_basicBlock.addInst(callInst);
    }

    public void buildPutSingleVar(IRValue irValue) {
        IRFunction irFunction = (IRFunction) IOLib.PUT_INT_32.getIoFuncValue();
        if (!(irValue.getIrType() instanceof IRIntType)) {
            irValue = buildConvInst(Operator.Zext, irValue); // 重载成i32的方便输出函数使用
            irFunction = (IRFunction) IOLib.PUT_CH.getIoFuncValue();
        }
        callInst = new CallInst(irFunction, irValue);
        cur_basicBlock.addInst(callInst);
    }

    public void buildPutConstStrByChar(String str) {
        IRConstInt arg;
        IRFunction function = (IRFunction) IOLib.PUT_CH.getIoFuncValue();

        // 处理字符串中的转义字符问题————防止\n被当成\、n两个字符对待
        String pattern = "\\\\n";
        Pattern compiledPattern = Pattern.compile(pattern);
        String[] parts = compiledPattern.split(str);
        // 谨防末尾的\n消失
        int cnt = 0;
        Matcher matcherC = compiledPattern.matcher(str);
        while (matcherC.find()) {
            cnt++; // '\n'出现的数量
        }

//        System.out.println("cnt:"+cnt);
//        System.out.println(str);
        int tmp = '\n';
        if (cnt > 0) {
            for (String s: parts) {
//                buildPutConstStrByChar(s); // parts中的肯定不含\n进入下面的else块中
                for (int i = 0; i < s.length(); i++) {
                    arg = new IRConstInt(s.charAt(i));
                    // 不用constChar因为，putchar也是要求i32做参数，用char还得zext
                    callInst = new CallInst(function, arg);
                    cur_basicBlock.addInst(callInst);
                }
                // 输出转义符\n
                if (cnt > 0) {
                    arg = new IRConstInt(tmp);
                    callInst = new CallInst(function, arg);
                    cur_basicBlock.addInst(callInst);
                    cnt--;
                }
//                System.out.printf("%chhh%d%c", tmp, tmp, tmp);
            }
            while (cnt > 0) {
                arg = new IRConstInt(tmp);
                callInst = new CallInst(function, arg);
                cur_basicBlock.addInst(callInst);
                cnt--;
            }
        } else {
            // 不含\n -- 可以直接输出
            for (int i = 0; i < str.length(); i++) {
                arg = new IRConstInt(str.charAt(i));
                // 不用constChar因为，putchar也是要求i32做参数，用char还得zext
                callInst = new CallInst(function, arg);
                cur_basicBlock.addInst(callInst);
            }
        }

    }

    private ArrayList<IRValue> buildPrintExps(ArrayList<Exp> exps) {
        ArrayList<IRValue> irValues = new ArrayList<>();
        for (Exp exp: exps) {
            irValues.add(buildExp(exp));
        }
        return irValues;
    }

    private ArrayList<IRValue> buildRealArgs(FuncRParams rParams) {
        ArrayList<IRValue> rArgs = new ArrayList<>();
        ArrayList<Exp> exps = rParams.getExps(); // 获取实参的表示
        for (Exp exp: exps) {
            rArgs.add(buildExp(exp)); // todo: Array 注意这里的实参可能是整个数组传递，不一定只传数组元素（所以Symbol是Array类型的也不一定有下标的exp可以求的！）
        }
        return rArgs;
    }

    // UnaryExp → PrimaryExp → '(' Exp ')' | LVal | Number | Character
    // %2 = add i32 1, 2  -- 可以直接计算的Number和Character处理【不涉及虚拟寄存器】
    public IRValue buildUnaryExp(UnaryExp unaryExp) {
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
                case NOT -> { // UnaryOp + UnaryExp(已在进入UnaryOp开头的情况中转化为unaryValue)
                    // 观察是否需要类型转化
//                    icmpInst = new IcmpInst(Operator.Eq, unaryValue, IRConstInt.zeroConstInt); // 因为是取非,unaryValue等0才能跳trueBlock
//                    cur_basicBlock.addInst(icmpInst);
                    icmpInst = (IcmpInst) builder.buildIcmpInst(unaryValue, Operator.Eq, IRConstInt.zeroConstInt);
                    return icmpInst;
                }
                default -> {
                    return null;
                }
            }
        } else if (unaryExp.getIsIdent()) {
            // call指令调用
            return buildCallInst(unaryExp);
        }
        return null;
    }

    public IRValue buildUnaryInst(Token unaryOp, IRValue unaryValue) {
        UnaryInst unaryInst = new UnaryInst(unaryOp, unaryValue);
        cur_basicBlock.addInst(unaryInst);
        return unaryInst;
    }

    // 常量是不是应该分离讨论：算了还是说把init中用到Number和Character的，和用ident的一样处理
    // TODO: 2024/11/28 应该在一开始识别常量，如果用常量init，那么alloc之后，直接store常值；；； 还是曲折一点，遇到常量先alloc，再store常量，再load，得到的新的虚拟寄存器的值再使用（store回去）
    public IRValue buildPrimaryExp(PrimaryExp primaryExp) {
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
            return buildLValInRight(primaryExp.getlVal());
        }
    }

    // LVal → Ident ['[' Exp ']']
    public IRValue buildLVal(LVal lVal) { // 获取可用的指针左值
        // 注意区分全局和局部变量
        // TODO: 2024/11/28 尚未实现getElement指令，无法操作数组
        symbol = cur_ir_symTable.findInCurSymTable(lVal.getIdentName());
        value = symbol.irValue; // 传回去的是IRPointer代表元素在内存中的位置
        /*if (symbol.isConstSymbol()) { // 源程序保证正确，所以不会出现常量再赋值
            // 关于局部变量在符号表中对应的IRValue的设置:Const肯定有对应的值不用愁，直接用val
            return new IRConstInt(symbol.getIntValue()); // 初始值，i32
        } else {
            return value;
        }*/
        /* 关于数组 */
        if (symbol.getIsArray()) {
            /*todo 目前的usage只有 LVal = getint() 的时候？*/
            Symbol tmpSymbol = symbol;
            IRValue index = buildExp(lVal.getExp()); // 由于是数组，数组不能直接作为左值，需要有
            /*todo 当前文法：当 LVal 表示数组时，方括号个数必须和数组变量的维数相同（即定位到元素）。*/
            return buildGEPInst(tmpSymbol.irValue, index);
        }
        return value;
    }

    public IRValue buildLValInRight(LVal lVal) { // 得到loadInst或常量——等号右边的式子
        // 注意区分全局和局部变量
        // TODO: 2024/11/28 尚未实现getElement指令，无法操作数组
        symbol = cur_ir_symTable.findInCurSymTable(lVal.getIdentName());
        value = symbol.irValue;
        /*System.out.println(lVal.getIdentName());
        System.out.println(symbol);
        System.out.println(symbol.getIsArray());*/
        Symbol tmpSym = symbol;
        IRValue tmpValue = value;

        /* todo: 处理调用的左值是数组的情况 */
        if (symbol.getIsArray()) { // 此时value存的是对应的数组地址
            /*if (lVal.getIsArrayElement()) {
            // 取值赋值：首先找对应的index（进行exp求值）
                IRValue index = buildExp(lVal.getExp()); // 若此时exp是lVal【应该得到对应的@或者alloca进行load
                // 取对应的数组元素
                gepInst = buildGEPInst(tmpValue, index); // 取出对应元素的地址
                // load出来
                return buildLoadInst(gepInst);
            } else {
                // 一整个传数组，需要加载到0的数组基地址
            }*/
            if (lVal.getIsArrayElement()) {
                IRValue index = buildExp(lVal.getExp()); // 若此时exp是lVal【应该得到对应的@或者alloca进行load
                // 取对应的数组元素
                // TODO: 2024/12/10 都叫你不要用全局的成员变量了吧！进了上一个buildExp的分析函数，symbol和value都乱套了！
                gepInst = buildGEPInst(tmpValue, index); // 取出对应元素的地址
                // load出来
                return buildLoadInst(gepInst);
            } else {
                // todo: Array 调用一个数组的情况，应该只有数组传参（不然不做左值？）==>可以直接写，只有一维数组
                // 一整个传数组，需要加载到0的数组基地址
                gepInst = buildGEPInst(tmpValue, 0);
                return gepInst; // 直接return这个数组的基地址指针
            }
        }

//        System.out.println(value);
        if (symbol.isConstSymbol()) {
            // 关于局部变量在符号表中对应的IRValue的设置:Const肯定有对应的值不用愁，直接用val
            return new IRConstInt(symbol.getIntValue()); // 初始值，i32
//            return symbol.irValue; // 初始值，i32
        } else {
            // 全局变量：直接用@
            // GlobalVar也是指针形式，需要先load再使用
            /*if (value instanceof IRGlobalVar) {
                return value;
            } else {
                // 局部变量，先load，再返回load的值
                return buildLoadInst(value); // 这个是左值在右边，给其他人赋值的情况?
            }*/
            return buildLoadInst(value); // 这个是左值在右边，给其他人赋值的情况?
        }
//        return value;
    }

    public GEPInst buildGEPInst(IRValue value, IRValue index) {
        // 得到index
        gepInst = new GEPInst(value, index);
        cur_basicBlock.addInst(gepInst);
        return gepInst;
    }

    public LoadInst buildLoadInst(IRValue value) {
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
                    loadInst = buildLoadInst(value);
                    /*loadInst = new LoadInst(value);
                    cur_basicBlock.addInst(loadInst);*/
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
    }

    public StoreInst buildAssignInst(IRValue lValIrValue, IRValue irValue) {
        // 前者为alloca的pointer，后者为右值（Exp）
        // 注意类型转化
        if (!irValue.getIrType().equals(((IRPointerType) lValIrValue.getIrType()).getElement_type())) {
            if (irValue.getIrType().equals(IRIntType.intType)) {
                irValue = buildConvInst(Operator.Trunc, irValue);
            } else if (irValue.getIrType().equals(IRCharType.charType)) {
                irValue = buildConvInst(Operator.Zext, irValue);
            }
        }
        storeInst = new StoreInst(irValue, lValIrValue);
        return storeInst;
    }

    public void buildStoreInst(IRValue irValue, IRValue lValIrValue) {
        /* todo: Array 因为IR阶段保证源代码正确，不会出现不符合文法的不同类型数组互传 */
        // 注意类型转化
        if (!irValue.getIrType().equals(((IRPointerType) lValIrValue.getIrType()).getElement_type())) {
            if (irValue.getIrType().equals(IRIntType.intType)) {
                irValue = buildConvInst(Operator.Trunc, irValue);
            } else if (irValue.getIrType().equals(IRCharType.charType)) {
                irValue = buildConvInst(Operator.Zext, irValue);
            }
        }
        /* store对应的内存位置在这里是lValIrValue */
        storeInst = new StoreInst(irValue, lValIrValue);
        cur_basicBlock.addInst(storeInst);
//        System.out.println(storeInst);
    }

    /* 服务于常量直接赋值 */
    public void buildStoreInst(int value, IRValue lValIrValue) {
        /* todo: Array 因为IR阶段保证源代码正确，不会出现不符合文法的不同类型数组互传 */
        // 注意类型:store是要到内存地址中，所以lVal通常是指针类
        IRConst irConst = new IRConst();
        IRType type = ((IRPointerType) lValIrValue.getIrType()).getElement_type();
        if (type instanceof IRIntType) {
            irConst = new IRConstInt(value);
        } else if (type instanceof IRCharType) {
            irConst = new IRConstChar(value);
        }
        /* store对应的内存位置在这里是lValIrValue */
//        System.out.println(irConst);
        storeInst = new StoreInst(irConst, lValIrValue);
        cur_basicBlock.addInst(storeInst);
//        System.out.println(storeInst);
    }

    // 跳转和循环
    private BrInst brInst;

    public void buildBrInst(IRBasicBlock condBlock, IRBasicBlock trueBlock, IRBasicBlock falseBlock, BinaryInst condInst) {
        brInst = new BrInst(condBlock, trueBlock, falseBlock, condInst);
//        condBlock.addInst(brInst); // ————在BrInst的constructor中统一构建了
    }

    public void buildBrInst(IRBasicBlock curBlock, IRBasicBlock finalBlock) {
        if (curBlock.getLastInst() instanceof BrInst) {
            return; // 防止连续两条跳转，第二条一定是无效的
        }
        brInst = new BrInst(curBlock, finalBlock);
//        curBlock.addInst(brInst);
    }

    public void buildBrInst(IRBasicBlock curBlock, IRBasicBlock branchBlock, boolean isLAnd, BinaryInst condInst) {
        /*if (curBlock.getLastInst() instanceof BrInst) {
            return;
        }*/
        if (!(condInst instanceof IcmpInst)) {
            /* 条件判断的非条件值问题：如果是计算得到的结果需要和0比较 */
            condInst = (IcmpInst) builder.buildIcmpInst(condInst, Operator.Ne, IRConstInt.zeroConstInt); // 再比较一次
        }
        brInst = new BrInst(curBlock, branchBlock, isLAnd, condInst);
//        curBlock.addInst(brInst);
    }

    IcmpInst icmpInst;
    public IRValue buildIcmpInst(IRValue tmp, Operator relOp, AddExp addExp) {
        // binaryInst(icmpInst) = tmp relOp addExp
        // tmp是AddExp的结果：i32类型
//        binaryInst = (BinaryInst) buildAddExp4Rel(addExp); // build的时候加入过基本块了
//        binaryInst = buildAddExp(addExp); // build的时候加入过基本块了
        // icmp允许%10 = icmp sgt i32 %9, 0 和常数比较，所以不需要非得是binaryInst的虚拟寄存器名称
        IRValue irValue = buildAddExp(addExp); // build的时候加入过基本块了
        // 注意传过来的tmp是否为i32类型，可能是IcmpInst relOp addExp（所以要类型转化）
//        if (tmp.getIrType() instanceof IRBoolType) {
        if (!(tmp.getIrType() instanceof IRIntType)) {
            tmp = buildConvInst(Operator.Zext, tmp);
        }
        if (!(irValue.getIrType() instanceof IRIntType)) {
            irValue = buildConvInst(Operator.Zext, irValue);
        }
        icmpInst = new IcmpInst(relOp, tmp, irValue); // 两个i32得到一个i1的结果
        // new而不是调用builder方法（除了BrInst）都要手动加入当前基本块
        cur_basicBlock.addInst(icmpInst);
        return icmpInst;
    }

    public IRValue buildIcmpInst(IRValue tmp, Operator eqOp, IRValue rel) {
        /* 改成变成i32统一比较 */
        if ((tmp.getIrType() instanceof IRBoolType) || (tmp.getIrType() instanceof IRCharType)) {
            tmp = buildConvInst(Operator.Zext, tmp);
        }
        if ((rel.getIrType() instanceof IRBoolType) || (rel.getIrType() instanceof IRCharType)) {
            rel = buildConvInst(Operator.Zext, rel);
        }
        icmpInst = new IcmpInst(eqOp, tmp, rel); // 两个i32得到一个i1的结果
        // new而不是调用builder方法（除了BrInst）都要手动加入当前基本块
        cur_basicBlock.addInst(icmpInst);
        return icmpInst;
    }

    public void buildControlBrInst(Token breakContinueToken, IRBasicBlock changeBlockGlobal, IRBasicBlock exitBlockGlobal) {
        // 处于循环内部
        switch (breakContinueToken.getTokenType()) {
            case BREAKTK -> {
                brInst = new BrInst(cur_basicBlock, exitBlockGlobal);
            }
            case CONTINUETK -> {
                brInst = new BrInst(cur_basicBlock, changeBlockGlobal);
            }
        }
    }

    /* 处理全局数组：以及初始化 */
    public IRConstArray buildConstArray(String arrayName_global, int[] vals, IRType elementType) {
        IRArrayType arrayType = new IRArrayType(elementType, vals.length);
        return new IRConstArray(arrayType, arrayName_global, vals);
    }

    /* 处理局部数组 */
    int array_length;
    GEPInst gepInst;
    public void buildArrayLocal(LexType varDefType, VarDef varDef) { // 此时进过IRGenerator中的筛选已经确保是数组了
        ident_name = varDef.getIdentName();
        array_length = varDef.getArrayLen();
        IRType elementType = IRIntType.intType;
        // 插入符号表
        if (varDefType.equals(LexType.CHARTK)) {
            elementType = IRCharType.charType;
            varDef.insertCharSymbol(cur_ir_symTable);
        } else {
            varDef.insertSymbol(cur_ir_symTable);
        }
        IRArrayType arrayType = new IRArrayType(elementType, array_length);
        // 构建IRValue
        symbol = cur_ir_symTable.findInCurSymTable(ident_name);
        // 完善指令:因为是常量，可以直接计算出constInit的值，所以就不用提前load赋值Exp中用到的变量
        allocaInst = new AllocaInst(arrayType);
        allocaInst.setIdent_name(ident_name);
        allocaInst.setName("%" + cur_func.getLocalValRegNumName());
        symbol.setIrValue(allocaInst); // 局部数组是用alloca指令对应的寄存器存数组位置的
        symbol.setPointerReg(allocaInst.getName());
        cur_basicBlock.addInst(allocaInst);

        // 注意普通的局部变量不一定有init
        if (varDef.getInitVal() != null) { // 如果有初始值
            // 获取计算的最终结果
            buildArrayInitVal(allocaInst, varDef.getInitVal()); // --> 获取最后存储结果的寄存器（load出来的）
        }
    }

    public void buildArrayInitVal(AllocaInst allocaInst, InitVal initVal) { // '{' [ Exp { ',' Exp } ] '}' | StringConst
        /* 要不IRConstString还是提上日常，不然要处理很多转移？ */
        // TODO: 2024/12/10 字符串转移未处理，暂时处理{}赋值的int和char数组
        if (initVal.getStringInit()) {
            // todo: charArray 的string“” init方式，需要注意是否替换转义字符 [ 以及原来存储的时候str还包括""前后得舍去，然后因为是字符串赋值的，最后要加一个'\0'就是ascii=0 ]
//            System.out.println(initVal.getStringInit());
            int[] chars = IRConstString.convertStrToAscii(initVal.getString());
            for (int i = 0; i < chars.length; i++) {
                gepInst = buildGEPInst(allocaInst, i);
                buildStoreInst(chars[i], gepInst);
            }
            return;
        }
        if (initVal.getArrayInit()) {
            ArrayList<Exp> exps = initVal.getInitExps();
            for (int i = 0; i < exps.size(); i++) {
                // 按序初始化赋值数组元素
                // 首先获取要赋值的数组元素的指针
                gepInst = buildGEPInst(allocaInst, i);
                // 获取对应元素的irValue，处理exp
                value = buildExp(exps.get(i)); // 如果exp是数组元素
                // 赋值
                buildStoreInst(value, gepInst);
            }
        }
    }

    public GEPInst buildGEPInst(IRValue arrayPointer, int i) {
        gepInst = new GEPInst(arrayPointer, i);
        cur_basicBlock.addInst(gepInst);
        return gepInst;
    }

    public void buildConstArrayLocal(LexType varDefType, ConstDef constDef) {
        // TODO: 2024/11/26 没完成数组  --->  12.10改成数组单开，这个专门处理普通变量
        ident_name = constDef.getIdentName();
        array_length = constDef.getArrayLen();
        IRType elementType = IRIntType.intType;
        // 插入符号表
        if (varDefType.equals(LexType.CHARTK)) {
            elementType = IRCharType.charType;
            constDef.insertCharSymbol(cur_ir_symTable);
        } else {
            constDef.insertSymbol(cur_ir_symTable);
        }
        IRArrayType arrayType = new IRArrayType(elementType, array_length);
        // 构建IRValue
        // 局部变量是在函数内的
        int val = constDef.getConstInitVal().getIntValue();
        /*if (elementType.equals(IRCharType.charType)) {
            // todo: 常量数组应该也可以用常量数组中的某个元素赋值，所以不能直接取值？（或者？常量数组值不会变直接存第一次的initArray（IRConstArray类型，中的值int[]数组取
                常量数组不用像变量数组一样，需要时刻维护值（而且不一定能在编译时计算，如函数调用等
            // char要模ascii码
            val %= 256;
        }*/
        // const一定有初始值
        value = new IRConst(elementType, "" + val, val);
//        value = new IRConst(type, "%" + cur_func.getLocalValRegNum(), val);
        value.setIdent_name(ident_name);
        symbol = cur_ir_symTable.findInCurSymTable(ident_name);
        symbol.setIrValue(value);
        symbol.setIntValue(val);
        // 完善指令:因为是常量，可以直接计算出constInit的值，所以就不用提前load赋值Exp中用到的变量
        allocaInst = new AllocaInst(value.getIrType());
        allocaInst.setIdent_name(ident_name);
        allocaInst.setName("%" + cur_func.getLocalValRegNumName());
        symbol.setPointerReg(allocaInst.getName());

        symbol.setIrValue(allocaInst);

        cur_basicBlock.addInst(allocaInst);

        storeInst = new StoreInst(value, allocaInst); // 因为是直接用数值，不是寄存器，所以不区分类型（不用进buildStore去类型转化
        cur_basicBlock.addInst(storeInst);
    }
}
