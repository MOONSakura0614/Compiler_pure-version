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

    /*// UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public class UnaryOp_FuncCall {
        static UnaryExp unaryExp; // 主要目的是用于输出toString的name
        UnaryOp unaryOp;
        FuncCallHelp funcCallHelp;

        public UnaryOp_FuncCall(UnaryOp unaryOp, FuncCallHelp funcCallHelp) {
            this.unaryOp = unaryOp;
            this.funcCallHelp = funcCallHelp;
        }

        public void print() {
            IOUtils.writeCorrectLine(unaryExp.toString());
            if (unaryOp != null) {
                unaryOp.print();
            }
            if (funcCallHelp != null) {
                funcCallHelp.print();
            }
        }
    }

    public class UnaryOp_PrimaryExp {
        static UnaryExp unaryExp;
        UnaryOp unaryOp;
        PrimaryExp primaryExp;
    }

    public class UnaryOp_UnaryExp {
        private static UnaryExp unaryExp;
        private UnaryOp unaryOp;
        private PrimaryExp primaryExp;
        private FuncCallHelp funcCallHelp;
        private Boolean isFuncCall;

        public UnaryOp_UnaryExp(UnaryOp unaryOp, Boolean isFuncCall, SyntaxNode syntaxNode) {
            this.unaryOp = unaryOp;
            this.isFuncCall = isFuncCall;
            if (isFuncCall) {
                if (syntaxNode instanceof FuncCallHelp)
                    this.funcCallHelp = (FuncCallHelp) syntaxNode;
                else
                    throw new RuntimeException("UnaryOp_UnaryExp构造异常");
            } else {
                this.primaryExp = (PrimaryExp) syntaxNode;
            }
        }

        public void print() {
            // 这个是循环时的数组存在
//            IOUtils.writeCorrectLine(unaryExp.toString());
            if (unaryOp != null) {
                unaryOp.print();
            }
            if (isFuncCall) {
                if (funcCallHelp != null) {
                    funcCallHelp.print();
                }
            } else {
                if (primaryExp != null) {
                    primaryExp.print();
                }
            }
            // 这个不是左递归，顺序没反？?
            IOUtils.writeCorrectLine(unaryExp.toString());
        }

    }*/

    public static void main(String[] args) {
        UnaryExp unaryExp1 = new UnaryExp();
        unaryExp1.isIdent = Boolean.TRUE;
        unaryExp1.ident_token = new Token("test", LexType.IDENFR, 2);
        unaryExp1.leftParent_token = new Token("(", LexType.LPARENT, 2);
        unaryExp1.rightParent_token = new Token(")", LexType.RPARENT, 2);

        unaryExp1.print();
    }
}
