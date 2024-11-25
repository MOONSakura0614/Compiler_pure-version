package llvm.value;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 22:56
 */
public class IRBasicBlock extends IRValue {
    private IRBasicBlock prevBlock;
    private IRBasicBlock nextBlock;
    private IRFunction inFunc;

    public IRBasicBlock(IRFunction function) {
        inFunc = function;
    }

    public void setPrevBlock(IRBasicBlock prevBlock) {
        this.prevBlock = prevBlock;
    }

    public void setNextBlock(IRBasicBlock nextBlock) {
        this.nextBlock = nextBlock;
    }
}
