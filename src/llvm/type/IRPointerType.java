package llvm.type;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 19:01
 */
public class IRPointerType implements IRType {
    private IRType element_type;

    public IRPointerType(IRType element_type) {
        this.element_type = element_type;
    }

    public IRPointerType() {}
}
