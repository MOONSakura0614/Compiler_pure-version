package llvm.value.constVar;

import llvm.type.IRType;
import llvm.value.IRValue;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 所有常量的基类
 * @date 2024/11/15 22:51
 */
public class IRConst extends IRValue {
    protected int val = 0;
    protected ArrayList<Integer> initArray = new ArrayList<>();

    public IRConst() {
        super();
    }

    public IRConst(IRType type) {
        super(type);
    }

    public IRConst(IRType type, String name) {
        super(type, name);
    }

    public IRConst(IRType type, String name, int val) {
        // 单const值
        super(type, name);
        this.val = val;
    }

    public int getVal() { // 注意传回去int，但是实际i8
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    // 常量的string（用于store的赋值）
    @Override
    public String toString() {
        return "" + val; // Array的话，在ConstArray中重写toString方法
    }

    public static void main(String[] args) {
        int a = 10;
        String s = "" + a;
        System.out.println(s);
    }
}
