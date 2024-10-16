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
}
