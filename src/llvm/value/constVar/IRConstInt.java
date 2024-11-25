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
    private int val = 0;

    public IRConstInt() {
        super();
    }

    public IRConstInt(int val) {
        super(IRIntType.intType);
        this.val = val;
    }

    public IRConstInt(String name, int val) {
        super(IRIntType.intType, name);
        this.val = val;
    }

    public IRConstInt(IRType type, String name, int val) {
        super(type, name);
    }

    public int getVal() {
        return val;
    }
}
