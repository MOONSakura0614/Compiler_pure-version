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
    protected String name; // 数字命名
    protected AbstractList<IRUser> userList; // def-use，保存value的使用者

    public IRValue() {
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

    public IRValue(String name) {
        this.name = name;
        userList = new ArrayList<>();
    }

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
