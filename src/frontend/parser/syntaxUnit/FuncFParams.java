package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import utils.IOUtils;

import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 函数形参表
 * FuncFParams → FuncFParam { ',' FuncFParam }
 */
public class FuncFParams extends SyntaxNode {
    // 函数无参数就无形参表，有形参表至少有一个参数
    private FuncFParam funcFParam;
    private ArrayList<Comma_FParam> comma_fParam_list;

    public FuncFParams() {
        super("FuncFParams");
        comma_fParam_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        if (isFParam()) {
            funcFParam = new FuncFParam();
            funcFParam.unitParser();
        }
        Token token;
        Comma_FParam comma_fParam;
        FuncFParam funcFParam1;
        while (isComma()) {
            if (lexIterator.iterator().hasNext()) {
                // 先把逗号解析出来
                token = lexIterator.iterator().next();
                if (isFParam()) {
                    funcFParam1 = new FuncFParam();
                    funcFParam1.unitParser();
                    comma_fParam = new Comma_FParam(token, funcFParam1);
                    comma_fParam_list.add(comma_fParam);
                } else {
                    throw new RuntimeException("没有下一个FuncFParam，多余逗号");
                }
            }
        }
    }

    @Override
    public void print() {
        if (funcFParam != null)
            funcFParam.print();
        if (!comma_fParam_list.isEmpty()) {
            for (Comma_FParam comma_fParam: comma_fParam_list) {
                comma_fParam.print();
            }
        }

        IOUtils.writeCorrectLine(toString());
    }

    public class Comma_FParam {
        private Token comma;
        private FuncFParam fParam;

        public Comma_FParam(Token token, FuncFParam funcFParam) {
            comma = token;
            fParam = funcFParam;
        }

        public void print() {
            if (comma != null) {
                IOUtils.writeCorrectLine(comma.toString());
            }
            if (fParam != null) {
                fParam.print();
            }
        }
    }
}
