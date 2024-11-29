package llvm.value.instruction.terminator;

import com.sun.jdi.VoidType;
import frontend.parser.syntaxUnit.FuncRParams;
import llvm.IRGenerator;
import llvm.type.IRFunctionType;
import llvm.type.IRIntType;
import llvm.type.IRType;
import llvm.type.IRVoidType;
import llvm.value.IRArgument;
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
    private ArrayList<IRValue> realArgs;
//    private IRValue singleArg; // 形参表中只有一个变量
    // 为了输出统一，在constructor里面对上面的singleArg还是给一个新的realArgs，然后加入其中

    // CallInst的类型与返回值类型一致
    public CallInst(IRFunction function, ArrayList<IRValue> arguments) {
        super(((IRFunctionType) function.getIrType()).getRet_type(), Operator.Call);
        IRFunctionType functionType = (IRFunctionType) (function.getIrType());
        if (!(functionType.getRet_type() instanceof IRVoidType)) {
            setName("%" + IRGenerator.cur_func.getLocalValRegNum()); // 有返回值
        }
        setIrType(functionType.getRet_type()); // void 或者 具体的返回值类型
//        System.out.println(functionType.getRet_type());
//        System.out.println(irType instanceof IRIntType);
//        System.out.println(functionType);
        calledFunc = function;
        realArgs = arguments;
    }

    public CallInst(IRFunction function, IRValue arg) {
        super(((IRFunctionType) function.getIrType()).getRet_type(), Operator.Call);
        IRFunctionType functionType = (IRFunctionType) (function.getIrType());
        if (!(functionType.getRet_type() instanceof IRVoidType)) {
            setName("%" + IRGenerator.cur_func.getLocalValRegNum()); // 有返回值
        }
        setIrType(functionType.getRet_type()); // void 或者 具体的返回值类型
//        System.out.println(functionType.getRet_type());
//        System.out.println(irType instanceof IRIntType);
//        System.out.println(functionType);
        calledFunc = function;
//        singleArg = arg;
        realArgs = new ArrayList<>();
        realArgs.add(arg);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        IRType returnType = ((IRFunctionType) calledFunc.getIrType()).getRet_type();
        System.out.println(returnType);
        if (returnType instanceof IRVoidType) {
            s.append("call ");
        } else {
            s.append(this.getName()).append(" = call ");
        }
        s.append(returnType.toString()).append(" @").append(calledFunc.getName()).append("(");
        for (int i = 0; i < realArgs.size(); i++) {
            s.append(realArgs.get(i).getIrType().toString()).append(" ").append(realArgs.get(i).getName());
            if (i != realArgs.size() - 1) {
                s.append(", ");
            }
        }
        s.append(")");
        return s.toString();
    }
}
