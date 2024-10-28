package frontend.lexer;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author 郑悦
 * @Description: 用于语法分析的迭代器
 * @date 2024/10/11 23:46
 */
public class LexIterator implements Iterable<Token> {
    private Lexer lexer;
    public int tokenCount;
    public int curPos; // 把tokenList的index暴露给语法分析程序的判断方法使用？
    public ArrayList<Token> tokenList;
    private static LexIterator lexIterator;
    public Iterator<Token> tokenIterator;

    private LexIterator() {
        lexer = Lexer.getInstance();
        curPos = 0;
        tokenIterator = iterator();
    }

    public static LexIterator getInstance() {

        if (lexIterator == null) {
            // init
            lexIterator = new LexIterator();
            lexIterator.lexer.lexicalAnalysis(); // 获取TokenList

//            lexIterator.lexer.printLexicalResult();

            lexIterator.tokenList = lexIterator.lexer.getTokenList();
            lexIterator.tokenCount = lexIterator.tokenList.size();
        }

        return lexIterator;
    }


    public Token nowToken() {
        if (curPos != 0)
            return tokenList.get(curPos - 1);
        // 本函数是传递当前（已遍历过的最后一个被遍历到的）-刚刚遍历到的token
        return null; // 不穿tokenList[0]吧
    }

    public void retract() {
        curPos--;
        // 用于分析失败的时候？
    }

    @Override
    public Iterator<Token> iterator() {
        return new Iterator<Token>() {

            @Override
            public boolean hasNext() {
                return curPos < tokenCount;
            }

            @Override
            public Token next() {
                return tokenList.get(curPos++);
            }
        };
    }
}
