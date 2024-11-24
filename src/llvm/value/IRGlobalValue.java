package llvm.value;

import llvm.type.IRType;

/**
 * @author 郑悦
 * @Description: 全局Value一定有name：是@a, @main形式的
 * @date 2024/11/15 21:17
 */
public class IRGlobalValue extends IRValue {
    public IRGlobalValue() {
        super();
    }

    public IRGlobalValue(IRType type, String name) {
        super(type, name);
    }

    @Override
    public String toString() {
        return "define dso_local";
    }
}
