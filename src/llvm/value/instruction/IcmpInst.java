package llvm.value.instruction;

import llvm.IRGenerator;
import llvm.type.IRBoolType;
import llvm.type.IRIntType;
import llvm.value.IRValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 郑悦
 * @Description: 之前本来想把比较语句合并在BinaryInst（因为都是二元运算）
 * 但是因为icmp的结果是 i1 类型的（有可能还要类型转化，遂放弃，因此新建了以下这个类）
 * @date 2024/12/6 19:35
 */
public class IcmpInst extends BinaryInst {
    private static final Map<Operator, String> icmpOpMap = new HashMap<>() {{
        put(Operator.Eq, "eq");
        put(Operator.Ne, "ne");
        put(Operator.Gt, "sgt");
        put(Operator.Ge, "sge");
        put(Operator.Lt, "slt");
        put(Operator.Le, "sle");
    }};

    public IcmpInst(Operator op, IRValue left, IRValue right) { // 加入的左右操作数为i32类型，如果是RelExp需先进行ConvInst
        super(op, "%" + IRGenerator.cur_func.getLocalValRegNumName(), left, right);
        setIrType(IRBoolType.boolType);
    }

    @Override // 还是不能直接跟着父类BinaryInst的重写方法好了
    public String toString() {
        IRValue left = this.getOperandByIndex(0);
        IRValue right = this.getOperandByIndex(1);
        // 注意icmp的操作数都是i32！所以如果是RelExp成为操作数需要转化！
        return getName() + " = icmp " + icmpOpMap.get(getOperator()) + " " +
                IRIntType.intType + " " +
                left.getName() + ", " +
                right.getName();
    }

}
