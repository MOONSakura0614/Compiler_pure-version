package llvm.value.constVar;

import llvm.type.IRType;
import llvm.value.IRValue;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 22:51
 */
public class IRConstInt extends IRConst {
    private int val;

    public IRConstInt() {
        super();
    }

    public IRConstInt(IRType type, String name, int val) {
        super(type, name);
    }
}
