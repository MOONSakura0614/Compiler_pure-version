package llvm;

import llvm.type.IRCharType;
import llvm.type.IRFunctionType;
import llvm.type.IRIntType;
import llvm.type.IRType;
import llvm.value.IRFunction;
import llvm.value.IRGlobalVar;
import llvm.value.IRValue;
import llvm.value.constVar.IRConst;
import llvm.value.constVar.IRConstChar;
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
    public IRGlobalVar buildIRGlobalVar(IRValue value) {
        // 构建的是非常量？————这里常量如果和变量一样构造？
        return new IRGlobalVar(value);
    }

    public IRConst buildConst() {
        // 构建常量：可能返回int型，char型，或者array
        return null;
    }

    public IRConstInt buildConstInt(String name, int val) {
        IRConstInt irConstInt = new IRConstInt(name, val);
        return irConstInt;
    }

    public IRConstInt buildConstInt(int val) { // 局部的常量
        IRConstInt irConstInt = new IRConstInt(val);
        return irConstInt;
    }

    /* Instruction */

    /* Function */
    public IRFunction buildIRFunction(String name, IRType ret, ArrayList<IRType> paramTypes) {
        return new IRFunction(name, ret, paramTypes);
    }

    public IRFunction buildIRMainFunc() {
        return new IRFunction("main", new IRFunctionType(IRIntType.intType, new ArrayList<>()));
    }

    public IRConstChar buildConstChar(String name, int val) {
        return new IRConstChar(name, val);
    }

    public IRValue buildChar(String name) { // 非const的char变量
        return new IRValue(IRCharType.charType, name);
    }

    public IRValue buildInt(String name) { // 非const的char变量
        return new IRValue(IRIntType.intType, name);
    }
}
