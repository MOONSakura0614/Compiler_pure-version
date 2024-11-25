package llvm.type;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/19 0:05
 */
public class IRVoidType implements IRType {
    public static final IRVoidType voidType;

    static {
        voidType = new IRVoidType();
    }

    @Override
    public String toString() {
        return "void";
    }
}
