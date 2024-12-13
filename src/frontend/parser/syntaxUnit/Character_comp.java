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
//        return character_token.getTokenValue().charAt(1);
        String value = character_token.getTokenValue();
        // 去掉前后的单引号
        String charContent = value.substring(1, value.length() - 1);

        // 如果是转义字符
        if (charContent.startsWith("\\")) {
            if (charContent.length() == 2) {  // 普通转义字符
                switch (charContent.charAt(1)) {
                    case 'a': return 7;   // \a (bell)
                    case 'b': return 8;   // \b (backspace)
                    case 't': return 9;   // \t (tab)
                    case 'n': return 10;  // \n (newline)
                    case 'v': return 11;  // \v (vertical tab)
                    case 'f': return 12;  // \f (form feed)
                    case 'r': return 13;  // \r (carriage return)
                    case '\\': return 92; // \\ (backslash)
                    case '\'': return 39; // \' (single quote)
                    case '\"': return 34; // \" (double quote)
                    case '0': return 0;   // \0 (null character)
                    default: return charContent.charAt(1);
                }
            }
        }
        // 普通字符
        return charContent.charAt(0);
    }
}
