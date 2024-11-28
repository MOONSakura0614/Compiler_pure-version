package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 字符（注意推导式右边已经是终结符，不用单独创类
 *  Character → CharConst
 */
public class Character_comp extends SyntaxNode {
    private Token character_token;

    public Character_comp() {
        super("Character");
    }

    @Override
    public void unitParser() {
        if (isCharacter()) {
            if (lexIterator.iterator().hasNext()) {
                character_token = lexIterator.iterator().next();
            }
        }
    }

    @Override
    public void print() {
        if (character_token != null)
            IOUtils.writeCorrectLine(character_token.toString());
        IOUtils.writeCorrectLine(toString());
    }

    public Token getCharacter_token() {
        return character_token;
    }

    public int getIntValue() {
        return character_token.getTokenValue().charAt(0);
    }
}
