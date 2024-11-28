package frontend.parser.syntaxUnit;

import frontend.symbol.Symbol;
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

    @Override
    public void visit() {
        if (addExp != null)
            addExp.visit();
    }

    public boolean isArrayElement() {
        if (addExp != null)
            return addExp.isArrayElement();
        return false;
    }

    public boolean isIdentArray() {
        if (addExp != null)
            return addExp.isIdentArray();
        return false;
    }

    public Symbol getIdentSymbol() {
        if (addExp != null)
            return addExp.getIdentSymbol();
        return null;
    }

    public int getIntValue() {
        if (addExp != null)
            return addExp.getIntValue();

        return 0;
    }

    public AddExp getAddExp() {
        return addExp;
    }
}
