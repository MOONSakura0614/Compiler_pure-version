package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 常量初值
 * ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
 */
public class ConstInitVal extends SyntaxNode {
    private ConstExp constExp; // 不管是不是数组，至少有一个Exp
    private Token left_brace_token;
    private ArrayList<Comma_ConstExp> comma_constExp_list;
    private Token right_brace_token;
    private Token string_const_token;
    private Boolean isArrayInit;
    private Boolean isStringInit;

    public ConstInitVal() {
        super("ConstInitVal");
        isArrayInit = Boolean.FALSE;
        isStringInit = Boolean.FALSE;
        comma_constExp_list = new ArrayList<>();
    }

    /*注意上述推导式子中的StringConst，字符串成常量（或解读为char[]一维数组常量，是终结符形式！不是要解析的语法成分？*/

    @Override
    public void unitParser() {
        if (isStringConst()) {
            isStringInit = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) {
                string_const_token = lexIterator.iterator().next();
            }
        } else if (isArrayInit()) { // 数组初始化
            isArrayInit = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) { // 肯定有左大括号
                left_brace_token = lexIterator.iterator().next();
            }
            // 注意不一定要有Exp，可以是空数组初始化[Exp {, Exp}]
            if (isExp()) {
                constExp = new ConstExp();
                constExp.unitParser();
            }
            Token token;
            ConstExp constExp1;
            Comma_ConstExp comma_constExp;
            while (isComma()) {
                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.iterator().next();
                    if (isConstExp()) {
                        constExp1 = new ConstExp();
                        constExp1.unitParser();
                        comma_constExp = new Comma_ConstExp(token, constExp1);
                        comma_constExp_list.add(comma_constExp);
                    } else {
                        throw new RuntimeException("InitVal解析错误: constExp不能识别");
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
                    // 缺少右大括号：不算语法分析的错误【缺少分号，右小括号，右中括号
                    lexIterator.retract(); // 多读一个token
                    throw new RuntimeException("InitVal解析错误: }不能识别");
                }
            }
        } else if (isConstExp()) {
            constExp = new ConstExp();
            constExp.unitParser();
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
            if (constExp != null) {
                constExp.print();
            }
            if (!comma_constExp_list.isEmpty()) {
                for (Comma_ConstExp comma_constExp: comma_constExp_list) {
                    comma_constExp.print();
                }
            }
            if (right_brace_token != null) {
                IOUtils.writeCorrectLine(right_brace_token.toString());
            }
        } else { // 普通的只有一个constExp
            if (constExp != null) {
                constExp.print();
            }
        }
        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        // 检查Exp中的变量引用
        if (isArrayInit) {
            for (Comma_ConstExp comma_constExp: comma_constExp_list) {
                if (comma_constExp.constExp != null)
                    comma_constExp.constExp.visit();
            }
        } else if (!isStringInit) {
            if (constExp != null) {
                constExp.visit();
            }
        }
    }

    public class Comma_ConstExp {
        Token comma_token;
        ConstExp constExp;

        public Comma_ConstExp(Token token, ConstExp constExp) {
            comma_token = token;
            this.constExp = constExp;
        }

        public void print() {
            if (comma_token != null)
                IOUtils.writeCorrectLine(comma_token.toString());
            if (constExp != null)
                constExp.print();
        }
    }
}
