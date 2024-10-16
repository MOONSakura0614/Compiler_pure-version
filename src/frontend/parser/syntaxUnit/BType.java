package frontend.parser.syntaxUnit;


import frontend.lexer.LexType;
import frontend.lexer.Token;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 两种数据类型-int和char
 * BType → 'int' | 'char'
 */
public class BType extends SyntaxNode {
    // 推导规则的右式由终结符组成
    Token basic_type_token;
//    Token char_token;
    Boolean isInt;

    public BType() {
        super("BType");
//        isInt = Boolean.TRUE; // 非BType就是null
    }

    @Override
    public void unitParser() {
        Token token;
        // 只有右式涉及终结符的，才会改变curPos，读进新的Token
        if (lexIterator.iterator().hasNext()) {
            token = lexIterator.iterator().next();
            if (token.getTokenType() == LexType.INTTK) {
                isInt = Boolean.TRUE;
            } else if (token.getTokenType() == LexType.CHARTK) {
                isInt = Boolean.FALSE;
            } else {
                throw new RuntimeException("解析BType出错: 此Token不是BType\n" + "Error Token: " + token.toString());
            }
        } else {
            throw new RuntimeException("没有剩余token可供解析");
        }
    }

    @Override
    public void print() {
        // BType无需输出——>只需输出Token信息
        IOUtils.writeCorrectLine(basic_type_token.toString());
    }
}
