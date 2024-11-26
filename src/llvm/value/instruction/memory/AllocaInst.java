package llvm.value.instruction.memory;

import llvm.type.IRPointerType;
import llvm.type.IRType;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/18 21:54
 */
public class AllocaInst extends Instruction {
    public AllocaInst(IRType type) { // 存的是地址指针
        super(new IRPointerType(type), Operator.Alloca);
    }

    @Override
    public String toString() {
        return getName() + " = alloca " + ((IRPointerType) irType).getElement_type();
    }
}
