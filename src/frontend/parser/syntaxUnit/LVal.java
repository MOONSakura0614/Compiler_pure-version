package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorHandler;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.symbol.Symbol;
import frontend.visitor.Visitor;
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
        IOUtils.writeCorrectLine(toString());
    }

    private Symbol ident_symbol;

    @Override
    public void visit() {
        if (ident_token == null)
            return;

        // 重点在错误处理：常量赋值 | 未定义调用符号
        // 注意一行应该不会出现两种错误
        Symbol symbol = null;
        symbol = Visitor.curTable.findInCurSymTable(ident_token.getTokenValue());
        if (symbol == null) {
            ErrorHandler.undefineErrorHandle(ident_token.getLineNum());
        } else {
            ident_symbol = symbol;
        }
        // 但是只出现LVal不一定会出现assign：另起一个函数判断常量？或者在不同plan的stmt中判断
        // 这里遍历的时候把Symbol的属性给到LVal应该就行（
    }

    public void handleConstAssignError() {
        if (ident_token == null)
            return;

        if (ident_symbol == null) {
            ident_symbol = Visitor.curTable.findInCurSymTable(ident_token.getTokenValue());
        }
        // 未定义的错误上面报过了，下面只要return即可
        if (ident_symbol == null) {
            return;
        }

        if (ident_symbol.isConstSymbol()) {
            ErrorHandler.constChangeErrorHandle(ident_token.getLineNum());
        }
    }

    public boolean isIdentArray() {
        // 查表ident
        if (ident_token == null)
            return false;
        if (isArrayElement)
            return false; // 虽然是符号表中的数组名，但是没有用整个数组传参，只有数组元素

        Symbol symbol = Visitor.curTable.findInCurSymTable(ident_token.getTokenValue());
        return symbol.getIsArray();
    }

    public Boolean getIsArrayElement() {
        return isArrayElement;
    }

    public Symbol getIdentSymbol() {
        if (ident_token == null)
            return null;

        return Visitor.curTable.findInCurSymTable(ident_token.getTokenValue());
    }
}
