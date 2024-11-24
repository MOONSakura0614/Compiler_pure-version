package llvm.type;

/**
 * @author 郑悦
 * @Description: 8bit（截断低8位）的
 * @date 2024/11/19 0:00
 */
public class IRCharType implements IRType {
    @Override
    public String toString() {
        return "i8";
    }
}
