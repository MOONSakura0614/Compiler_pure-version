package llvm.type;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 19:01
 */
public class IRPointerType implements IRType {
    public static final IRPointerType i32PointType;
    public static final IRPointerType i8PointType;
    private IRType element_type;

    public IRPointerType() {}

    public IRPointerType(IRType element_type) {
        this.element_type = element_type;
    }

    static {
        i32PointType = new IRPointerType(IRIntType.intType);
        i8PointType = new IRPointerType(IRCharType.charType);
    }

    public IRType getElement_type() {
        return element_type;
    }

    @Override
    public String toString() {
        return element_type.toString() + "*";
    }

    private Boolean isString = Boolean.FALSE; // 是字符串常量

    public IRPointerType(IRType element_type, boolean isString) {
        this.element_type = element_type;
        this.isString = isString;
    }

    public boolean isString() {
        return isString;
    }
}
