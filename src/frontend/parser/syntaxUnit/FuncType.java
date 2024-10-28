package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 函数类型（三种）
 *  FuncType → 'void' | 'int' | 'char'
 */
public class FuncType extends SyntaxNode {
    private Token funcType_token;

    public FuncType() {
        super("FuncType");
    }

    @Override
    public void unitParser() {
        if (isFuncType()) {
            funcType_token = lexIterator.iterator().next();
        }
    }

    @Override
    public void print() {
        if (funcType_token != null) {
            IOUtils.writeCorrectLine(funcType_token.toString());
        }

        IOUtils.writeCorrectLine(toString());
    }

    public LexType getFuncType() {
        if (funcType_token == null)
            return null;
        return funcType_token.getTokenType();
    }
}
