package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import java.util.AbstractList;
import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 函数实参表
 * FuncRParams → Exp { ',' Exp }
 */
public class FuncRParams extends SyntaxNode {
    private Exp exp;
    private ArrayList<Comma_Exp> comma_exp_list;

    public FuncRParams() {
        super("FuncRParams");
        comma_exp_list = new ArrayList<>();
        rParamCount = 0;
    }

    @Override
    public void unitParser() {
        if (isExp()) {
            exp = new Exp();
            exp.unitParser();
        }
        Token token;
        Exp exp1;
        Comma_Exp comma_exp;
        while (isComma()) {
            if (lexIterator.iterator().hasNext()) {
                // 先把逗号解析出来
                token = lexIterator.iterator().next();
                if (isExp()) {
                    exp1 = new Exp();
                    exp1.unitParser();
                    comma_exp = new Comma_Exp(token, exp1);
                    comma_exp_list.add(comma_exp);
                } else {
                    throw new RuntimeException("没有下一个Exp，多余逗号");
                }
            }
        }
    }

    @Override
    public void print() {
        if (exp != null)
            exp.print();
        if (!comma_exp_list.isEmpty()) {
            for (Comma_Exp comma_exp: comma_exp_list) {
                comma_exp.print();
            }
        }

        IOUtils.writeCorrectLine(toString());
    }

    public class Comma_Exp {
        Token comma_token;
        Exp exp;

        public Comma_Exp(Token token, Exp exp) {
            comma_token = token;
            this.exp = exp;
        }

        public void print() {
            if (comma_token != null)
                IOUtils.writeCorrectLine(comma_token.toString());
            if (exp != null)
                exp.print();
        }
    }

    @Override
    public void visit() {
        if (exp != null)
            exp.visit();

        for (Comma_Exp comma_exp: comma_exp_list) {
            if (comma_exp.exp != null)
                comma_exp.exp.visit();
        }
    }

    private int rParamCount;

    public int getRParamCount() {
        if (rParamCount != 0)
            return rParamCount;

        if (exp != null)
            rParamCount = 1;
        /*for (Comma_Exp comma_exp: comma_exp_list) {
            if (comma_exp.exp != null)
                rParamCount++;
        }*/
        rParamCount += comma_exp_list.size();
        return rParamCount;
    }

    private ArrayList<Exp> exps = null;
    public void setExps() {
        exps = new ArrayList<>();
        if (exp != null)
            exps.add(exp);
        for (Comma_Exp comma_exp: comma_exp_list) {
            if (comma_exp.exp != null)
                exps.add(comma_exp.exp);
        }
    }

    public ArrayList<Exp> getExps() {
        if (exps == null)
            setExps();

        return exps;
    }
}
