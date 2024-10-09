package frontend;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 郑悦
 * @Description: 单词拆分并判断类型
 * @date 2024/9/24 9:18
 */
public class Token {
    private String tokenValue;
    private LexType lexType;
    private int lineNum;
    private static Map<String, LexType> reserveWords = new HashMap<>();

    static {
        /*保留字*/
        reserveWords.put("main", LexType.MAINTK);
        reserveWords.put("const", LexType.CONSTTK);
        reserveWords.put("int", LexType.INTTK);
        reserveWords.put("char", LexType.CHARTK);
        reserveWords.put("break", LexType.BREAKTK);
        reserveWords.put("continue", LexType.CONTINUETK);
        reserveWords.put("if", LexType.IFTK);
        reserveWords.put("else", LexType.ELSETK);
        reserveWords.put("!", LexType.NOT);
        reserveWords.put("&&", LexType.AND);
        reserveWords.put("||", LexType.OR);
        reserveWords.put("for", LexType.FORTK);
        reserveWords.put("getint", LexType.GETINTTK);
        reserveWords.put("getchar", LexType.GETCHARTK);
        reserveWords.put("printf", LexType.PRINTFTK);
        reserveWords.put("return", LexType.RETURNTK);
        reserveWords.put("+", LexType.PLUS);
        reserveWords.put("-", LexType.MINU);
        reserveWords.put("void", LexType.VOIDTK);
        reserveWords.put("*", LexType.MULT);
        reserveWords.put("/", LexType.DIV);
        reserveWords.put("%", LexType.MOD);
        reserveWords.put("<", LexType.LSS);
        reserveWords.put("<=", LexType.LEQ);
        reserveWords.put(">", LexType.GRE);
        reserveWords.put(">=", LexType.GEQ);
        reserveWords.put("==", LexType.EQL);
        reserveWords.put("!=", LexType.NEQ);
        reserveWords.put("=", LexType.ASSIGN);
        reserveWords.put(";", LexType.SEMICN);
        reserveWords.put(",", LexType.COMMA);
        reserveWords.put("(", LexType.LPARENT);
        reserveWords.put(")", LexType.RPARENT);
        reserveWords.put("[", LexType.LBRACK);
        reserveWords.put("]", LexType.RBRACK);
        reserveWords.put("{", LexType.LBRACE);
        reserveWords.put("}", LexType.RBRACE);

        /*传回其他的TokenType*/
        reserveWords.put("Ident", LexType.IDENFR);
        reserveWords.put("IntConst", LexType.INTCON);
        reserveWords.put("StringConst", LexType.STRCON);
        reserveWords.put("CharConst", LexType.CHRCON);
    }

    public Token(String tokenValue, LexType lexType, int lineNum) {
        this.tokenValue = tokenValue;
        this.lexType = lexType;
        this.lineNum = lineNum;
    }

    public Token() {}

    public LexType getTokenType() {
        return lexType;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public static LexType reverser(String word) {
        return reserveWords.get(word);
    }

    public static boolean isReversedContained(String word) {
        return reserveWords.containsKey(word);
    }
}
