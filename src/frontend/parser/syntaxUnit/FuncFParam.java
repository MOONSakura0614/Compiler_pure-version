package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.symbol.SymbolTable;
import frontend.symbol.SymbolType;
import frontend.symbol.VarSymbol;
import frontend.visitor.Visitor;
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

    @Override
    public void insertSymbol(SymbolTable symbolTable) {
        VarSymbol symbol = new VarSymbol(ident_token, Visitor.curScope);

        // 设置符号类型
        if (bType.getIsInt()) {
            if (isArray) {
                symbol.setIsArray();
                symbol.setSymbolType(SymbolType.IntArray);
            }
            else
                symbol.setSymbolType(SymbolType.Int);
        } else {
            if (isArray) {
                symbol.setIsArray();
                symbol.setSymbolType(SymbolType.CharArray);
            }
            else
                symbol.setSymbolType(SymbolType.Char);
        } // Symbol有了符号名和类型，感觉不需要加入VarDef了

        symbolTable.insertSymbol(symbol); // 注意FFPs中的每个形参也是要判断是否重定义的
    }

    public Boolean getIsArray() {
        return isArray;
    }

    public boolean isInt() {
        if (bType == null)
            return false;
        return bType.getIsInt();
    }
}
