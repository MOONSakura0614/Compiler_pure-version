package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @description 主要用于协助解析函数调用（实际没有这个规则）
 * FuncCall： Ident '(' [FuncRParams] ')'
 * @implNote 注意区分，与FuncDef无关，比如括号中可以解析的是实参表，不是形参表
 */
public class FuncCallHelp extends SyntaxNode {
    private Token func_ident_token;
    private Token leftParent_token;
    private FuncRParams funcRParams;
    private Token rightParent_token;
    private Boolean isRParams;

    public FuncCallHelp() {
        isRParams = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        if (isIdent()) {
            if (lexIterator.iterator().hasNext()) {
                func_ident_token = lexIterator.iterator().next();
            }
            // 左括号
            if (lexIterator.curPos < lexIterator.tokenCount) {
                if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.LPARENT) {
                    leftParent_token = lexIterator.iterator().next();
                    if (isRParams()) {
                        funcRParams = new FuncRParams();
                        funcRParams.unitParser();
                    }
                    // 分析右括号
                    if (lexIterator.curPos < lexIterator.tokenCount) {
                        if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.RPARENT) {
                            rightParent_token = lexIterator.iterator().next();
                        } else {
                            CompileError error = new CompileError(lexIterator.nowToken().getLineNum(), ErrorType.LackRPARENT);
                            IOUtils.compileErrors.add(error);
                        }
                    }
                }
            } else {
                throw new RuntimeException("FuncCallError: 无法识别左括号");
            }
        } else {
            throw new RuntimeException("FuncCallError: 无法识别调用函数名");
        }
    }

    @Override
    public void print() {
        if (func_ident_token != null) {
            IOUtils.writeCorrectLine(func_ident_token.toString());
        }
        if (leftParent_token != null) {
            IOUtils.writeCorrectLine(leftParent_token.toString());
        }
        if (funcRParams != null)
            funcRParams.print();
        if (rightParent_token != null) {
            IOUtils.writeCorrectLine(rightParent_token.toString());
        }
    }
}
