package llvm.value;

import llvm.type.IRFunctionType;
import llvm.type.IRType;
import llvm.value.instruction.Instruction;
import utils.IOUtils;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 包括函数定义和函数调用（no，函数调用在Call指令）？
 * @date 2024/11/18 21:39
 */
public class IRFunction extends IRGlobalValue {
    private Boolean isMainFunc;
    private ArrayList<IRArgument> irArguments_list;
    // 没懂为什么需要一个isLibrary的bool常量？-->查看是否是外联的库函数
    private ArrayList<IRBasicBlock> irBasicBlock_list;
    private int reg_num = 0; // 函数内部的
    // 还没涉及跳转，普通自定义函数，如果有args一定会先load

    public IRFunction() {
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
        isMainFunc = Boolean.FALSE;
    }

    public IRFunction(String name, IRType ret_type, ArrayList<IRType> irTypes) {
        // 普通自定义函数，直接传参数类型的list
        // 合成IRFuncType
        super(new IRFunctionType(ret_type, irTypes), name);
//        isMainFunc = isMain; // 不需要main标注，main和普通自定义函数是一样的（只是没参数，名字固定main，返回值固定int 0）
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
        // 得到形参
        for (IRType arg_type: irTypes) {
//            IRArgument argument = new IRArgument(arg_type, "%" + (this.reg_num++));
            IRArgument argument = new IRArgument(arg_type, "%reg_" + (this.reg_num++));
            irArguments_list.add(argument);
        }
        // 符号表在build 还是 AST遍历的时候记录？
    }

    /*public IRFunction(String name, IRType ret_type, ArrayList<IRArgument> arguments) {
        // 普通自定义函数，直接传参数类型的list
        // 合成IRFuncType
        super(new IRFunctionType(ret_type, irTypes), name);
//        isMainFunc = isMain; // 不需要main标注，main和普通自定义函数是一样的（只是没参数，名字固定main，返回值固定int 0）
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
        // 得到形参
        for (IRType arg_type: irTypes) {
            IRArgument argument = new IRArgument(arg_type, "%" + (this.reg_num++));
            irArguments_list.add(argument);
        }
        // 符号表在build 还是 AST遍历的时候记录？
    }*/

    public IRArgument getArgByIndex(int index) {
        return irArguments_list.get(index);
    }

    public IRFunction(String name, IRType ret_type, ArrayList<IRType> irTypes, Boolean isMain) {
        // 合成IRFuncType
        super(new IRFunctionType(ret_type, irTypes), name);
        isMainFunc = isMain;
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
        // 得到形参
        for (IRType arg_type: irTypes) {
            IRArgument argument = new IRArgument(arg_type, "%reg_" + (this.reg_num++));
            irArguments_list.add(argument);
        }
        // 符号表在build 还是 AST遍历的时候记录？
        if (isMainFunc) { // 不对，都是进block的时候比形参用的多一个
            reg_num = 1; // 从%1给值
        }
    }

//    public IRFunction(String name, IRFunctionType type, Boolean isMain) { // 传入IRFuncType才对
    public IRFunction(String name, IRType type) { // 传入IRFuncType
        super(new IRFunctionType(type, new ArrayList<>()), name);
//        isMainFunc = isMain;
        // 合成IRFuncType
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
    }

    public IRFunction(String name, IRFunctionType type) { // 传入IRFuncType
        super(type, name);
//        isMainFunc = isMain;
        // 合成IRFuncType
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
    }

    public void addBasicBlock(IRBasicBlock irBasicBlock) {
        // 注意维护基本块之间的跳转关系
        IRBasicBlock prev = null;
        if (!irBasicBlock_list.isEmpty()) {
            prev = irBasicBlock_list.get(irBasicBlock_list.size() - 1);
        }
        irBasicBlock_list.add(irBasicBlock);
        if (prev != null) {
            prev.setNextBlock(irBasicBlock);
            irBasicBlock.setPrevBlock(prev);
        }
        // 让全局量继承User
        addOperand(irBasicBlock);
    }

    @Override
    public void printIR() {
        IOUtils.writeLLVMIR(this.toString());
    }

    public void setIrArguments_list(ArrayList<IRArgument> irArguments_list) {
        this.irArguments_list = irArguments_list;
    }

    public ArrayList<IRArgument> getIrArguments_list() {
        return irArguments_list;
    }

    public Instruction getLastInst() {
        if (irBasicBlock_list.isEmpty())
            return null;

        return irBasicBlock_list.get(irBasicBlock_list.size() - 1).getLastInst();
    }

    public void fParams2Args(ArrayList<IRType> irTypes) {
        // 命名行动
        for (IRType arg_type: irTypes) {
            IRArgument argument = new IRArgument(arg_type, "%" + (++this.reg_num)); // 维护函数内的寄存器命名
            irArguments_list.add(argument);
        }
    }

    @Override
    public String toString() {
        StringBuilder funcDeclare = new StringBuilder();
        funcDeclare.append("define dso_local ");
        if (irType instanceof IRFunctionType)
            funcDeclare.append(((IRFunctionType) irType).getRet_type().toString()).append(" "); // void | i32 | i8
        funcDeclare.append("@" + getName() + '(');
        // 形参表
        // -emit-llvm结果：做library的函数和做main的函数：但是好像这里没有要求？
        // define dso_local void @play(int, int)(i32 signext %0, i32 signext %1)
        // define dso_local i32 @main(i32 signext %0)
        // 这里应该是任何函数，都是直接上形参表，不写纯类型的那个——比如(int, int)这种
        for (int i = 0; i < irArguments_list.size(); i++) {
            if (i != 0) {
                funcDeclare.append(", ");
            }
            // 这时候给argument赋值来得及吗，还是之前build function的时候？
//            argument.setName("%" + (++this.reg_num)); // 虚拟寄存器从0开始
            funcDeclare.append(irArguments_list.get(i).toString()); // 从%0开始？
        }
//        funcDeclare.append(") {\n"); // 形参结束，BasicBlock自带前面的换行
        funcDeclare.append(") {"); // 形参结束
        for (IRBasicBlock basicBlock: irBasicBlock_list) {
            funcDeclare.append(basicBlock.toString());
        }
        funcDeclare.append("}\n\n"); // 形参结束
        // '{\n'留给BasicBlock？和‘}\n’？
        return funcDeclare.toString();
    }

    public String getArgumentListIR() {
        if (irArguments_list.isEmpty()) {
            return null;
        }

        return "";
    }

    public String getBodyIR() {
        // 函数体内的IR，主要是instruction
        return null;
    }

    public void addFParamsInst() {

    }

    public int getReg_num() {
        return reg_num;
    }

    public void addReg_num() {
        reg_num++;
    }

    public int getLocalValRegNum() {
        int tmp = reg_num;
        reg_num++;
        return tmp;
    }

    public String getLocalValRegNumName() {
        int tmp = reg_num;
        reg_num++;
        return "reg_" + tmp;
    }

    public IRType getRetType() {
        return ((IRFunctionType) irType).getRet_type();
    }

    public ArrayList<IRType> getArgTypes() {
        return ((IRFunctionType) irType).getParam_type_list();
    }
}
