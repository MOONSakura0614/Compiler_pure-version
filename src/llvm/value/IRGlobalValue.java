package llvm.value;

import llvm.type.IRType;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 全局Value一定有name：是@a, @main形式的
 * @date 2024/11/15 21:17
 */
public class IRGlobalValue extends IRUser {

    public IRGlobalValue() {
        super();
    }

    public IRGlobalValue(IRType type, String name) {
        super(type, name);
    }

//    public void printIR() {}

    @Override
    public String toString() {
        return "define dso_local"; // 这个是函数
    }
}
