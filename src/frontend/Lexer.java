package frontend;

import errors.CompileError;
import errors.ErrorType;
import utils.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 词法分析器类（单例模式）
 * @date 2024/9/20 17:34
 */
public class Lexer {
    private String source;
    /*读取的字符串要原样保留着便于输出，特别是数字，
    这里输出的并不是真正的单词值，其实是读入的字符串，
    单词值需另行记录。 ——说明无需特地转化后的*/
    private String word = "";
    /*单词代表的真实值含义——供后面阶段使用*/
    /*Value value;(int/char/array)*/
    private String typeCode;
    private Token token;
    private char currentSym;
    private int lineNum = 1; // 当前行号
    private final int codeLength;
    private int number;
    private int curPos = 0; // 当前处理的字符数（在testfile的转成长String中的位置）
    private ArrayList<Token> tokenList;
    private ArrayList<CompileError> errorList;
    private boolean isLexicalCorrect;
    private LexType lexType = LexType.IDENFR;

    private Lexer() {
        tokenList = new ArrayList<>();
        errorList = new ArrayList<>();
        isLexicalCorrect = true; /*默认无词法错误*/
        try {
            source = IOUtils.read("testfile.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        codeLength = source.length();
        if (codeLength > 0) {
            currentSym = source.charAt(curPos);
        }
    }

    private static Lexer lexer;

    public static Lexer getInstance() {
        if (lexer == null)
            lexer = new Lexer();
        return lexer;
    }

    public void catToken() {
        word += currentSym;
    }

    public void lexicalAnalysis() {
        if (codeLength == 0) {
            return;
        }
        getChar();
        while (curPos < codeLength) {
            next(); // 在next中retract：curP--所以下面再getChar
            getChar();
        }

        printLexicalResult();
    }

    private void printLexicalResult() {
        if (isLexicalCorrect) {
            // 输出tokenList
            try {
                FileWriter fileWriter = new FileWriter("lexer.txt");
                for (Token token: tokenList) {
                    fileWriter.write(token.getTokenType().toString() + ' ' + token.getTokenValue()+'\n');
                }
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                FileWriter fileWriter = new FileWriter("error.txt");
                for (CompileError error: errorList) {
                    fileWriter.write(Integer.toString(error.getLineNum()) + ' ' + error.getErrorType().getErrorTypeCode().toString() + '\n');
//                    System.out.println(error.getLineNum() + ' ' + error.getErrorType().getErrorTypeCode().toString() + '\n');
//                    System.out.println(error.getLineNum());
                }
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @Description 处理下一个单词
     * @return null
     * @author 郑悦
     * @date 2024/9/20 17:37
    **/
    public boolean next() {
        // 获取下一个字符
        clearToken();
        while (isSpace()) { // 换行单独判断
//            getChar(); // 跳过空白符
            if (curPos < codeLength) // 进getchar之后还要curP++
                getChar();
            else
                return true;
        }
        while (isNewLine()) {
            lineNum++;
            if (curPos < codeLength)
                getChar();
            else
                return true; // 到文件末尾
        }

        /* 字母或下划线 */
        if(isUnderline() || isLetter()) { // 标识符或保留字
//            token += c;
//            while ((isUnderline() || isLetter() || isDigit()) && curPos < codeLength) { // 这样是不是吞掉了一个curSym
            while (isUnderline() || isLetter() || isDigit()) {
                catToken();
                if (curPos < codeLength)
                    getChar();
                else
                    break; // 也有可能遇到retract？
            }
            if (!(isUnderline() || isLetter() || isDigit()))
                retract();
            lexType = reserve(); // 查关键字表
//            return true;
        } else if (isDigit()) {
            while (isDigit()) {
                catToken();
                if (curPos < codeLength) {
                    getChar();
                } else {
                    break;
                }
            }
            if (!isDigit())
                retract(); // 跳出循环后要回退
            lexType = LexType.INTCON;
//            number = Integer.parseInt(word); // 最大的无符号正数int转换不了？
        } else if (isDivi()) { // == ‘/’
            catToken();
            if (curPos < codeLength) {
                getChar();
                if (currentSym == '/') {
                    // 循环直到注释结束
                    while (curPos < codeLength) {
                        getChar();
                        if (currentSym == '\n') {
                            lineNum++;
                            break;
                        }
                    }
                    return true; // 但是不加tokenList，直接跳过
                } else if (currentSym == '*') {
                    while (curPos < codeLength) { // 进入循环首次为*
                        getChar(); // 得到下一个字符 // 在下面状态机的循环里面已经getChar了
                        if (currentSym == '\n') {
                            lineNum++;
                        }
                        while (curPos < codeLength && currentSym != '*') {
                            getChar();
                            if (currentSym == '\n') {
                                lineNum++;
                            }
                        }
                        while (curPos < codeLength && currentSym == '*') {
                            getChar();
                            // 这边如果是\n是不是打破循环之后没有newline++
                        }
//                        if (curPos < codeLength && currentSym == '/') { // 是不是不用签名的条件
                        if (currentSym == '/') {
                            // 结束注释
                            break;
                        }
                        if (currentSym == '\n') {
                            lineNum++;
                        }
                    }
                    return true; // 注释部分不加tokenList，直接跳过
                } else {
                    retract(); // 后退一个字符
                    lexType = reserve(); // 就是DIV
                }
            }
        } else if (isDoubleQuote()) {
            catToken();
            while (curPos < codeLength) {
                getChar();
                catToken();
                if (currentSym == '"') {
                    break;
                }
            }
            lexType = LexType.STRCON;
        } else if (isSingleQuote()) {
            catToken();
            while (curPos < codeLength) {
                getChar();
                catToken();
                if (currentSym == '\\') { // 转义符号引发的问题
                    if (curPos >= codeLength)
                        break;
                    else {
                        getChar();
                        catToken();
                        if (currentSym == '\'') { // 保证词法除了a错其他不错？
//                            if (curPos )
                            continue;
                        }
                    }
                }
                if (currentSym == '\'') { // 转义符号引发的问题
//                    catToken();
                    if (!word.isEmpty() && word.charAt(word.length() - 1) == '\\') {
                        continue; // 这个‘是转义，还有下一个’
                    }
                    else {
                        break;
                    }
                }
            }
            lexType = LexType.CHRCON;
        } else if (currentSym == '&') {
            catToken();
            if (curPos < codeLength) {
                getChar();
                if (currentSym == '&') {
                    catToken();
                } else {
                    isLexicalCorrect = false;
                    retract();
                    CompileError error = new CompileError(lineNum, ErrorType.IllegalSymbol);
                    errorList.add(error);
                }
            } else {
                isLexicalCorrect = false;
//                retract();
                CompileError error = new CompileError(lineNum, ErrorType.IllegalSymbol);
                errorList.add(error);
            }
            lexType = LexType.AND;
        } else if (currentSym == '|') {
            catToken();
            if (curPos < codeLength) {
                getChar();
                if (currentSym == '|') {
                    catToken();
                } else {
                    isLexicalCorrect = false;
                    retract();
                    CompileError error = new CompileError(lineNum, ErrorType.IllegalSymbol);
                    errorList.add(error);
                    return false;
                }
            } else {
                isLexicalCorrect = false;
//                retract();
                CompileError error = new CompileError(lineNum, ErrorType.IllegalSymbol);
                errorList.add(error);
                return false;
            }
            lexType = LexType.AND;
        } else if (currentSym == '!') {
            catToken();
            if (curPos < codeLength) {
                getChar();
                if (currentSym == '=') {
                    catToken();
                    lexType = LexType.NEQ;
                } else {
                    retract();
                    lexType = LexType.NOT;
                }
            } else {
                lexType = LexType.NOT;
            }
        } else if (isEqu()) {
            catToken();
            if (curPos < codeLength) {
                getChar();
                if (isEqu()) {
                    catToken();
                    lexType = LexType.EQL;
                } else {
                    retract();
                    lexType = LexType.ASSIGN;
                }
            } else {
                lexType = LexType.ASSIGN;
            }
        } else if (currentSym == '<') {
            catToken();
            if (curPos < codeLength) {
                getChar();
                if (isEqu()) {
                    catToken();
//                    lexType = reserve();
                } else {
                    retract();
                }
            }
            lexType = reserve();
        } else if (currentSym == '>') {
            catToken();
            if (curPos < codeLength) {
                getChar();
                if (isEqu()) {
                    catToken();
//                    lexType = reserve();
                } else {
                    retract();
                }
            }
            lexType = reserve();
        } else if (currentSym == '%') {
            catToken();
        } else if (isStar()) {
            catToken();
        } else if (isMinus()) {
            catToken();
        } else if (isPlus()) {
            catToken();
        } else if (currentSym == ';') {
            catToken();
        } else if (currentSym == ',') {
            catToken();
        } else if (currentSym == '(') {
            catToken();
        } else if (currentSym == ')') {
            catToken();
        } else if (currentSym == '[') {
            catToken();
        } else if (currentSym == ']') {
            catToken();
        } else if (currentSym == '{') {
            catToken();
        } else if (currentSym == '}') {
            catToken();
        }
        if (Token.isReversedContained(word)) {
            lexType = reserve();
        }
        if (!word.isEmpty()) {
            Token token = new Token(word, lexType, lineNum);
            tokenList.add(token);
        }

        return true;
    }

    public void clearToken() {
        word = "";
    }

    private void retract() {
        // 万一是换行？
        if (curPos != 0) {
            curPos--;
        }
        /*else {
            lineNum--;
//            curPos = lines.get(lineNum).length();
        }*/
    }

    public LexType reserve() {
        if (!Token.isReversedContained(word)) {
            return LexType.IDENFR;
        } else {
            return Token.reverser(word);
        }
    }

    public Token getSym() {
        clearToken();
        while (isSpace()) getChar();
        return new Token();
    }

    public void getChar() {
        if (curPos >= codeLength) { // 字符串越界
            return;
        }
        currentSym = source.charAt(curPos);
        curPos++;
    }

    /**
     * @Description  获取读取的单词值
     * @return java.lang.String
     * @author 郑悦
     * @date 2024/9/20 17:39
    **/
    public String getToken() {
        return "";
    }

    public LexType getLexType() {
        return null;
    }
    public LexType getTokenType() {
        return token.getTokenType();
    }

    // 判断函数
    public boolean isDoubleQuote() {
        return currentSym == '"';
    }

    public boolean isSingleQuote() {
        return currentSym == '\'';
    }

    public boolean isUnderline() {
        return currentSym == '_';
    }

    public boolean isSpace() {
        return currentSym == ' ' || currentSym == '\t' ||
                currentSym == '\r' || currentSym == '\f';
    }

    public boolean isNewLine() {
        return currentSym == '\n';
    }

    public boolean isTab() {
        return currentSym == '\t';
    }
    // 识别字母
    public boolean isLetter() {
        return Character.isLetter(currentSym);
    }

    // 识别数字
    public boolean isDigit() {
        return Character.isDigit(currentSym);
    }

    // 判断空白字符
    public boolean isSpace_2() {
        return Character.isSpaceChar(currentSym);
    }

    // 其他符号判断函数
    public boolean isPlus() { return currentSym == '+'; }
    public boolean isMinus() { return currentSym == '-'; }
    public boolean isStar() { return currentSym == '*'; }
    public boolean isDivi() { return currentSym == '/'; }
    public boolean isLpar() { return currentSym == '('; }
    public boolean isRpar() { return currentSym == ')'; }
    public boolean isComma() { return currentSym == ','; }
    public boolean isSemi() { return currentSym == ';'; }
    public boolean isColon() { return currentSym == ':'; }
    public boolean isEqu() { return currentSym == '='; }

    public static void main(String[] args) {
        //
    }
}
