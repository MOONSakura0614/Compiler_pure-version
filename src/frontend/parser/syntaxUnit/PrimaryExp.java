package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
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
}
