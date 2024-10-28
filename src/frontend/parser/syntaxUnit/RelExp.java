package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 关系表达式（为什么这里还有+-*、的事情？
 * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 */
public class RelExp extends SyntaxNode {
    private AddExp addExp;
    private ArrayList<RelOp_AddExp> relOp_addExp_list;

    public RelExp() {
        super("RelExp");
        relOp_addExp_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        if (isAddExp()) {
            addExp = new AddExp();
            addExp.unitParser();

            Token token;
            AddExp addExp1;
            RelOp_AddExp relOp_addExp;
            while (isRelOperator()) {
                token = lexIterator.iterator().next();

                if (isAddExp()) {
                    addExp1 = new AddExp();
                    addExp1.unitParser();

                    relOp_addExp = new RelOp_AddExp(token, addExp1);
                    relOp_addExp_list.add(relOp_addExp);
                }
            }
        }
    }

    @Override
    public void print() {
        if (addExp != null) {
            addExp.print();
        }
        if (!relOp_addExp_list.isEmpty()) {
            for (RelOp_AddExp relOp_addExp: relOp_addExp_list) {
                relOp_addExp.print();
            }
        }
        IOUtils.writeCorrectLine(toString());
    }

    private class RelOp_AddExp {
        private Token relOp_token;
        private AddExp addExp;
        private static final RelExp relExp = new RelExp();

        RelOp_AddExp(Token token, AddExp addExp) {
            relOp_token = token;
            this.addExp = addExp;
        }

        public void print() {
            IOUtils.writeCorrectLine(relExp.toString());

            if (relOp_token != null)
                IOUtils.writeCorrectLine(relOp_token.toString());

            if (addExp != null)
                addExp.print();
        }
    }

    @Override
    public void visit() {
        if (addExp != null)
            addExp.visit();
        for (RelOp_AddExp relOp_addExp: relOp_addExp_list) {
            if (relOp_addExp.addExp != null)
                relOp_addExp.addExp.visit();
        }
    }
}
