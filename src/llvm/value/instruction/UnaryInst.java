package llvm.value.instruction;

import frontend.lexer.Token;
import llvm.IRGenerator;
import llvm.type.IRIntType;
import llvm.value.IRValue;

/**
 * @author 郑悦
 * @Description: 单目运算
 * @date 2024/11/26 8:31
 */
public class UnaryInst extends Instruction {
    // 只有一个操作数
    Boolean isBiInst = Boolean.FALSE;

    public UnaryInst(Token op_token, IRValue unaryValue) {
        super(Operator.getOperator(op_token), "%" + IRGenerator.cur_func.getLocalValRegNum());
        addOperand(unaryValue);
        setIrType(IRIntType.intType);
    }

    public boolean isNot() {
        return this.getOperator() == Operator.Not;
    }

    @Override
    public String toString() {
        String s = getName() + " = ";
        switch (this.getOperator()) {
            case Add:
                s += "add i32 0, ";
                break;
            case Sub:
                s += "sub i32 0, ";
                break;
            case Not:
                // not !没有具体用途，是br的跳转到label判断罢了
                break;
            default:
                break;
        }
        s += this.getOperandByIndex(0).getName();
        return s;
    }
}
