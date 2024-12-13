package llvm.value.constVar;

import llvm.type.IRIntType;
import llvm.type.IRType;
import llvm.value.IRValue;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 22:51
 */
public class IRConstInt extends IRConst {
    public static final IRConstInt zeroConstInt = new IRConstInt(0);

    public IRConstInt() {
        super();
    }

    public IRConstInt(int val) {
        super(IRIntType.intType);
        setName("" + val);
        this.val = val;
    }

    public IRConstInt(String name, int val) {
        super(IRIntType.intType, name);
        this.val = val;
    }

    public IRConstInt(IRType type, String name, int val) {
        super(type, name);
    }

    @Override
    public String toString() {
        return "i32 " + val;
    }

    public void toCharAssignVal() {
        this.val = val % 128;
    }
}
