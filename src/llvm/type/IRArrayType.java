package llvm.type;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 19:02
 */
public class IRArrayType implements IRType {
    private final IRType elementType; // 数组元素类型
    private final int length;

    /* todo: Array-related 主要服务于函数形参是数组的情况 */
    public IRArrayType(IRType elementType) {
        this.elementType = elementType; // 注意是final元素，数组元素类型不变
        this.length = 0;
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

    @Override
    public String toString() {
        return "[" + length + " x " + elementType.toString() + "]";
    }

}
