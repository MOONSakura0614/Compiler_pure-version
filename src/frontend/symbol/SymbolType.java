package frontend.symbol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/10/16 22:31
 */
public enum SymbolType {
    /*VAR,
    FUNC,
    ARRAY;*/
    ConstChar,
    ConstInt,
    ConstCharArray,
    ConstIntArray,
    Char,
    Int,
    CharArray,
    IntArray,
    VoidFunc,
    CharFunc,
    IntFunc;

    private static Map<SymbolType, String> symbolTypeNameMap = new HashMap<>();

    SymbolType() {

    }

    /*@Override
    public String toString() {
        if (symbolTypeNameMap.containsKey(this))
            return symbolTypeNameMap.get(this);

        return null;
    }*/

    public static void main(String[] args) {
        System.out.println(SymbolType.Char);
    }
}
