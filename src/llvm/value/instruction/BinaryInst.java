package llvm.value.instruction;

import com.sun.jdi.Value;
import llvm.value.IRValue;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/26 2:51
 */
public class BinaryInst extends Instruction {

    private void addOperand(IRValue left, IRValue right) {
        this.addOperand(left);
        this.addOperand(right);
    }

    public boolean isAdd() {
        return this.getOperator() == Operator.Add;
    }
    
    public boolean isSub() {
        return this.getOperator() == Operator.Sub;
    }
    
    public boolean isMul() {
        return this.getOperator() == Operator.Mul;
    }
    
    public boolean isDiv() {
        return this.getOperator() == Operator.Div;
    }
    
    public boolean isMod() {
        return this.getOperator() == Operator.Mod;
    }
    
    public boolean isAnd() {
        return this.getOperator() == Operator.And;
    }
    
    public boolean isOr() {
        return this.getOperator() == Operator.Or;
    }
    
    public boolean isLt() {
        return this.getOperator() == Operator.Lt;
    }
    
    public boolean isLe() {
        return this.getOperator() == Operator.Le;
    }
    
    public boolean isGe() {
        return this.getOperator() == Operator.Ge;
    }
    
    public boolean isGt() {
        return this.getOperator() == Operator.Gt;
    }
    
    public boolean isEq() {
        return this.getOperator() == Operator.Eq;
    }
    
    public boolean isNe() {
        return this.getOperator() == Operator.Ne;
    }
    
    public boolean isCond() {
        return this.isLt() || this.isLe() || this.isGe() || this.isGt() || this.isEq() || this.isNe();
    }
    
    @Override
    public String toString() {
        String s = getName() + " = ";
        switch (this.getOperator()) {
            case Add:
                s += "add i32 ";
                break;
            case Sub:
                s += "sub i32 ";
                break;
            case Mul:
                s += "mul i32 ";
                break;
            case Div:
                s += "sdiv i32 ";
                break;
            case Mod:
                s += "srem i32 ";
                break;
            case And:
                s += "and " + this.getOperand(0).getIrType().toString() + " ";
                break;
            case Or:
                s += "or " + this.getOperand(0).getIrType().toString() + " ";
                break;
            case Lt:
                s += "icmp slt " + this.getOperand(0).getIrType().toString() + " ";
                break;
            case Le:
                s += "icmp sle " + this.getOperand(0).getIrType().toString() + " ";
                break;
            case Ge:
                s += "icmp sge " + this.getOperand(0).getIrType().toString() + " ";
                break;
            case Gt:
                s += "icmp sgt " + this.getOperand(0).getIrType().toString() + " ";
                break;
            case Eq:
                s += "icmp eq " + this.getOperand(0).getIrType().toString() + " ";
                break;
            case Ne:
                s += "icmp ne " + this.getOperand(0).getIrType().toString() + " ";
                break;
            default:
                break;
        }
        s += this.getOperand(0).getName() + ", " + this.getOperand(1).getName();
        return s;
    }
}
