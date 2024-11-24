package llvm;

import llvm.type.IRFunctionType;
import llvm.type.IRType;
import llvm.value.IRFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 郑悦
 * @Description: 用来构建LLVM IR的所有value
 * @date 2024/11/16 13:10
 */
public class Builder {
    private static final Builder builder = new Builder();

    private Builder() {}

    public static Builder getInstance() {
        return builder;
    }

    /* Function */
    public IRFunction buildIRFunction(String name, IRType ret, ArrayList<IRType> paramTypes) {
        return new IRFunction(name, new IRFunctionType(ret, paramTypes), Boolean.FALSE);
    }

    public IRFunction buildIRMainFunc(String name, IRType ret, ArrayList<IRType> paramTypes) {
        return new IRFunction(name, new IRFunctionType(ret, paramTypes), Boolean.TRUE);
    }
}
