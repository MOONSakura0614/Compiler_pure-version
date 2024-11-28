package llvm.value.instruction.memory;

import llvm.value.IRValue;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;

/**
 * @author 郑悦
 * @Description: 存到内存中，被操作值在前
 * @date 2024/11/18 21:55
 */
public class StoreInst extends Instruction {
    private IRValue memReg; // pointer，内存地址
    private IRValue numValue; // 赋值

    public StoreInst(IRValue value, IRValue pointer) {
        super(value.getIrType(), Operator.Store);
        addOperand(value);
        addOperand(pointer);
        memReg = pointer;
        numValue = value;
//        System.out.println(operandList);
    }

    public IRValue getLVal() { // 把内存中取的变量[这里也是alloca取出的寄存器name]存到寄存器中
        return getOperandByIndex(0);
    }

    public IRValue getPointer() {
        return getOperandByIndex(1);
    }
    @Override
    public String toString() {
        return "store " + numValue.getIrType() + " " + numValue.getName()
                + ", " + memReg.getIrType() + " " + memReg.getName();
//        return "store " + getLVal().getIrType() + " " + getLVal().getName()
//                + ", " + getPointer().getIrType() + " " + getPointer().getName();
//                + ", " + getPointer().getIrType() + "* " + getPointer().getName();
    }
}
