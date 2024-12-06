package llvm.value.instruction;

import frontend.lexer.Token;
import llvm.IRGenerator;
import llvm.type.IRIntType;
import llvm.value.IRValue;
import llvm.value.constVar.IRConstInt;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/26 2:51
 */
public class BinaryInst extends Instruction {
    // 补充操作符的左操作数

    public BinaryInst(Operator operator, String name) {
        super(operator, name);
    }

    public BinaryInst(Operator operator, String name, IRValue left, IRValue right) {
        super(operator, name);
        // TODO: 2024/11/28 先不管数组，并且认为二目运算符暂时不实现条件判断相关，条件表达式的结果是i1吗？？先把结果类型定成int↓ 
        setIrType(IRIntType.intType);
        addOperand(left, right);
    }

    public BinaryInst(Token op_token, IRValue unaryValue) {
        super(Operator.getOperator(op_token), "%" + IRGenerator.cur_func.getLocalValRegNum());
        addOperand(unaryValue);
    }

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
                s += "and " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            case Or:
                s += "or " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            case Lt:
                s += "icmp slt " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            case Le:
                s += "icmp sle " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            case Ge:
                s += "icmp sge " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            case Gt:
                s += "icmp sgt " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            case Eq:
                s += "icmp eq " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            case Ne:
                s += "icmp ne " + this.getOperandByIndex(0).getIrType().toString() + " ";
                break;
            default:
                break;
        }
//        System.out.println(this.operator);
        IRValue left = this.getOperandByIndex(0);
        if (left instanceof IRConstInt) {
            s += ((IRConstInt) left).getVal();
        } else {
            s += left.getName(); // 有虚拟寄存器
        }
//        s += this.getOperandByIndex(0).getName() + ", " + this.getOperandByIndex(1).getName();
        s += ", ";
        IRValue right = this.getOperandByIndex(1);
        if (right instanceof IRConstInt) {
            s += ((IRConstInt) right).getVal();
        } else {
            s += right.getName(); // 有虚拟寄存器
        }
        return s;
    }
}
