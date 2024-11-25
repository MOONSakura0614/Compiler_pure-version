package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 数值
 * Number → IntConst
 */
public class Number_comp extends SyntaxNode {
    private Token number_token;

    public Number_comp() {
        super("Number");
    }

    @Override
    public void unitParser() {
        if (isNumber()) {
            if (lexIterator.iterator().hasNext()) {
                number_token = lexIterator.iterator().next();
            }
        }
    }

    @Override
    public void print() {
        if (number_token != null)
            IOUtils.writeCorrectLine(number_token.toString());
        IOUtils.writeCorrectLine(toString());
    }

    public int getIntValue() {
        // 只支持整数：i32或i8
        if (number_token != null)
            return Integer.parseInt(number_token.getTokenValue());

        return 0;
    }
}
