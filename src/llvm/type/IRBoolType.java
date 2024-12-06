package llvm.type;

/**
 * @author 郑悦
 * @Description: icmp指令对应虚存reg类型
 * @date 2024/12/6 19:38
 */
public class IRBoolType implements IRType {
    public static final IRBoolType boolType;

    static {
        boolType = new IRBoolType();
    }

    @Override
    public String toString() {
        return "i1";
    }
}
