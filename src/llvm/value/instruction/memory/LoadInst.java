package llvm.value.instruction.memory;

import llvm.IRGenerator;
import llvm.type.IRPointerType;
import llvm.value.IRGlobalVar;
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
        super(((IRPointerType) pointer.getIrType()).getElement_type(), Operator.Load);
//        setName("%" + ++valNumber);
        setName("%" + IRGenerator.cur_func.getLocalValRegNum());
        addOperand(pointer);
    }

    public IRValue getPointer() {
        return getOperandByIndex(0);
    }

    @Override
    public String toString() {
        return getName() + " = load " + getIrType() + ", " + getPointer().getIrType() + " " + getPointer().getName();
    }
}
