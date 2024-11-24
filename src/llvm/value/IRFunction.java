package llvm.value;

import llvm.type.IRFunctionType;
import llvm.type.IRType;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/18 21:39
 */
public class IRFunction extends IRGlobalValue {
    private Boolean isMainFunc;
    private ArrayList<IRBasicBlock> IRBasicBlock_list;

    public IRFunction() {
        IRBasicBlock_list = new ArrayList<>();
        isMainFunc = Boolean.FALSE;
    }

    public IRFunction(String name, IRType type, Boolean isMain, ArrayList<IRBasicBlock> irBasicBlocks) {
        super(type, name);
        isMainFunc = isMain;
        // 合成IRFuncType
    }

    public IRFunction(String name, IRFunctionType type, Boolean isMain) { // 传入IRFuncType才对
        super(type, name);
        isMainFunc = isMain;
        // 合成IRFuncType
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
