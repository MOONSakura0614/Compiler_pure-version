package llvm.value;

import llvm.type.IRType;

/**
 * @author 郑悦
 * @Description: 记录函数的形参
 * @date 2024/11/24 16:47
 */
public class IRArgument extends IRValue {
    // 主要是%0,%1保留做形参？也可以再往上加？
    public IRArgument() {
        super(); // 自动初始化使用者列表等
    }

    public IRArgument(IRType type, String name) {
        super(type, name);
    }

    @Override
    public String toString() {
        return irType.toString() + " " + name;
    }

}
