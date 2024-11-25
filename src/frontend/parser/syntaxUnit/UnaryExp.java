package frontend.parser.syntaxUnit;

import errors.CompileError;
import errors.ErrorHandler;
import errors.ErrorType;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.Parser;
import frontend.symbol.FuncSymbol;
import frontend.symbol.Symbol;
import frontend.visitor.Visitor;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;
/**
 * @author 郑悦
 * @Description: 一元表达式
 * UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
 * // 3种情况均需覆盖,函数调用也需要覆盖FuncRParams的不同情况
 */
public class UnaryExp extends SyntaxNode {
    // 相邻两个 UnaryOp 不能相同，如 int a = ++-i; ，但是 int a = +-+i; 是可行的。
    private Boolean isOp;
    private Boolean isIdent;
    private Boolean isPrimaryExp;
    private PrimaryExp primaryExp;
    private Token ident_token;
    private Token leftParent_token;
    private FuncRParams funcRParams;
    private Token rightParent_token;
    private UnaryOp unaryOp; // 指的是只有这个开头的
    private UnaryExp unaryExp;
//  最右侧一定是PrimaryExp或Ident(函数参数)结尾！-->这二者都可能是数组存在多个的

    public UnaryExp() {
        super("UnaryExp");
        isOp = Boolean.FALSE;
        isIdent = Boolean.FALSE;
        isPrimaryExp = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        // 狂喜：UnaryExp没有左递归
        if (isPrimaryExp()) {
            isPrimaryExp = Boolean.TRUE;
            primaryExp = new PrimaryExp();
            primaryExp.unitParser();
            // return
        }
        else if (isFuncCall()) { // first集不重叠，只有FuncCall在上面右式的个选择项中才是Ident开头的
            isIdent = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) {
                ident_token = lexIterator.iterator().next();
                if (lexIterator.iterator().hasNext()) {
                    leftParent_token = lexIterator.iterator().next();

                    if (isRParams()) {
                        funcRParams = new FuncRParams();
                        funcRParams.unitParser();
                    }

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
        else if (isUnaryOp()) {
            // 会不会递归调用太深？直到解析到PE或者FC才能逐层函数返回
            // Operator打头
            isOp = Boolean.TRUE;
            unaryOp = new UnaryOp();
            unaryOp.unitParser();
            unaryExp = new UnaryExp();
            unaryExp.unitParser();
        }
    }

    @Override
    public void print() {
        if (isPrimaryExp) {
            primaryExp.print();
        } else if (isIdent) {
            if (ident_token != null)
                IOUtils.writeCorrectLine(ident_token.toString());
            if (leftParent_token != null)
                IOUtils.writeCorrectLine(leftParent_token.toString());
            if (funcRParams != null)
                funcRParams.print();
            if (rightParent_token != null)
                IOUtils.writeCorrectLine(rightParent_token.toString());
        } else if (isOp) {
            if (unaryOp != null) {
                unaryOp.print();
            }
            if (unaryExp != null) {
                unaryExp.print();
            }
        }

        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        if (isPrimaryExp) {
            if (primaryExp != null)
                primaryExp.visit();
        } else if (isIdent) {
            if (ident_token == null)
                return;
            Symbol funcSym = Visitor.curTable.findInCurSymTable(ident_token.getTokenValue());
            // isIdent就代表调用函数，下面可能出现形参表和未定义的情况
            if (funcSym == null) {
                ErrorHandler.undefineErrorHandle(ident_token.getLineNum());
            } else {
                if (!funcSym.isFunc()) { // 会出现不是func的情况吗（之前重定义的时候不同类型的symbol？？
                    // TODO: 2024/10/26 如果遇到调用符号，结果符号类型不是函数
                    return;
                }
                if (funcSym instanceof FuncSymbol) {
                    ((FuncSymbol) funcSym).paramsMatch(funcRParams, ident_token.getLineNum());
                }
                // 判断实参与形参的对应
                /*上面在FuncSym内部判断参数对应关系，下面单独判断符号未定义*/
                if (funcRParams != null)
                    // 而且传入的实参是Exp形式，有可能是单独Ident也可能是其他
                    funcRParams.visit();
            }
        }
    }

    public boolean isArrayElement() {
        if (isIdent || isOp) { // ident开头就只有函数调用了，所以不是数组
            return false;
        }
        if (isPrimaryExp) {
            if (primaryExp != null)
                return primaryExp.isArrayElement();
        }
        return false;
    }

    public Symbol getIdentSymbol() {
        if (primaryExp != null)
            return primaryExp.getIdentSymbol();
        return null;
    }

    public boolean isIdentArray() {
        if (isIdent || isOp) { // ident开头就只有函数调用了，所以不是数组
            return false;
        }
        if (isPrimaryExp) {
            if (primaryExp != null)
                return primaryExp.isIdentArray();
        }
        return false;
    }

    public int getIntValue() {
        if (isPrimaryExp) {
            if (primaryExp != null)
                return primaryExp.getIntValue();

            return 1;
        }

        int res = 0;
        if (isOp) {
            if (unaryOp == null || unaryExp == null)
                return 1;

            LexType type = unaryOp.getUnaryOp_token().getTokenType();
            switch (type) {
                case PLUS -> {
                    res += unaryExp.getIntValue();
                }
                case MINU -> {
                    res -= unaryExp.getIntValue();
                }
                case NOT -> {
                    // '!'仅出现在条件表达式中 --> 在求intValue的时候先不做处理
                    // TODO: 2024/11/25 条件判断
                }
            }
            return res;
        }

        // TODO: 2024/11/25 如果是Ident():就是调用函数，需要借用call instruction实现
        return 1; // 防止把其他的MulExp给消了
    }
}
