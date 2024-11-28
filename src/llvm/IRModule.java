package llvm;

/**
 * @author 郑悦
 * @Description: Module类似之前的CompUnit作用，在这里开始遍历
 * 1. 输出到对应文件
 * 2. 不确认AST node的visitor是否要在这里实现
 * @date 2024/11/13 16:09
 */

import llvm.value.IRFunction;
import llvm.value.IRGlobalVar;
import llvm.value.instruction.Instruction;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IRModule {
    private static final IRModule module = new IRModule(); // 单例模式
    private List<IRGlobalVar> globalVarList;
    private List<IRFunction> functionList;
    private HashMap<Integer, Instruction> instructions;

    private IRModule() {
        this.globalVarList = new ArrayList<>();
        this.functionList = new ArrayList<>();
//        this.functions = new IList<>(this);
        this.instructions = new HashMap<>();
    }

    public static IRModule getInstance() {
        /*if (module == null) // Cannot assign a value to final variable 'module'
            module = new IRModule();*/
        return module;
    }

    public void addGlobalVar(IRGlobalVar globalVar) {
        globalVarList.add(globalVar);
    }

    public void addFunction(IRFunction function) {
        functionList.add(function);
    }

    public List<IRGlobalVar> getGlobalVarList() {
        return globalVarList;
    }

    public List<IRFunction> getFunctionList() {
        return functionList;
    }

    public void setGlobalVarList(List<IRGlobalVar> globalVarList) {
        this.globalVarList = globalVarList;
    }

    public void setFunctionList(List<IRFunction> functionList) {
        this.functionList = functionList;
    }

    public void printIR() {
        IOUtils.clearFile(IOUtils.ir);
        IOUtils.initIROutput(); // 加入四条外联的输入输出函数
        for (IRGlobalVar globalVariable: globalVarList) {
            IOUtils.writeLLVMIR(globalVariable.toString() + '\n');
        }
        for (IRFunction function: functionList) {
            // 注意function定义是大工程，需要遍历打印
            IOUtils.writeLLVMIR(function.toString());
//            function.printIR();
        }
    }
}
