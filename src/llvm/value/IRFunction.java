package llvm.value;

import llvm.type.IRFunctionType;
import llvm.type.IRType;
import utils.IOUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 包括函数定义和函数调用（no，函数调用在Call指令）？
 * @date 2024/11/18 21:39
 */
public class IRFunction extends IRGlobalValue {
    private Boolean isMainFunc;
    private ArrayList<IRArgument> irArguments_list;
    // todo 没懂为什么需要一个isLibrary的bool常量？
    private ArrayList<IRBasicBlock> irBasicBlock_list;
    private int reg_num = -1; // 函数内部的

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
            IRArgument argument = new IRArgument(arg_type, "%" + (++this.reg_num));
            irArguments_list.add(argument);
        }
        // 符号表在build 还是 AST遍历的时候记录？
    }

    public IRFunction(String name, IRType ret_type, ArrayList<IRType> irTypes, Boolean isMain) {
        // 合成IRFuncType
        super(new IRFunctionType(ret_type, irTypes), name);
        isMainFunc = isMain;
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
        // 得到形参
        for (IRType arg_type: irTypes) {
            IRArgument argument = new IRArgument(arg_type, "%" + (++this.reg_num));
            irArguments_list.add(argument);
        }
        // 符号表在build 还是 AST遍历的时候记录？
    }

//    public IRFunction(String name, IRFunctionType type, Boolean isMain) { // 传入IRFuncType才对
    public IRFunction(String name, IRFunctionType type) { // 传入IRFuncType
        super(type, name);
//        isMainFunc = isMain;
        // 合成IRFuncType
        irArguments_list = new ArrayList<>();
        irBasicBlock_list = new ArrayList<>();
        /*if (isMainFunc) { // 不对，都是进block的时候比形参用的多一个
            reg_num = 0; // 从%1给值
        }*/
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

    @Override
    public String toString() {
        StringBuilder funcDeclare = new StringBuilder();
        funcDeclare.append("define dso_local ");
        funcDeclare.append(((IRFunctionType) irType).getRet_type().toString()).append(" "); // void | i32 | i8
        funcDeclare.append("@" + getName() + '(');
        // 形参表
        // TODO: 2024/11/24 疑似函数形参用的寄存器和函数体之间都要跳一个，比如4个形参0~3，但是4被跳过，下面从5开始
        // -emit-llvm结果：做library的函数和做main的函数：但是好像这里没有要求？
        // define dso_local void @play(int, int)(i32 signext %0, i32 signext %1)
        // define dso_local i32 @main(i32 signext %0)
        // 这里应该是任何函数，都是直接上形参表，不写纯类型的那个——比如(int, int)这种
        for (IRArgument argument: irArguments_list) {
            // 这时候给argument赋值来得及吗，还是之前build function的时候？
            argument.setName("%" + (++this.reg_num)); // 虚拟寄存器从0开始
            funcDeclare.append(argument.toString()); // 从%0开始？
        }
        funcDeclare.append(')'); // 形参结束
        // '{\n'留给BasicBlock？和‘}\n’？
        return funcDeclare.toString();
    }

    public String getArgumentListIR() {
        if (irArguments_list.isEmpty()) {
            return null;
        }

        return "";
    }

    public static void main(String[] args) {
        // 测试Java中的多重继承
        Son son = new Son("female");
        ((Father) son).setAge(10);
        son.setName("yyy");
        System.out.println(((Father) son).name); // 可以访问public变量和方法
        System.out.println(son.toString());
    }
}


class Grandpa {
    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    @Override
    public String toString() {
        return "grand ";
    }
}

class Father extends Grandpa {
    int age;

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        return sb.append("father ").toString();
    }
}

class Son extends Father {
    String sex;
    public Son(String sex) {
        this.sex = sex;
    }

    public Son() {}
}
