package llvm.value.instruction;

import frontend.lexer.LexType;
import frontend.lexer.Token;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 20:24
 */
public enum Operator {
    Add, Sub, Mul, Div, Mod, And, Or, // 二元运算
    Lt, Le, Ge, Gt, Eq, Ne, // 关系运算符
    Zext, Bitcast, Trunc, // 类型转换
    Alloca, Load, Store, GEP, // 内存操作
    Phi, MemPhi, LoadDep, // Phi指令
    Br, Call, Ret, // 跳转指令
    Not; // 非运算符-一元

    public static Operator getOperator(Token op_token) {
        if (op_token == null)
            return null;

        /*switch (op_token.getTokenType()) {
            case PLUS -> {
                return Add;
            }
            case MINU -> {
                return Sub;
            }
            case MULT -> {
                return Mul;
            }
            case DIV -> {
                return Div;
            }
        }*/
        if (opMap.containsKey(op_token.getTokenType())) {
            return opMap.get(op_token.getTokenType());
        }

        return null; // 类型转换等不在其中
    }

    public static final HashMap<LexType, Operator> opMap;
    static {
        opMap = new HashMap<>();
        opMap.put(LexType.PLUS, Add);
        opMap.put(LexType.MINU, Sub);
        opMap.put(LexType.MULT, Mul);
        opMap.put(LexType.DIV, Div);
        opMap.put(LexType.MOD, Mod);

        opMap.put(LexType.AND, And);
        opMap.put(LexType.OR, Or);

        opMap.put(LexType.LSS, Lt);
        opMap.put(LexType.LEQ, Le);
        opMap.put(LexType.GRE, Gt);
        opMap.put(LexType.GEQ, Ge);
        opMap.put(LexType.EQL, Eq);
        opMap.put(LexType.NEQ, Ne);

        opMap.put(LexType.NOT, Not);

    }
}
