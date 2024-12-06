package frontend.parser.syntaxUnit;

import utils.IOUtils;

/**
 * @author 郑悦
 * @Description: 条件表达式
 * Cond → LOrExp
 */
public class Cond extends SyntaxNode {
    private LOrExp lOrExp;

    public Cond() {
        super("Cond");
    }

    @Override
    public void unitParser() {
        if (isLOrExp()) {
            lOrExp = new LOrExp();
            lOrExp.unitParser();
        }
    }

    @Override
    public void print() {
        if (lOrExp != null) {
            lOrExp.print();
        }
        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        if (lOrExp != null)
            lOrExp.visit();
    }

    public LOrExp getlOrExp() {
        return lOrExp;
    }
}
