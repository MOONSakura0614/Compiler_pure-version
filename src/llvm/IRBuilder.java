package llvm;

import frontend.symbol.Symbol;
import llvm.type.IRCharType;
import llvm.type.IRFunctionType;
import llvm.type.IRIntType;
import llvm.type.IRType;
import llvm.value.IRArgument;
import llvm.value.IRFunction;
import llvm.value.IRGlobalVar;
import llvm.value.IRValue;
import llvm.value.constVar.IRConst;
import llvm.value.constVar.IRConstChar;
import llvm.value.constVar.IRConstInt;
import llvm.value.instruction.memory.AllocaInst;
import llvm.value.instruction.memory.LoadInst;
import llvm.value.instruction.memory.StoreInst;

import java.util.ArrayList;

import static llvm.IRGenerator.*;

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

    /* Instruction */
    AllocaInst allocaInst;
    StoreInst storeInst;
    LoadInst loadInst;

    public void buildFuncArgInsts(IRArgument argument) { // 构建和函数形参有关的指令：至少包括alloca和store
        // alloca:申请内存地址
        allocaInst = new AllocaInst(argument.getIrType());
        // 给符号表里的对应形参ident存上上面获得的内存地址
        String ident_name = argument.getIdent_name();
        allocaInst.setIdent_name(ident_name);
        allocaInst.setName("%" + cur_func.getLocalValRegNum());
        Symbol symbol = cur_ir_symTable.findInCurSymTable(ident_name);
        if (symbol != null) {
            symbol.setPointerReg(allocaInst.getName());
        }
        cur_basicBlock.addInst(allocaInst);

        // store:存入对应值(store arg对应的reg 到 alloca对应的内存指针reg)
        storeInst = new StoreInst(argument, allocaInst);
        System.out.println(argument);
        System.out.println(allocaInst);
        cur_basicBlock.addInst(storeInst);
        System.out.println(storeInst);
        // 注意随用随load，在要用到这个值（查询调用的ident_name之类的再生成load指令）
    }

    public AllocaInst buildLocalVar() {
        return null;
    }
}
