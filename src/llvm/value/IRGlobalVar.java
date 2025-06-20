package llvm.value;

import frontend.parser.syntaxUnit.Decl;
import frontend.symbol.SymbolTable;
import llvm.IRModule;
import llvm.type.*;
import llvm.value.constVar.IRConst;
import llvm.value.constVar.IRConstArray;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 22:55
 */
//public class GlobalVariable extends IRValue { // 下面的父类继承IRUser
public class IRGlobalVar extends IRGlobalValue {
    protected Boolean isConst = Boolean.FALSE;
    protected Boolean isArray = Boolean.FALSE;
    protected IRValue irValue;
    protected int int_value;
//    protected char char_value;
    protected ArrayList<Integer> int_array_value;
    protected ArrayList<Character> char_array_value;
    protected IRConstArray initArray;

    public IRGlobalVar() {
        super();
        int_array_value = new ArrayList<>();
        char_array_value = new ArrayList<>();
    }

    public IRGlobalVar(IRValue irValue) {
        super();
        int_array_value = new ArrayList<>();
        char_array_value = new ArrayList<>();
        this.irValue = irValue;
        if (irValue instanceof IRConst) {
            isConst = Boolean.TRUE;
        }
        if (irValue instanceof IRConstArray) {
            initArray = (IRConstArray) irValue; // 用于打印@array_name = dso_local global 后面的那些部分
            isArray = Boolean.TRUE;
        }
        if (irValue.getIrType().equals(IRIntType.intType)) {
            irType = IRPointerType.i32PointType;
        } else if (irValue.getIrType().equals(IRCharType.charType)) {
            irType = IRPointerType.i8PointType;
        } else {
            irType = new IRPointerType(irValue.getIrType()); // 给arrayType再封装一层pointer
        }
//        IRModule.getInstance().addGlobalVar(this);
        // 重设全局变量的名称
        setName("@"+irValue.getName());
//        irValue.setName("@"+irValue.getName());
    }

    public IRGlobalVar(String name, IRValue irValue) {
        super.setName(name);
        int_array_value = new ArrayList<>();
        char_array_value = new ArrayList<>();
        this.irValue = irValue;
    }

    public IRGlobalVar(IRType type, String name, IRValue irValue) {
        super(type, name);
        int_array_value = new ArrayList<>();
        char_array_value = new ArrayList<>();
        this.irValue = irValue;
    }

    public static ArrayList<IRGlobalVar> genGlobalVariable(Decl decl, SymbolTable symbolTable) {
        return null;
    }

    public Boolean IsConst() {
        return isConst;
    }

    public IRType getGlobalVarIrType() {
        if (irValue == null)
            return null;

        return irValue.getIrType(); // 直接判断类型 instanceOf
        // int, constInt, intArray, char, constChar, charArray
        // stringConst -> i8*  --->   pointer
    }

    /*public String getGlobalVarName() {
        if (irValue == null)
            return null;

        return irValue.getName(); // 直接判断类型 instanceOf
        // int, constInt, intArray, char, constChar, charArray
        // stringConst -> i8*  --->   pointer
    }*/

    public void setInt_value(int int_value) {
        this.int_value = int_value;
    }

    public void setInt_array_value(ArrayList<Integer> int_array_value) {
        this.int_array_value = int_array_value;
    }

    public void setArray(Boolean array) {
        isArray = array;
    }

    public void setConst(Boolean aConst) {
        isConst = aConst;
    }

    @Override
    public String toString() {
//        return "@" + getGlobalVarName() + " = " + super.toString() + " global " + getGlobalVarIrType() + int_value;

        IRType type = getGlobalVarIrType();
        if (type == null)
            return super.toString(); // 错误输出

//        if (getGlobalVarIrType() instanceof IRIntType)
        if (isArray) {
            // 注意要用0-padding
            if (getGlobalVarIrType() instanceof IRArrayType) {
//                IRArrayType arrayType = (IRArrayType) type;
                return getName() + " = dso_local global " + initArray;
//                return "@" + getGlobalVarName() + " = dso_local global " + '[' + arrayType.getElementType().toString()
                /*return getName() + " = dso_local global " + '[' + arrayType.getElementType().toString()
                        + " x " + arrayType.getLength() + ']' + " " + getArrayContent(); // char也改成int输出*/
            }
        }
        return getName() + " = dso_local global " + getGlobalVarIrType() +" " + int_value; // char也改成int输出
//        return "@" + getGlobalVarName() + " = dso_local global " + getGlobalVarIrType() +" " + int_value; // char也改成int输出
    }

    public String getArrayContent() {
        StringBuilder sb = new StringBuilder();

        IRType type = getGlobalVarIrType();
        if (type == null)
            return super.toString(); // 错误输出

        if (!(getGlobalVarIrType() instanceof IRArrayType)) {
            return null; // 不是Array
        }
        IRArrayType arrayType = (IRArrayType) type;

        int array_len = arrayType.getLength();
        if (int_array_value.isEmpty()) {
            sb.append(arrayType.getElementType()).append(" 0");
        } else {
            sb.append(arrayType.getElementType()).append(" ").append(int_array_value.get(0));
        }
        for (int i = 1; i < array_len; i++) {
            if (i >= int_array_value.size()) {
                sb.append(arrayType.getElementType() + " 0");
            } else {
                sb.append(arrayType.getElementType()).append(" ").append(int_array_value.get(i));
            }
        }

        return sb.toString();
    }

    public IRValue getIrValue() {
        return irValue;
    }
}
