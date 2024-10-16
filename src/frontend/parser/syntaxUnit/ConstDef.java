package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;
// TODO: 2024/10/16 准备将Error迁移 

/**
 * @author 郑悦
 * @Description: 常量定义
 * ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
 */
public class ConstDef extends SyntaxNode {
    private Token ident_token;
    private Token left_bracket_token;
    private ConstExp constExp;
    private Token right_bracket_token;
    private Token assign_token;
    private ConstInitVal constInitVal;
    private Boolean isArray;
    // const常量，必定被赋初值
    /*private Boolean isAssigned;*/

    public ConstDef() {
        super("ConstDef");
        isArray = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        Token token;
        if (lexIterator.iterator().hasNext()) {
            token = lexIterator.iterator().next();
            lineNum_begin = token.getLineNum();
            if (token.getTokenType() == LexType.IDENFR) {
                ident_token = token;
            } else {
                lexIterator.retract(); // 需要回退一个curPos用于其他解析吗
                throw new RuntimeException("ConstDef解析出错：Ident标识符无法解析\n此token实际为："+token);
            }
        }
        if (isArray()) {
            // 注意是不是[]中括号只会在数组中出现
            isArray = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.LBRACK) {
                    left_bracket_token = token;
                } else {
                    throw new RuntimeException("ConstDef解析出错：[无法解析\n此token实际为："+token);
                }
            }
            // 数组为定长，下面解析ConstExp
            constExp = new ConstExp();
            constExp.unitParser();
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.RBRACK) {
                    right_bracket_token = token;
                } else {
                    lexIterator.retract();
                    // 缺少右中括号，是已知错误
                    CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRBRACK);
                    IOUtils.compileErrors.add(error);
                    Parser.isSyntaxCorrect = Boolean.FALSE;
//                    throw new RuntimeException("VarDef解析出错：]无法解析\n此token实际为："+token);
                }
            }
        }
        if (isAssign()) {
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.ASSIGN) {
                    assign_token = token;
                } else {
                    throw new RuntimeException("ConstDef解析出错：=无法解析\n此token实际为："+token);
                }
                // 必须有constInitVal
                constInitVal = new ConstInitVal();
                constInitVal.unitParser();
            }
        } else {
            throw new RuntimeException("ConstDef解析出错：=无法解析");
        }
    }

    @Override
    public void print() {
        IOUtils.writeCorrectLine(ident_token.toString());
        if (isArray != null) {
            if (isArray) {
                if (left_bracket_token != null)
                    IOUtils.writeCorrectLine(left_bracket_token.toString());
                if (constExp != null)
                    constExp.print();
                if (right_bracket_token != null)
                    IOUtils.writeCorrectLine(right_bracket_token.toString());
            }
        }
        if (assign_token != null)
            IOUtils.writeCorrectLine(ident_token.toString());
        if (constInitVal != null)
            constInitVal.print(); // 如果是null要不要抛RE异常？
        IOUtils.writeCorrectLine(toString());
    }
}
