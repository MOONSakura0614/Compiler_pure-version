package llvm.value.instruction.terminator;

import com.sun.jdi.VoidType;
import llvm.IRGenerator;
import llvm.type.IRFunctionType;
import llvm.type.IRType;
import llvm.type.IRVoidType;
import llvm.value.IRFunction;
import llvm.value.IRValue;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 函数调用指令
 * @date 2024/11/18 21:56
 */
public class CallInst extends Instruction {
    private IRFunction calledFunc;

    public CallInst(IRFunction function, ArrayList<IRValue> arguments) {
        super(function.getIrType(), Operator.Call);
        IRFunctionType functionType = (IRFunctionType) function.getIrType();
        if (!(functionType.getRet_type() instanceof IRVoidType)) {
            setName("%" + IRGenerator.cur_func.getLocalValRegNum()); // 有返回值
        }
        setIrType(functionType.getRet_type()); // void 或者 具体的返回值类型
        calledFunc = function;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        IRType returnType = ((IRFunctionType) calledFunc.getIrType()).getRet_type();
        if (returnType instanceof VoidType) {
            s.append("call ");
        } else {
            s.append(this.getName()).append(" = call ");
        }
        s.append(returnType.toString()).append(" @").append(calledFunc.getName()).append("(");
        for (int i = 1; i < this.getOperandList().size(); i++) {
            s.append(this.getOperandByIndex(i).getIrType().toString()).append(" ").append(this.getOperandByIndex(i).getName());
            if (i != this.getOperandList().size() - 1) {
                s.append(", ");
            }
        }
        s.append(")");
        return s.toString();
    }
}
