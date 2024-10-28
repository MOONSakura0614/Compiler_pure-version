package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import utils.IOUtils;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 语句
 * ForStmt → LVal '=' Exp
 */
public class ForStmt extends SyntaxNode {
    private LVal lVal;
    private Token assign_token;
    private Exp exp;

    public ForStmt() {
        super("ForStmt");
    }

    @Override
    public void unitParser() {
        if (isLVal()) {
            lVal = new LVal();
            lVal.unitParser();
            if (lexIterator.iterator().hasNext()) {
                if (lexIterator.tokenList.get(lexIterator.curPos).getTokenType() == LexType.ASSIGN) {
                    assign_token = lexIterator.iterator().next();
                    if (isExp()) {
                        exp = new Exp();
                        exp.unitParser();
                    } else {
                        throw new RuntimeException("ForStmt解析失败：无法识别Exp");
                    }
                } else {
                    throw new RuntimeException("ForStmt解析失败：无法识别=");
                }
            } else {
                throw new RuntimeException("ForStmt解析失败：无法识别=");
            }
        } else {
            throw new RuntimeException("ForStmt解析失败：无法识别LVal");
        }
    }

    @Override
    public void print() {
        if (lVal != null)
            lVal.print();
        if (assign_token != null)
            IOUtils.writeCorrectLine(assign_token.toString());
        if (exp != null)
            exp.print();

        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        if (lVal != null)
            lVal.visit();
        if (exp != null)
            exp.visit();
        if (lVal != null)
            lVal.handleConstAssignError();
    }
}
