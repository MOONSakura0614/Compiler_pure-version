package llvm.value.instruction;

import llvm.IRGenerator;
import llvm.type.IRCharType;
import llvm.type.IRIntType;
import llvm.value.IRValue;

/**
 * @author 郑悦
 * @Description: 类型转化指令，i32 <-> i8
 * @date 2024/11/26 2:56
 */
public class ConvInst extends Instruction {

    // TODO: 2024/11/26 还没写到基本块：没用跳转和循环语句（除了ret
    public ConvInst(Operator op, IRValue irValue) {
        super(op, "%" + IRGenerator.cur_func.getLocalValRegNumName());
//        System.out.println(IRGenerator.cur_func.getName());
//        System.out.println(getName());
        if (op == Operator.Zext) {
            setIrType(IRIntType.intType);
        } else if (op == Operator.Trunc) {
            setIrType(IRCharType.charType);
        }
        // 有点类似单目
        addOperand(irValue);
    }
    @Override
    public String toString() {
        if (getOperator() == Operator.Zext) {
            return getName() + " = zext " + getOperandByIndex(0).getIrType() + " " + getOperandByIndex(0).getName() + " to i32";
            /*if (getOperandByIndex(0).getIrType() instanceof IRBoolType) { // 对于icmp单独处理（或者i1改成getIrType也行，但是懒得调用函数）
                return getName() + " = zext i1 " + getOperandByIndex(0).getName() + " to i32";
            }
            return getName() + " = zext i8 " + getOperandByIndex(0).getName() + " to i32";*/
        } else if (getOperator() == Operator.Trunc) {
            return getName() + " = trunc i32 " + getOperandByIndex(0).getName() + " to i8";
        } else {
            return null;
        }
    }
}
