package llvm.value.instruction.terminator;

import llvm.type.IRVoidType;
import llvm.value.IRValue;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/18 21:57
 */
public class RetInst extends Instruction {
    // 注意void函数，不论源文件有无显式return;语句，在-S -emit-llvm 后都得生成ret void

    public RetInst() { // type, op
        super(IRVoidType.voidType, Operator.Ret);
    }

    public RetInst(IRValue retValue) {
        super(retValue.getIrType(), Operator.Ret);
        addOperand(retValue);
    }

    @Override
    public String toString() {
        if (!getOperandList().isEmpty()) {
            return "ret " + getOperandByIndex(0).getIrType() + " " + getOperandByIndex(0).getName();
        } else {
            return "ret void";
        }
    }
}
