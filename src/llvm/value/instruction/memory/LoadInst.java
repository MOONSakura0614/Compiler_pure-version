package llvm.value.instruction.memory;

import com.sun.jdi.Value;
import llvm.type.IRPointerType;
import llvm.value.IRValue;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;

/**
 * @author 郑悦
 * @Description: 取出指针对应地址上的值
 * @date 2024/11/18 21:54
 */
public class LoadInst extends Instruction {
    public LoadInst(IRValue pointer) {
//        super(((IRPointerType) pointer.getIrType()).getTargetType(), Operator.Load);
//        setName("%" + ++valNumber);
        addOperand(pointer);
    }

    public IRValue getPointer() {
        return getOperand(0);
    }

    @Override
    public String toString() {
        return getName() + " = load " + getIrType() + ", " + getPointer().getIrType() + " " + getPointer().getName();
    }
}
