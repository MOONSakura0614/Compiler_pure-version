package llvm.value;

import llvm.type.IRType;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 17:16
 */
public class IRUser extends IRValue {
    protected ArrayList<IRValue> operandList; // 使用者有操作数列表

    public IRUser() {
        super();
        operandList = new ArrayList<>();
    }

    public IRUser(IRType type, String name) {
        super(type, name);
        operandList = new ArrayList<>();
    }

    public ArrayList<IRValue> getOperandList() {
        return operandList;
    }

    public IRValue getOperand(int index) {
        if (index >= operandList.size())
            return null;

        return operandList.remove(index);
    }

    public void addOperand(IRValue operand) {
        this.operandList.add(operand);
        // 添加新的use边
    }
}
