package frontend.parser.syntaxUnit;

import utils.IOUtils;

/**
 * @author 郑悦
 * @Description: 常量表达式
 *  ConstExp → AddExp 注：使用的 Ident 必须是常量
 */
public class ConstExp extends SyntaxNode {
    private AddExp addExp;

    public ConstExp() {
        super("ConstExp");
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
