package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 逻辑与表达式
 * LAndExp → EqExp | LAndExp '&&' EqExp
 */
public class LAndExp extends SyntaxNode {
    private EqExp eqExp;
    private ArrayList<And_EqExp> and_eqExp_list;

    public LAndExp() {
        super("LAndExp");
        and_eqExp_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        if (isEqExp()) {
            eqExp = new EqExp();
            eqExp.unitParser();

            Token token;
            EqExp eqExp1;
            And_EqExp and_eqExp;
            while (isAndOP()) {
                token = lexIterator.iterator().next();

                if (isEqExp()) {
                    eqExp1 = new EqExp();
                    eqExp1.unitParser();

                    and_eqExp = new And_EqExp(token, eqExp1);
                    and_eqExp_list.add(and_eqExp);
                } else {
                    throw new RuntimeException("LAndExp解析失败：无法识别EqExp");
                }
            }
        } else {
            throw new RuntimeException("LAndExp解析失败：无法识别EqExp");
        }
    }

    @Override
    public void print() {
        if (eqExp != null)
            eqExp.print();
        if (!and_eqExp_list.isEmpty()) {
            for (And_EqExp and_eqExp: and_eqExp_list) {
                and_eqExp.print();
            }
        }
        IOUtils.writeCorrectLine(toString());
    }

    public class And_EqExp {
        private Token and_token;
        private EqExp eqExp;
        private static LAndExp lAndExp = new LAndExp();

        public And_EqExp(Token token, EqExp eqExp) {
            and_token = token;
            this.eqExp = eqExp;
        }

        public void print() {
            // 注意是把前一个解析出来的LAndExp当成LOrExp
            IOUtils.writeCorrectLine(lAndExp.toString());

            if (and_token != null) {
                IOUtils.writeCorrectLine(and_token.toString());
            }
            if (eqExp != null) {
                eqExp.print();
            }
        }
    }

    @Override
    public void visit() {
        if (eqExp != null)
            eqExp.visit();
        for (And_EqExp and_eqExp: and_eqExp_list) {
            if (and_eqExp.eqExp != null)
                and_eqExp.eqExp.visit();
        }
    }

    public EqExp getEqExp() {
        return eqExp;
    }

    public boolean isAndEqExpsEmpty() {
        return and_eqExp_list.isEmpty();
    }

    public ArrayList<EqExp> getAndEqExps() { // 返回&&连接的那些EqExp——这步构建也需要短路求值（添加br跳转）
        ArrayList<EqExp> andExps = new ArrayList<>();
        for (And_EqExp and_eqExp: and_eqExp_list) {
            andExps.add(and_eqExp.eqExp);
        }
        return andExps;
    }
}
