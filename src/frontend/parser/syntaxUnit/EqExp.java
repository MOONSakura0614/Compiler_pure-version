package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import llvm.value.instruction.Operator;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 相等性表达式
 * EqExp → RelExp | EqExp ('==' | '!=') RelExp
 */
public class EqExp extends SyntaxNode {
    private RelExp relExp;
    private ArrayList<EqOp_RelExp> eqOp_relExp_list;

    public EqExp() {
        super("EqExp");
        eqOp_relExp_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        if (isRelExp()) {
            relExp = new RelExp();
            relExp.unitParser();

            Token token;
            RelExp relExp1;
            EqOp_RelExp eqOp_relExp;
            while (isEqOperator()) {
                token = lexIterator.iterator().next();

                if (isRelExp()) {
                    relExp1 = new RelExp();
                    relExp1.unitParser();

                    eqOp_relExp = new EqOp_RelExp(token, relExp1);
                    eqOp_relExp_list.add(eqOp_relExp);
                }
            }
        }
    }

    @Override
    public void print() {
        if (relExp != null) {
            relExp.print();
        }

        if (!eqOp_relExp_list.isEmpty()) {
            for (EqOp_RelExp eqOp_relExp: eqOp_relExp_list) {
                eqOp_relExp.print();
            }
        } // 最后一个就是RelExp，按文法规则，无需输出EqExp。下面是总的整体的EqExp

        IOUtils.writeCorrectLine(toString());
    }

    public class EqOp_RelExp {
        Token eq_token;
        RelExp relExp;
        private final static EqExp eqExp = new EqExp();

        public EqOp_RelExp(Token token, RelExp relExp) {
            eq_token = token;
            this.relExp = relExp;
        }

        public void print() {
            IOUtils.writeCorrectLine(eqExp.toString());

            if (eq_token != null)
                IOUtils.writeCorrectLine(eq_token.toString());

            if (relExp != null)
                relExp.print();
        }

        public RelExp getRelExp() {
            return relExp;
        }

        public Operator getEqOp() {
            return Operator.opMap.get(eq_token.getTokenType());
        }
    }

    @Override
    public void visit() {
        if (relExp != null) {
            relExp.visit();
        }
        for (EqOp_RelExp eqOp_relExp: eqOp_relExp_list) {
            if (eqOp_relExp.relExp != null)
                eqOp_relExp.relExp.visit();
        }
    }

    public boolean isEqOpRelExpsEmpty() {
        return eqOp_relExp_list.isEmpty();
    }

    public RelExp getRelExp() {
        return relExp;
    }

    /* 仅返回EqOp( == | != )后的RelExp的List */
    public ArrayList<RelExp> getEqOpRelExps() {
        ArrayList<RelExp> relExps = new ArrayList<>();
        for (EqOp_RelExp eqOp_relExp: eqOp_relExp_list) {
            relExps.add(eqOp_relExp.relExp);
        }
        return relExps;
    }

    public ArrayList<EqOp_RelExp> getEqOp_relExp_list() {
        return eqOp_relExp_list;
    }
}
