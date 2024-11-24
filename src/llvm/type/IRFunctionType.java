package llvm.type;

import llvm.value.IRGlobalValue;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 19:01
 */
public class IRFunctionType implements IRType {
    private IRType ret_type;
    private ArrayList<IRType> param_type_list;
    private ArrayList<String> param_name_list; // 函数形参分配的寄存器

    public IRFunctionType(IRType retType, ArrayList<IRType> paramTypes, ArrayList<String> paramNames) {
        ret_type = retType;
        param_type_list = paramTypes;
        param_name_list = paramNames;
    }

    public IRFunctionType(IRType retType, ArrayList<IRType> paramTypes) {
        ret_type = retType;
        param_type_list = paramTypes;
    }

    public IRType getRet_type() {
        return ret_type;
    }

    public ArrayList<IRType> getParam_type_list() {
        return param_type_list;
    }

    public ArrayList<String> getParam_name_list() {
        return param_name_list;
    }
}
