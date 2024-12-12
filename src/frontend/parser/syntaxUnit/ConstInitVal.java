package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.symbol.Symbol;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 常量初值
 * ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
 */
public class ConstInitVal extends SyntaxNode {
    private ConstExp constExp; // 不管是不是数组，至少有一个Exp
    private Token left_brace_token;
    private ArrayList<Comma_ConstExp> comma_constExp_list;
    private Token right_brace_token;
    private Token string_const_token;
    private Boolean isArrayInit;
    private Boolean isStringInit;

    public ConstInitVal() {
        super("ConstInitVal");
        isArrayInit = Boolean.FALSE;
        isStringInit = Boolean.FALSE;
        comma_constExp_list = new ArrayList<>();
    }

    /*注意上述推导式子中的StringConst，字符串成常量（或解读为char[]一维数组常量，是终结符形式！不是要解析的语法成分？*/

    @Override
    public void unitParser() {
        if (isStringConst()) {
            isStringInit = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) {
                string_const_token = lexIterator.iterator().next();
            }
        } else if (isArrayInit()) { // 数组初始化
            isArrayInit = Boolean.TRUE;
            if (lexIterator.iterator().hasNext()) { // 肯定有左大括号
                left_brace_token = lexIterator.iterator().next();
            }
            // 注意不一定要有Exp，可以是空数组初始化[Exp {, Exp}]
            if (isExp()) {
                constExp = new ConstExp();
                constExp.unitParser();
            }
            Token token;
            ConstExp constExp1;
            Comma_ConstExp comma_constExp;
            while (isComma()) {
                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.iterator().next();
                    if (isConstExp()) {
                        constExp1 = new ConstExp();
                        constExp1.unitParser();
                        comma_constExp = new Comma_ConstExp(token, constExp1);
                        comma_constExp_list.add(comma_constExp);
                    } else {
                        throw new RuntimeException("InitVal解析错误: constExp不能识别");
                    }
                } else {
                    lexIterator.retract();
                    throw new RuntimeException("InitVal解析错误: Comma不能识别");
                }
            }
            // 右括号
            if (lexIterator.iterator().hasNext()) { // 肯定有左大括号
                token = lexIterator.iterator().next();
                if (token.getTokenType() == LexType.RBRACE) {
                    right_brace_token = token;
                } else {
                    // 缺少右大括号：不算语法分析的错误【缺少分号，右小括号，右中括号
                    lexIterator.retract(); // 多读一个token
                    throw new RuntimeException("InitVal解析错误: }不能识别");
                }
            }
        } else if (isConstExp()) {
            constExp = new ConstExp();
            constExp.unitParser();
        }
    }

    @Override
    public void print() {
        if (isStringInit) { // 初始化就false了，不用担心null
            if (string_const_token != null)
                IOUtils.writeCorrectLine(string_const_token.toString());
        } else if (isArrayInit) {
            if (left_brace_token != null) {
                IOUtils.writeCorrectLine(left_brace_token.toString());
            }
            if (constExp != null) {
                constExp.print();
            }
            if (!comma_constExp_list.isEmpty()) {
                for (Comma_ConstExp comma_constExp: comma_constExp_list) {
                    comma_constExp.print();
                }
            }
            if (right_brace_token != null) {
                IOUtils.writeCorrectLine(right_brace_token.toString());
            }
        } else { // 普通的只有一个constExp
            if (constExp != null) {
                constExp.print();
            }
        }
        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        // 检查Exp中的变量引用
        if (isArrayInit) {
            for (Comma_ConstExp comma_constExp: comma_constExp_list) {
                if (comma_constExp.constExp != null)
                    comma_constExp.constExp.visit();
            }
        } else if (!isStringInit) {
            if (constExp != null) {
                constExp.visit();
            }
        }
    }

    public class Comma_ConstExp {
        Token comma_token;
        ConstExp constExp;

        public Comma_ConstExp(Token token, ConstExp constExp) {
            comma_token = token;
            this.constExp = constExp;
        }

        public void print() {
            if (comma_token != null)
                IOUtils.writeCorrectLine(comma_token.toString());
            if (constExp != null)
                constExp.print();
        }
    }

    public int getIntValue() {
        // 注意数组初始化的时候，int: 0-padding; char: '\0'-padding（应该也就是ascii的0）
        if (isArrayInit) {
            // TODO: 2024/11/25 数组初始化
            return 0;
        } else if (isStringInit) {
            // todo: stringConst只在char数组初始化 以及 printf的格式串中出现
            return 0;
        } else {
            if (constExp != null)
                return constExp.getIntValue();

            return 0;
        }
    }

    public ArrayList<ConstExp> getInitConstExps() {
        ArrayList<ConstExp> constExps = new ArrayList<>();
        constExps.add(constExp);
        for (Comma_ConstExp comma_constExp: comma_constExp_list) {
            constExps.add(comma_constExp.constExp);
        }
        return constExps;
    }

    public int[] getArrayValue(int length) { // 初始化的值不一定满足length，其他元素默认初始化为0
        int[] res = new int[length]; // 其他应该默认为0
        ArrayList<ConstExp> constExps = getInitConstExps();
        int i;
        for (i = 0; i < constExps.size(); i++) {
            res[i] = constExps.get(i).getIntValue(); // todo: 数组取值完善【仅限常量数组 --> 变量数组还是通过GEP
        }
        return res;
    }

    public int[] getArrayCharValue(int length) { // 字符数组，不超过128（计算过程i32，结构i8
        int[] res = new int[length]; // 其他应该默认为0
        if (isStringInit) {
            // todo:StringCon待实现
        }
        ArrayList<ConstExp> constExps = getInitConstExps();
        int i;
        for (i = 0; i < constExps.size(); i++) {
            res[i] = (constExps.get(i).getIntValue() % 128);
        }
        return res;
    }

    public static void main(String[] args) {
        int length = 10;
        int[] res = new int[length];
        res[0] = 8;
        res[1] = 18;
        System.out.println(res); // 直接print只能得到地址hash：[I@41629346
        System.out.println(Arrays.toString(res)); // 可以观察到默认赋值为0
    }
}
