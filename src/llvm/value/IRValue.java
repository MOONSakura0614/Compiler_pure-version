package llvm.value;

import llvm.type.IRType;

import java.util.AbstractList;
import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 17:16
 */
public class IRValue {
    protected IRType irType;
    protected String name; // 局部虚拟寄存器的%+数字命名 | GlobalValue的@+文字命名
    public static int reg_number = -1; // 每个函数应该是从0开始
    // 【和基本块的符号表不同，SSA跳出基本块但在同一函数内reg_num也是递增的】
    protected AbstractList<IRUser> userList; // def-use，保存value的使用者

    public IRValue() {
        System.out.println("test init");
        userList = new ArrayList<>(); // 注意static块只能初始化static变量，非类变量的单纯成员变量还是在构造的时候初始化
    }

    public IRValue(IRType type, String name) {
        this.irType = type;
        this.name = name;
        userList = new ArrayList<>();
    }

    public IRValue(IRType type) {
        this.irType = type;
        this.name = "";
        userList = new ArrayList<>();
    }

    public IRValue(String name) { // 只有全局变量才有@+真实name，其他都是维护虚拟寄存器数字
        this.name = name;
        userList = new ArrayList<>();
    }

    public void printIR() {}

    public void addUser(IRUser irUser) {
        this.userList.add(irUser);
    }

    public void setName(String name) {
        this.name = name;
    }

    public IRType getIrType() {
        return irType;
    }

    public String getName() {
        return name;
    }
}
