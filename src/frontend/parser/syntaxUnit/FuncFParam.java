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
 * @Description: 函数形参
 * FuncFParam → BType Ident ['[' ']']
 */
public class FuncFParam extends SyntaxNode {
    private BType bType;
    private Token ident_token;
    private Boolean isArray;
    private Token lBracket_token;
    private Token rBracket_token;

    public FuncFParam() {
        super("FuncFParam");
        isArray = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        if (isBType()) {
            bType = new BType();
            bType.unitParser();
        }
        if (isIdent()) {
            if (lexIterator.iterator().hasNext()) {
                ident_token = lexIterator.iterator().next();
                if (isArray()) {
                    isArray = Boolean.TRUE;
                    if (lexIterator.iterator().hasNext()) {
                        lBracket_token = lexIterator.iterator().next();
                        Token token;
                        if (lexIterator.iterator().hasNext()) {
                            token = lexIterator.tokenList.get(lexIterator.curPos);
                            if (token.getTokenType() == LexType.RBRACK) {
                                rBracket_token = lexIterator.iterator().next();
                            } else {
                                CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRBRACK);
                                IOUtils.compileErrors.add(error);
                                Parser.isSyntaxCorrect = Boolean.FALSE;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void print() {
        if (bType != null)
            bType.print();
        if (ident_token != null) {
            IOUtils.writeCorrectLine(ident_token.toString());
        }
        if (isArray) {
            if (lBracket_token != null)
                IOUtils.writeCorrectLine(lBracket_token.toString());
            if (rBracket_token != null)
                IOUtils.writeCorrectLine(rBracket_token.toString());
        }

        IOUtils.writeCorrectLine(toString());
    }
}
