package llvm.type;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 19:02
 */
public class IRArrayType implements IRType {
    private final IRType elementType; // 数组元素类型
    private int length;

    public IRArrayType(IRType elementType) {
        this.elementType = elementType; // 注意是final元素，数组元素类型不变
        this.length = 1;
    }

    public IRArrayType(IRType elementType, int length) {
        this.elementType = elementType; // 注意是final元素，数组元素类型不变
        this.length = length;
    }

    public IRType getElementType() {
        return elementType;
    }

    public int getLength() {
        return length;
    }
}
