package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 逻辑或表达式
 * LOrExp → LAndExp | LOrExp '||' LAndExp
 */
public class LOrExp extends SyntaxNode {
    private LAndExp lAndExp;
    // 注意这里也是颠倒顺序防止左递归
    private ArrayList<Or_LAndExp> or_lAndExp_list;

    public LOrExp() {
        super("LOrExp");
        or_lAndExp_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        if (isLAndExp()) {
            lAndExp = new LAndExp();
            lAndExp.unitParser();

            Token token;
            LAndExp lAndExp1;
            Or_LAndExp or_lAndExp;
            while (isOrOP()) {
                token = lexIterator.iterator().next();

                if (isLAndExp()) {
                    lAndExp1 = new LAndExp();
                    lAndExp1.unitParser();

                    or_lAndExp = new Or_LAndExp(token, lAndExp1);
                    or_lAndExp_list.add(or_lAndExp);
                } else {
                    throw new RuntimeException("LOrExp解析失败：无法识别LAndExp");
                }
            }
        } else {
            throw new RuntimeException("LOrExp解析失败：无法识别LAndExp");
        }
    }

    @Override
    public void print() {
        if (lAndExp != null)
            lAndExp.print();
        if (!or_lAndExp_list.isEmpty()) {
            for (Or_LAndExp or_lAndExp: or_lAndExp_list) {
                or_lAndExp.print();
            }
        }

        IOUtils.writeCorrectLine(toString());
    }

    public class Or_LAndExp {
        private Token or_token;
        private LAndExp lAndExp;
        private static LOrExp lOrExp = new LOrExp();

        public Or_LAndExp(Token token, LAndExp lAndExp) {
            or_token = token;
            this.lAndExp = lAndExp;
        }

        public void print() {
            // 注意是把前一个解析出来的LAndExp当成LOrExp
            IOUtils.writeCorrectLine(lOrExp.toString());

            if (or_token != null) {
                IOUtils.writeCorrectLine(or_token.toString());
            }
            if (lAndExp != null) {
                lAndExp.print();
            }
        }
    }

    @Override
    public void visit() {
        if (lAndExp != null)
            lAndExp.visit();
        for (Or_LAndExp or_lAndExp: or_lAndExp_list) {
            if (or_lAndExp.lAndExp != null)
                or_lAndExp.lAndExp.visit();
        }
    }
}
