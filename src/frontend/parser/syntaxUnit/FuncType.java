package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import frontend.parser.Parser;

/**
 * @author 郑悦
 * @Description: 函数类型（三种）
 *  FuncType → 'void' | 'int' | 'char'
 */
public class FuncType extends SyntaxNode {
    Token int_token;
    Token char_token;
    Token void_token;

    public FuncType() {
        super("FuncType");
    }

    @Override
    public void unitParser() {
        Token token = Parser.lexIterator.nowToken();
        switch (token.getTokenType()) {
            case INTTK -> {
                int_token = token;
                break;
            }
            case CHARTK -> {
                char_token = token;
                break;
            }
            case VOIDTK -> {
                void_token = token;
                break;
            }
        }
    }

    @Override
    public void print() {

    }
}
