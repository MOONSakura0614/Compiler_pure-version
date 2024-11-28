package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.symbol.FuncSymbol;
import frontend.symbol.SymbolTable;
import frontend.symbol.VarSymbol;
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
    private int paramCount;

    public FuncFParams() {
        super("FuncFParams");
        comma_fParam_list = new ArrayList<>();
        paramCount = 0;
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

    public int getParamCount() {
        if (paramCount != 0)
            return paramCount;

        if (funcFParams != null) {
            paramCount = funcFParams.size();
            return paramCount;
        }

        // 应该只会调用一次，不会重复计算
        // 不想改语法分析：不在unitParse中统计形参数量和类型
        if (funcFParam != null)
            paramCount = 1;
        if (comma_fParam_list != null && !comma_fParam_list.isEmpty())
            paramCount += comma_fParam_list.size();
        return paramCount;
    }

    private ArrayList<FuncFParam> funcFParams;

    public void setFuncFParams() {
        funcFParams = new ArrayList<>();
        if (funcFParam != null)
            funcFParams.add(funcFParam);
        for (Comma_FParam comma_fParam: comma_fParam_list) {
            if (comma_fParam.fParam != null)
                funcFParams.add(comma_fParam.fParam);
        }
    }

    public ArrayList<FuncFParam> getFParamsDetail() {
        if (funcFParams != null)
            return funcFParams;
        setFuncFParams();

        return funcFParams;
    }

    public void implFFPsSymbolDetail(FuncSymbol funcSymbol) {
        funcSymbol.setFuncFParams(getFParamsDetail()); // 形参个数为0也不会是null，因为new过
        funcSymbol.setParamsCount(getParamCount());
    }

    @Override
    public void insertSymbol(SymbolTable symbolTable) {
        if (funcFParams == null)
            setFuncFParams();

        VarSymbol fParamSymbol;
        for (FuncFParam fParam: funcFParams) {
            fParam.insertSymbol(symbolTable);
        }
    }

    public ArrayList<LexType> getArgTypes() {
        ArrayList<LexType> types = new ArrayList<>();
        if (funcFParam == null)
            return types; // 空集合

        types.add(funcFParam.getVarType());
        for (Comma_FParam comma_fParam: comma_fParam_list) {
            types.add(comma_fParam.fParam.getVarType());
        }
        return types;
    }

    public ArrayList<String> getIdentNames() {
        ArrayList<String> names = new ArrayList<>();
        if (funcFParam == null)
            return names;

        names.add(funcFParam.getIdentName());
        for (Comma_FParam comma_fParam: comma_fParam_list) {
            names.add(comma_fParam.fParam.getIdentName());
        }
        return names;
    }
}
