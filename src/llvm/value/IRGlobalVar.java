package llvm.value;

import frontend.parser.syntaxUnit.Decl;
import frontend.symbol.SymbolTable;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 22:55
 */
//public class GlobalVariable extends IRValue {
public class IRGlobalVar extends IRGlobalValue {
    private Boolean isConst;

    public static ArrayList<IRGlobalVar> genGlobalVariable(Decl decl, SymbolTable symbolTable) {
        return null;
    }

    public Boolean IsConst() {
        return isConst;
    }
}
