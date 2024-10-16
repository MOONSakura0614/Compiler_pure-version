package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 弹幕运算符
 * UnaryOp → '+' | '−' | '!'
 *  注：'!'仅出现在条件表达式中
 */
public class UnaryOp extends SyntaxNode {
    private Token unaryOp_token;

    public UnaryOp() {
        super("UnaryOp");
    }

    @Override
    public void unitParser() {
        if (isUnaryOp()) {
            if (lexIterator.iterator().hasNext()) {
                unaryOp_token = lexIterator.iterator().next();
            }
        }
    }

    @Override
    public void print() {
        if (unaryOp_token != null)
            IOUtils.writeCorrectLine(unaryOp_token.toString());
        IOUtils.writeCorrectLine(toString());
    }
}
