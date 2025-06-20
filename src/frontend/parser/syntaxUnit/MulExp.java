package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.symbol.Symbol;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 乘除模表达式
 * MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 */
public class MulExp extends SyntaxNode {
    private UnaryExp unaryExp;
    private ArrayList<MulOp_UnaryExp> mulOp_unaryExp_list;

    public MulExp() {
        super("MulExp");
        mulOp_unaryExp_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        if (isUnaryExp()) {
            unaryExp = new UnaryExp();
            unaryExp.unitParser();
        } else {
            throw new RuntimeException("MulExp解析失败：UnaryExp无法识别");
        }
        Token token;
        MulOp_UnaryExp mulOp_unaryExp;
        UnaryExp unaryExp1;
        while (isMulOperator()) {
            if (lexIterator.iterator().hasNext()) {
                token = lexIterator.iterator().next();
                if (isMulExp()) {
                    unaryExp1 = new UnaryExp();
                    unaryExp1.unitParser();
                    mulOp_unaryExp = new MulOp_UnaryExp(token, unaryExp1);
                    mulOp_unaryExp_list.add(mulOp_unaryExp);
                } else {
                    throw new RuntimeException("MulExp解析失败：MulOp后无法识别到UnaryExp");
                }
            }
        }
    }

    @Override
    public void print() {
        if (unaryExp != null)
            unaryExp.print();
        if (!mulOp_unaryExp_list.isEmpty()) {
            for (MulOp_UnaryExp mulOpUnaryExp: mulOp_unaryExp_list) {
                mulOpUnaryExp.print();
            }
        }
        IOUtils.writeCorrectLine(toString());
    }

    public boolean isArrayElement() {
        if (unaryExp != null)
            return unaryExp.isArrayElement();
        return false;
    }

    public Symbol getIdentSymbol() {
        if (unaryExp != null)
            return unaryExp.getIdentSymbol();
        return null;
    }

    public boolean isIdentArray() {
        if (unaryExp != null)
            return unaryExp.isIdentArray();
        return false;
    }

    public int getIntValue() {
        if (mulOp_unaryExp_list.isEmpty()) {
            if (unaryExp != null)
                return unaryExp.getIntValue();

            return 0;
        }

        int res = 1;
        // 是mulExp mulOp AddExp……的格式
        if (unaryExp != null)
            res =  unaryExp.getIntValue(); // 最左的

        for (MulOp_UnaryExp mulOp_unaryExp: mulOp_unaryExp_list) {
            if (mulOp_unaryExp.mulOp_token != null) {
                LexType opType = mulOp_unaryExp.mulOp_token.getTokenType();
                if (opType.equals(LexType.MULT)) { // '*'
                    if (mulOp_unaryExp.unaryExp != null)
                        res *= mulOp_unaryExp.unaryExp.getIntValue();
                } else if (opType.equals(LexType.DIV)) { // '/'
                    if (mulOp_unaryExp.unaryExp != null)
                        res /= mulOp_unaryExp.unaryExp.getIntValue();
                } else if (opType.equals(LexType.MOD)) { // '%'
                    if (mulOp_unaryExp.unaryExp != null)
                        res %= mulOp_unaryExp.unaryExp.getIntValue();
                }
            }
        }
        return res;
    }

    public boolean isUndefinedIdent() {
        if (mulOp_unaryExp_list.isEmpty()) {
            if (unaryExp != null) {
                return unaryExp.isUndefinedIdent();
            }
            return false;
        } else {
            if (unaryExp.isUndefinedIdent()) {
                return true;
            }
            for (MulOp_UnaryExp mulOp_unaryExp: mulOp_unaryExp_list) {
                if (mulOp_unaryExp.unaryExp.isUndefinedIdent()) {
                    return true;
                }
            }
            return false;
        }
    }

    public class MulOp_UnaryExp {
        Token mulOp_token;
        UnaryExp unaryExp;
        static MulExp mulExp = new MulExp(); // 静态变量专门提供输出

        public MulOp_UnaryExp(Token token, UnaryExp unaryExp) {
            mulOp_token = token;
            this.unaryExp = unaryExp;
        }

        public void print() {
            IOUtils.writeCorrectLine(mulExp.toString());
            // 注意在AddExp中，如果存在很多MulExp，说明是经过AddExp AddOp MulExp的推导的
            if (mulOp_token != null) {
                IOUtils.writeCorrectLine(mulOp_token.toString());
            }
            if (unaryExp != null) {
                unaryExp.print();
            }
        }

        public UnaryExp getUnaryExp() {
            return unaryExp;
        }

        public Token getMulOp_token() {
            return mulOp_token;
        }
    }

    @Override
    public void visit() {
        if (unaryExp != null)
            unaryExp.visit();

        for (MulOp_UnaryExp mulOp_unaryExp: mulOp_unaryExp_list) {
            if (mulOp_unaryExp.unaryExp != null)
                mulOp_unaryExp.unaryExp.visit();
        }
    }

    public UnaryExp getUnaryExp() {
        return unaryExp;
    }

    public ArrayList<MulOp_UnaryExp> getMulOp_unaryExp_list() {
        return mulOp_unaryExp_list;
    }
}
