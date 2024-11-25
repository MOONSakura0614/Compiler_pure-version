package llvm;

import llvm.type.IRFunctionType;
import llvm.type.IRType;
import llvm.value.IRFunction;
import llvm.value.IRGlobalVar;
import llvm.value.constVar.IRConst;
import llvm.value.constVar.IRConstInt;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 用来构建LLVM IR的所有value
 * @date 2024/11/16 13:10
 */
public class IRBuilder {
    private static final IRBuilder builder = new IRBuilder();

    private IRBuilder() {}

    public static IRBuilder getInstance() {
        return builder;
    }

    /* GlobalVariable */
    // 没有初值时，需要用0初始化值
    public IRGlobalVar buildIRGlobalVar() {
        // 构建的是非常量？————这里常量如果和变量一样构造？
        return null;
    }

    public IRConst buildConst() {
        // 构建常量：可能返回int型，char型，或者array
        return null;
    }

    public IRConstInt buildConstInt() {
        IRConstInt irConstInt = new IRConstInt();
        return irConstInt;
    }

    /* Instruction */

    /* Function */
    public IRFunction buildIRFunction(String name, IRType ret, ArrayList<IRType> paramTypes) {
        return new IRFunction(name, new IRFunctionType(ret, paramTypes), Boolean.FALSE);
    }

    public IRFunction buildIRMainFunc(String name, IRType ret, ArrayList<IRType> paramTypes) {
        return new IRFunction(name, new IRFunctionType(ret, paramTypes), Boolean.TRUE);
    }
}
