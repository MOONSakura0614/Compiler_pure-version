package llvm.value.instruction;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/26 8:31
 */
public class UnaryInst extends Instruction {

    public boolean isNot() {
        return this.getOperator() == Operator.Not;
    }
}
