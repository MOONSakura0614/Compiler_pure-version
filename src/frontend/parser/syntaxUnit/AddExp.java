package frontend.parser.syntaxUnit;


import frontend.lexer.Token;
import frontend.symbol.Symbol;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;


/**
 * @author 郑悦
 * @Description: 加减表达式
 * AddExp → MulExp | AddExp ('+' | '−') MulExp
 */
public class AddExp extends SyntaxNode {
    private MulExp mulExp; // 只有一个，或者第一个Mul后面除了输出Mul也都要输出Add
    private ArrayList<AddOp_MulExp> addOp_mulExp_list;

    public AddExp() {
        super("AddExp");
        addOp_mulExp_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        // 防止左递归，先解析MulExp
        if (isMulExp()) {
            mulExp = new MulExp();
            mulExp.unitParser();
        } else {
            throw new RuntimeException("AddExp解析失败：MulExp无法识别");
        }
        Token token;
        AddOp_MulExp addOp_mulExp;
        MulExp mulExp1;
        while (isAddOperator()) {
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (isMulExp()) {
                    mulExp1 = new MulExp();
                    mulExp1.unitParser();
                    addOp_mulExp = new AddOp_MulExp(token, mulExp1);
                    addOp_mulExp_list.add(addOp_mulExp);
                } else {
                    throw new RuntimeException("AddExp解析失败：AddOp后无法识别到MulExp");
                }
            }
        }
    }

    @Override
    public void print() {
        if (mulExp != null)
            mulExp.print();
        if (!addOp_mulExp_list.isEmpty()) {
            for (AddOp_MulExp addOp_mulExp: addOp_mulExp_list) {
                addOp_mulExp.print();
            }
        }
        IOUtils.writeCorrectLine(toString());
    }

    public boolean isArrayElement() {
        if (mulExp != null)
            return mulExp.isArrayElement();
        return false;
    }

    public Symbol getIdentSymbol() {
        if (mulExp != null)
            return mulExp.getIdentSymbol();
        return null;
    }

    public boolean isIdentArray() {
        if (mulExp != null)
            return mulExp.isIdentArray();
        return false;
    }

    public class AddOp_MulExp {
        Token addOp_token;
        MulExp mulExp;

        public AddOp_MulExp(Token token, MulExp mulExp) {
            addOp_token = token;
            this.mulExp = mulExp;
        }

        public void print() {
            // 注意在AddExp中，如果存在很多MulExp，说明是经过AddExp AddOp MulExp的推导的
            // 前一个MulExp，已经输出<MulExp>下面先输出<AddExp>即可
            IOUtils.writeCorrectLine(new AddExp().toString());
            if (addOp_token != null) {
                IOUtils.writeCorrectLine(addOp_token.toString());
            }
            if (mulExp != null) {
                mulExp.print();
            }
        }

    }

    @Override
    public void visit() {
        if (mulExp != null)
            mulExp.visit();

        for (AddOp_MulExp addOp_mulExp: addOp_mulExp_list) {
            if (addOp_mulExp.mulExp != null)
                addOp_mulExp.mulExp.visit();
        }
    }
}
