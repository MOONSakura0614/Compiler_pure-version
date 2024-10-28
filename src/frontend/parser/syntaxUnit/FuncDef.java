package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.symbol.FuncSymbol;
import frontend.symbol.SymbolTable;
import frontend.symbol.SymbolType;
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

    @Override
    public void visit() {
        // 规则定义：将有新建符号表的定义为visit；其他只插入的用insert
        SymbolTable table = initSymbolTable();
        // 先把形参表的加入 FuncFParams
        if (funcFParams != null)
            funcFParams.insertSymbol(table);
        // 把block中的加入 Block
        /*应该区分一般的block和函数中第一层的block
        * 1.返回值问题
        * 2.形参表是外层的，其他的遇到block应该新增一层表！*/
        if (block != null) { // block应该有visit才可（？遇到新的block）
            block.visitInFunc(funcType.getFuncType()); // 此处用curTable就可（如果要加符号）
            // 怎么判断在FuncBlock最后一行（'}'之前的return语句）
        }

        // funcDef中的符号表建立结束之后应该退出:scope不改变，但是curScope改变
        exitCurScope();
        /*Visitor.curTable = Visitor.curTable.getFatherTable(); // curTable至少是CompUnit级别的全局，不会为null
        Visitor.curScope = Visitor.curTable.getScope();*/ // 让下一个能有父符号表
    }

    @Override
    public void insertSymbol(SymbolTable symbolTable) {
        // 此处重写方法目的是将函数名加入符号表（属于给传入参数table增加，
        // 不是函数这个作用域的符号表新建
        // 注意和普通变量声明不同，这边是函数符号声明
        if (this.ident_token == null)
            return;
        // 注意需要检查是否未曾存在才能插入
        /*String ident_name = this.ident_token.getTokenValue();
        if (symbolTable.isSymbolExist(ident_name)) {
            // 重定义-错误处理
            ErrorHandler.redefineErrorHandle(this.ident_token.getLineNum());
            return;
        }*/
//        Symbol symbol = new FuncSymbol(this, ident_name);
        FuncSymbol symbol = new FuncSymbol(this, ident_token, symbolTable.getScope());
        if (this.funcType.getFuncType() != null) {
            symbol.setFuncType(this.funcType.getFuncType());
        }
        if (funcFParams != null) {
            funcFParams.implFFPsSymbolDetail(symbol);
        }
        switch (funcType.getFuncType()) {
            case VOIDTK:
                symbol.setSymbolType(SymbolType.VoidFunc);
                break;
            case CHARTK:
                symbol.setSymbolType(SymbolType.CharFunc);
                break;
            case INTTK:
                symbol.setSymbolType(SymbolType.IntFunc);
                break;
        }

        symbolTable.insertSymbol(symbol); // 其实就是Visitor的静态变量curTable
    }
}
