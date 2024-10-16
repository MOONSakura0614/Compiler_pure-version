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
 * @Description: 具有左值的
 * LVal → Ident ['[' Exp ']']
 */
public class LVal extends SyntaxNode {
    private Token ident_token;
    private Boolean isArrayElement;
    private Token lBracket_token;
    private Exp exp;
    private Token rBracket_token;

    public LVal() {
        super("LVal");
        isArrayElement = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        if (isIdent()) {
            ident_token = lexIterator.iterator().next();

            if (lexIterator.iterator().hasNext()) {
                if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LBRACK) {
                    isArrayElement = Boolean.TRUE;
                    lBracket_token = lexIterator.iterator().next();

                    exp = new Exp();
                    exp.unitParser();

                    if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.RBRACK) {
                        rBracket_token = lexIterator.iterator().next();
                    } else {
                        CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRBRACK);
                        IOUtils.compileErrors.add(error);
                        Parser.isSyntaxCorrect = Boolean.FALSE;
                    }
                }
            }
        } else {
            throw new RuntimeException("LVal解析失败：无法识别Ident");
        }
    }

    @Override
    public void print() {
        if (ident_token != null)
            IOUtils.writeCorrectLine(ident_token.toString());
        if (isArrayElement) {
            if (lBracket_token != null) {
                IOUtils.writeCorrectLine(lBracket_token.toString());
            }
            if (exp != null) {
                exp.print();
            }
            if (rBracket_token != null) {
                IOUtils.writeCorrectLine(rBracket_token.toString());
            }
        }
    }
}
