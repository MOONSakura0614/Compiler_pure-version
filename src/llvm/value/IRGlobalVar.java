package llvm.value;

import frontend.parser.syntaxUnit.Decl;
import frontend.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 22:55
 */
//public class GlobalVariable extends IRValue { // 下面的父类继承IRUser
public class IRGlobalVar extends IRGlobalValue {
    private Boolean isConst;
    private int int_value;
    private char char_value;
    private ArrayList<Integer> int_array_value;
    private ArrayList<Character> char_array_value;

    public IRGlobalVar() {
        super();
        int_array_value = new ArrayList<>();
        char_array_value = new ArrayList<>();
    }

    public static ArrayList<IRGlobalVar> genGlobalVariable(Decl decl, SymbolTable symbolTable) {
        return null;
    }

    public Boolean IsConst() {
        return isConst;
    }
}
