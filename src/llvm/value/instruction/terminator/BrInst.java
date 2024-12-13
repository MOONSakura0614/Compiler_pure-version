package llvm.value.instruction.terminator;

import llvm.IRBuilder;
import llvm.value.IRBasicBlock;
import llvm.value.IRValue;
import llvm.value.instruction.Instruction;

/**
 * @author 郑悦
 * @Description: 有条件跳转（短路求值也用这个实现）和无条件跳转
 * @date 2024/11/18 21:56
 */
public class BrInst extends Instruction {
    private Boolean isUncondJump = Boolean.FALSE;
    private IRBasicBlock trueBlock;
    private IRBasicBlock falseBlock;
    private IRBasicBlock nextBlock;
    private IRValue condRes; // 通常是icmp的那条语句（懒得建IcmpInst，就用BinaryInst！）

    public BrInst(IRBasicBlock curBasicBlock, IRBasicBlock trueBasicBlock, IRBasicBlock falseBasicBlock, IRValue icmpInst) {
        condRes = icmpInst;
        isUncondJump = Boolean.FALSE;
        trueBlock = trueBasicBlock;
        falseBlock = falseBasicBlock;
        // 添加跳转语句
        curBasicBlock.addInst(this);
    }

    public BrInst(IRBasicBlock curBasicBlock, IRBasicBlock notShortCircuitBB, boolean isLAnd, IRValue icmpInst) {
        condRes = icmpInst;
        isUncondJump = Boolean.FALSE;
        if (isLAnd) {
            trueBlock = notShortCircuitBB; // &&中为假才能短路
        } else {
            falseBlock = notShortCircuitBB; // ||中为真才能短路，短路的需要后期回填，现在可以创建的是正常执行，非短路的
        }
        // 添加跳转语句
        curBasicBlock.addInst(this);
    }

    public BrInst(IRBasicBlock curBasicBlock, IRBasicBlock nextBasicBlock) {
        isUncondJump = Boolean.TRUE; // BB快结束跳转到出口（FinalBasicBlock）
        nextBlock = nextBasicBlock;
        // 在builder工厂的构造BrInst里面加了
        curBasicBlock.addInst(this);
    }

    // 为了LOrExp，一部分为真就可以调ifTrueBB【为假跳到下一个logical cond】
    public void setTrueBlock(IRBasicBlock trueBlock) {
        this.trueBlock = trueBlock;
    }

    // 为了LAndExp，前半部分为假就跳走，为真继续判断
    public void setFalseBlock(IRBasicBlock falseBlock) {
        this.falseBlock = falseBlock;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("br ");
        if (isUncondJump) {
            sb.append("label %").append(nextBlock.getName());
        } else {
            sb.append("i1 ").append(condRes.getName());
            sb.append(", label %").append(trueBlock.getName());
            sb.append(", label %").append(falseBlock.getName());
        }
        return sb.toString();
    }
}
