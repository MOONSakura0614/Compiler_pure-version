package llvm.value.instruction.memory;

import com.sun.jdi.Value;
import llvm.IRGenerator;
import llvm.type.IRArrayType;
import llvm.type.IRPointerType;
import llvm.type.IRType;
import llvm.value.IRBasicBlock;
import llvm.value.IRGlobalVar;
import llvm.value.IRValue;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 郑悦
 * @Description: GetElementPtr指令——数组相关
 * @date 2024/12/5 20:19
 */
public class GEPInst extends Instruction {
    /*private IRType elementType;
    private IRValue target;

    public GEPInst(IRBasicBlock basicBlock, IRValue pointer, List<IRValue> indices) {
        super(new IRPointerType(getElementType(pointer, indices)), Operator.GEP, basicBlock);
        this.setName("%" + IRGenerator.cur_func.getLocalValRegNumName());
        if (pointer instanceof GEPInst) {
            target = ((GEPInst) pointer).target;
        } else if (pointer instanceof AllocaInst) {
            target = pointer;
        } else if (pointer instanceof IRGlobalVar) {
            target = pointer;
        }
        this.addOperand(pointer);
        for (IRValue value : indices) {
            this.addOperand(value);
        }
        this.elementType = getElementType(pointer, indices);
    }

    public GEPInst(IRBasicBlock basicBlock, IRValue pointer, int offset) {
        this(basicBlock, pointer, ((IRArrayType) ((IRPointerType) pointer.getIrType()).getTargetType()).offset2Index(offset));
    }

    public IRValue getPointer() {
        return getOperands().get(0);
    }

    private static IRType getElementType(IRValue pointer, List<IRValue> indices) {
        IRType type = pointer.getIrType();
        for (IRValue ignored : indices) {
            if (type instanceof IRArrayType) {
                type = ((IRArrayType) type).getElementType();
            } else if (type instanceof IRPointerType) {
                type = ((IRPointerType) type).getTargetType();
            } else {
                break;
            }
        }
        return type;
    }

    public List<Integer> getGEPIndex() {
        List<Integer> index = new ArrayList<>();
        for (int i = 1; i < getOperands().size(); i++) {
            index.add(((IRConstInt) getOperand(i)).getValue());
        }
        return index;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getName()).append(" = getelementptr ");
        // 如果是字符串，需要加 inbounds
        if (getPointer().getIrType() instanceof IRPointerType && ((IRPointerType) getPointer().getIrType()).isString()) {
            s.append("inbounds ");
        }
        s.append(((IRPointerType) getPointer().getIrType()).getTargetType()).append(", ");
        for (int i = 0; i < getOperands().size(); i++) {
            if (i == 0) {
                s.append(getPointer().getIrType()).append(" ").append(getPointer().getName());
            } else {
                s.append(", ").append(getOperands().get(i).getType()).append(" ").append(getOperands().get(i).getName());
            }
        }
        return s.toString();
    }*/
}
