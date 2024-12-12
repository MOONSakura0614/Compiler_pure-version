package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 变量初值（普通/一维数组）
 * InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
 */
public class InitVal extends SyntaxNode {
    private Exp exp; // 不管是不是数组，至少有一个Exp
    private Token left_brace_token;
    private ArrayList<Comma_Exp> comma_exp_list;
    private Token right_brace_token;
    private Token string_const_token;
    private Boolean isArrayInit;
    private Boolean isStringInit;

    public InitVal() {
        super("InitVal");
        comma_exp_list = new ArrayList<>(); // List要初始化
        isArrayInit = Boolean.FALSE;
        isStringInit = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        if (isStringConst()) {
            isStringInit = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) {
                string_const_token = lexIterator.iterator().next();
            }
            return;
        }
        if (isArrayInit()) {
            isArrayInit = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) { // 肯定有左大括号
                left_brace_token = lexIterator.iterator().next();
            }
            // 至少有一个Exp
            if (isExp()) {
                exp = new Exp();
                exp.unitParser();
            } else {
                lexIterator.retract();
                throw new RuntimeException("InitVal解析错误: Exp不能识别");
            }
            Token token;
            Exp exp1;
            Comma_Exp comma_exp;
            while (isComma()) {
                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.iterator().next();
                    if (isExp()) {
                        exp1 = new Exp();
                        exp1.unitParser();
                        comma_exp = new Comma_Exp(token, exp1);
                        comma_exp_list.add(comma_exp);
                    } else {
                        throw new RuntimeException("InitVal解析错误: Exp不能识别");
                    }
                } else {
                    lexIterator.retract();
                    throw new RuntimeException("InitVal解析错误: Comma不能识别");
                }
            }
            // 右括号
            if (lexIterator.iterator().hasNext()) { // 肯定有左大括号
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.RBRACE) {
                    right_brace_token = token;
                } else {
                    // 缺少右大括号：不算语法分析的错误
                    lexIterator.retract(); // 多读一个token
                    throw new RuntimeException("InitVal解析错误: }不能识别");
                }
            }
            return;
        }
        if (isExp()) {
            exp = new Exp();
            exp.unitParser();
        }
    }

    @Override
    public void print() {
        if (isStringInit) { // 初始化就false了，不用担心null
            if (string_const_token != null)
                IOUtils.writeCorrectLine(string_const_token.toString());
        } else if (isArrayInit) {
            if (left_brace_token != null) {
                IOUtils.writeCorrectLine(left_brace_token.toString());
            }
            if (exp != null) {
                exp.print();
            }
            if (!comma_exp_list.isEmpty()) {
                for (Comma_Exp comma_exp: comma_exp_list) {
                    comma_exp.print();
                }
            }
            if (right_brace_token != null)
                IOUtils.writeCorrectLine(right_brace_token.toString());
        } else { // 普通的只有一个Exp
            if (exp != null) {
                exp.print();
            }
        }

        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        // 检查Exp中的变量引用
        if (isArrayInit) {
            for (Comma_Exp comma_exp: comma_exp_list) {
                if (comma_exp.exp != null)
                    comma_exp.exp.visit();
            }
        } else if (!isStringInit) {
            if (exp != null) {
                exp.visit();
            }
        }
    }

    public static class Comma_Exp { // --> 变成类变量之后，供Stmt的printf情况解析中的占位符对应实值使用~
        Token comma_token;
        Exp exp;

        public Comma_Exp(Token token, Exp exp) {
            comma_token = token;
            this.exp = exp;
        }

        public void print() {
            if (comma_token != null) {
                IOUtils.writeCorrectLine(comma_token.toString());
            }
            if (exp != null) {
                exp.print();
            }
        }
    }

    public int getIntValue() {
        // 注意数组初始化的时候，int: 0-padding; char: '\0'-padding（应该也就是ascii的0）
        if (isArrayInit) {
            // TODO: 2024/11/25 数组初始化
            return 0;
        } else if (isStringInit) {
            // todo: stringConst只在char数组初始化 以及 printf的格式串中出现
            return 0;
        } else {
            if (exp != null)
                return exp.getIntValue();

            return 0;
        }
    }

    public Exp getExp() {
        return exp;
    }

    public Boolean getArrayInit() {
        return isArrayInit;
    }

    public Boolean getStringInit() {
        return isStringInit;
    }

    public ArrayList<Exp> getInitExps() {
        ArrayList<Exp> exps = new ArrayList<>();
        exps.add(exp);
        for (Comma_Exp comma_exp: comma_exp_list) {
            exps.add(comma_exp.exp);
        }
        return exps;
    }

    public int[] getArrayValue(int len) {
        int[] res = new int[len]; // 其他应该默认为0
        ArrayList<Exp> exps = getInitExps(); // 调用这个方法的只有能求出初始化值的！（比如局部常量数组，或者全局
        int i;
        for (i = 0; i < exps.size(); i++) {
            res[i] = exps.get(i).getIntValue(); // todo: 数组取值完善【仅限常量数组 --> 变量数组还是通过GEP
        }
        return res;
    }

    public int[] getArrayCharValue(int len) {
        int[] res = new int[len]; // 其他应该默认为0
        if (isStringInit) {
            // todo:StringCon待实现
        }
        ArrayList<Exp> exps = getInitExps(); // 调用这个方法的只有能求出初始化值的！（比如局部常量数组，或者全局
        int i;
        for (i = 0; i < exps.size(); i++) {
            res[i] = (exps.get(i).getIntValue() % 128);
        }
        return res;
    }
}
