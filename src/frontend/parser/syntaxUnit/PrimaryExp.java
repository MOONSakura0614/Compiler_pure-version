package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.symbol.Symbol;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description:
 * PrimaryExp → '(' Exp ')' | LVal | Number | Character
 */
public class PrimaryExp extends SyntaxNode {
    // 检查之后感觉没有什么有害规则：不多余，均是识别符号到终结符中的一环；也没发现左递归
    private Token leftParent_token;
    private Exp exp;
    private Token rightParent_token;
    private Boolean isParent; // 是括号包住Exp的组合
    private LVal lVal;
    private Boolean isLVal;
    private Number_comp number;
    private Boolean isNumber;
    private Character_comp character;
    private Boolean isCharacter;

    public PrimaryExp() {
        super("PrimaryExp");
        isParent = Boolean.FALSE;
        isLVal = Boolean.FALSE;
        isNumber = Boolean.FALSE;
        isCharacter = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        if (isLVal()) {
            isLVal = Boolean.TRUE;
            lVal = new LVal();
            lVal.unitParser();
        } else if (isNumber()) {
            isNumber = Boolean.TRUE;
            number = new Number_comp();
            number.unitParser();
        } else if (isCharacter()) {
            isCharacter = Boolean.TRUE;
            character = new Character_comp();
            character.unitParser();
        } else { // 剩下 (Exp) 的判断
            if (lexIterator.curPos < lexIterator.tokenCount) {
                if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LPARENT) {
                    isParent = Boolean.TRUE;

                    leftParent_token = lexIterator.iterator().next();
                    exp = new Exp();
                    exp.unitParser();
                    if (lexIterator.curPos < lexIterator.tokenCount) {
                        if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.RPARENT) {
                            rightParent_token = lexIterator.iterator().next();
                        } else {
                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                            IOUtils.compileErrors.add(error);
                            Parser.isSyntaxCorrect = Boolean.FALSE;
                        }
                    } else {
                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                        IOUtils.compileErrors.add(error);
                        Parser.isSyntaxCorrect = Boolean.FALSE;
                    }
                }
            }
        }
    }

    @Override
    public void print() {
        if (isNumber) {
            if (number != null)
                number.print();
        } else if (isCharacter) {
            if (character != null)
                character.print();
        } else if (isLVal) {
            if (lVal != null)
                lVal.print();
        } else if (isParent) {
            if (leftParent_token != null)
                IOUtils.writeCorrectLine(leftParent_token.toString());
            if (exp != null)
                exp.print();
            if (rightParent_token != null)
                IOUtils.writeCorrectLine(rightParent_token.toString());
        }

        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        if (isNumber || isCharacter)
            return ;
        else if (isParent && exp != null) {
            exp.visit();
        }
        else if (isLVal && lVal != null) {
            lVal.visit();
        }
    }

    public boolean isArrayElement() {
        if (isNumber || isCharacter)
            return false;
        if (isParent) {
            return exp != null && exp.isArrayElement();
        }
        if (isLVal) {
            return lVal != null && lVal.getIsArrayElement();
        }

        return false;
    }

    public Symbol getIdentSymbol() {
        if (isParent) {
            return exp == null ? null: exp.getIdentSymbol();
        }
        if (isLVal) {
            return lVal == null ? null: lVal.getIdentSymbol();
        }

        return null;
    }

    public boolean isIdentArray() {
        if (isNumber || isCharacter)
            return false;
        if (isParent) {
            return exp != null && exp.isIdentArray();
        }
        if (isLVal) {
            if (lVal != null && lVal.getIsArrayElement())
                return false; // 是数组元素，不是数组
            return lVal != null && lVal.isIdentArray();
        }

        return false;
    }

    public int getIntValue() {
        if (isNumber) {
            if (number != null)
                return number.getIntValue();

            return 0;
        } else if (isLVal) {
            // 得到的是左值，比如某个变量名目前存的值
            if (lVal != null)
                return lVal.getIntValue();

            return 0;
        } else if (isCharacter) {
            // 这里是否需要强转，ascii码to数字
            if (character != null)
                if (character.getCharacter_token() != null && !character.getCharacter_token().getTokenValue().isEmpty())
                    return character.getCharacter_token().getTokenValue().charAt(0);

            return 0;
        } else {
            // 推导右式是(Exp)
            if (exp != null)
                return exp.getIntValue();

            return 0;
        }
    }

    public Exp getExp() {
        return exp;
    }

    public Boolean getIsLVal() {
        return isLVal;
    }

    public LVal getlVal() {
        return lVal;
    }

    public Boolean getIsParent() { // 就是Exp
        return isParent;
    }

    public Boolean getIsNumber() {
        return isNumber;
    }

    public Number_comp getNumber() {
        return number;
    }

    public Boolean getIsCharacter() {
        return isCharacter;
    }

    public Character_comp getCharacter() {
        return character;
    }
}
