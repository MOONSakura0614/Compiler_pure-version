package llvm.value.constVar;

import llvm.type.IRCharType;
import llvm.type.IRIntType;
import llvm.type.IRType;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 22:51
 */
public class IRConstChar extends IRConst {
    private int val = 0;

    public IRConstChar() {
        super();
    }

    public IRConstChar(int val) {
        super(IRCharType.charType);
        this.val = val;
    }

    public IRConstChar(String name, int val) {
        super(IRCharType.charType, name);
        this.val = val;
    }

    public IRConstChar(IRType type, String name, int val) {
        super(type, name);
        this.val = val;
    }

    public int getVal() { // 注意传回去int，但是实际i8
        return val;
    }
}
