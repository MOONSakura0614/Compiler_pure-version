package llvm.value.constVar;

import llvm.type.IRType;
import llvm.value.IRValue;

/**
 * @author 郑悦
 * @Description: 所有常量的基类
 * @date 2024/11/15 22:51
 */
public class IRConst extends IRValue {

    public IRConst() {
        super();
    }

    public IRConst(IRType type) {
        super(type);
    }

    public IRConst(IRType type, String name) {
        super(type, name);
    }
}
