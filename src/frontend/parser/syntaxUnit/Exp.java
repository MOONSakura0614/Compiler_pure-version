package frontend.parser.syntaxUnit;

import utils.IOUtils;

/**
 * @author 郑悦
 * @Description: 表达式
 * Exp → AddExp
 */
public class Exp extends SyntaxNode {
    private AddExp addExp;

    public Exp() {
        super("Exp");
    }

    @Override
    public void unitParser() {
        if (isAddExp()) {
            addExp = new AddExp();
            addExp.unitParser();
        } else {
            throw new RuntimeException("解析ConstExp错误：无法得到AddExp");
        }
    }

    @Override
    public void print() {
        if (addExp != null) {
            addExp.print();
        }
        IOUtils.writeCorrectLine(this.toString());
    }
}
