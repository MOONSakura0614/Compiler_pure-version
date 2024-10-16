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
 * @Description: 函数定义
 * FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
 */
public class FuncDef extends SyntaxNode {
    private FuncType funcType;
    private Token ident_token;
    private Token lParent_token;
    private FuncFParams funcFParams;
    private Token rParent_token;
    private Block block;

    public FuncDef() {
        super("FuncDef");
    }

    @Override
    public void unitParser() {
        Token token;
        // 解析当前的，并给出进入下一个的解析窗口
        if (isFuncType()) {
            funcType = new FuncType();
            funcType.unitParser();

            if (isIdent()) {
                ident_token = lexIterator.iterator().next();

                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.LPARENT) {
                        lParent_token = lexIterator.iterator().next();

                        if (isFParams()) {
                            funcFParams = new FuncFParams();
                            funcFParams.unitParser();
                        }

                        if (lexIterator.iterator().hasNext()) {
                            token = lexIterator.tokenList.get(lexIterator.curPos);
                            if (token.getTokenType() == LexType.RPARENT) {
                                rParent_token = lexIterator.iterator().next();
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

                        if (isBlock()) {
                            block = new Block();
                            block.unitParser();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void print() {
        if (funcType != null)
            funcType.print();
        if (ident_token != null)
            IOUtils.writeCorrectLine(ident_token.toString());
        if (lParent_token != null)
            IOUtils.writeCorrectLine(lParent_token.toString());
        if (funcFParams != null)
            funcFParams.print();
        if (rParent_token != null)
            IOUtils.writeCorrectLine(rParent_token.toString());
        if (block != null)
            block.print();

        IOUtils.writeCorrectLine(toString());
    }

    /*@Override
    void setSyntaxName() {
        syntaxName = "FuncDef";
    }*/
}
