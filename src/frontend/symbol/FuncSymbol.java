package frontend.symbol;

import errors.ErrorHandler;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.parser.syntaxUnit.Exp;
import frontend.parser.syntaxUnit.FuncDef;
import frontend.parser.syntaxUnit.FuncFParam;
import frontend.parser.syntaxUnit.FuncRParams;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description: 函数标识符
 * @date 2024/10/17 8:26
 */
public class FuncSymbol extends Symbol {
    private int paramsCount; // 函数的参数个数
//    private ArrayList<FuncParamSymbol> funcParamSymbols; // 函数形参类似变量定义，就统一用varSymbol
//    private ArrayList<VarSymbol> funcFParams;
    private ArrayList<FuncFParam> funcFParams;
    private LexType funcType;
    private FuncDef funcDef;

    public FuncSymbol() {
        // 隐式调用父类的无参数构造器
        funcFParams = new ArrayList<>(); // 按序添加参数
    }

    public FuncSymbol(FuncDef funcDef, String ident_name) { // 要不要拒绝子类，直接用Symbol类（让Symbol类很多成员（不一定使用
        super(ident_name);
        this.funcDef = funcDef;
        funcFParams = new ArrayList<>();
        paramsCount = 0; // 默认为0
    }

    public FuncSymbol(FuncDef funcDef, Token ident_token, int id) { // 要不要拒绝子类，直接用Symbol类（让Symbol类很多成员（不一定使用
        super(ident_token, id);
        this.funcDef = funcDef;
        funcFParams = new ArrayList<>();
        paramsCount = 0; // 默认为0
    }

    public void setFuncType(LexType type) {
        funcType = type;
    }

    public void setParamsCount(int count) {
        paramsCount = count;
    }

    public int getParamsCount() {
        return paramsCount;
    }

    public void setFuncFParams(ArrayList<FuncFParam> fFPs) {
        funcFParams = fFPs;
    }

    public void paramsMatch(FuncRParams funcRParams, int funcCallLine) {
        // 个数不匹配
        if (funcRParams == null) { // 传入实参为0
            if (paramsCount == 0)
                return;
            ErrorHandler.funcParamsNumErrorHandle(funcCallLine);
            return;
        }
        else if (paramsCount != funcRParams.getRParamCount()) {
            ErrorHandler.funcParamsNumErrorHandle(funcCallLine);
            return; // 同行是不是不会多个错误
        }

        int i = 0;
        Exp exp = null;
        ArrayList<Exp> exps = funcRParams.getExps();
        int size = exps.size(); // 就是RealParamsCount
        Symbol fParamSym = null, rParamSym = null; // 形参很可能不在当前作用域下，所以用funcFParam判断即可，但是RParam要取出符号
        // 类型不匹配：重点在于判断数组的传递
        for (FuncFParam funcFParam: funcFParams) {
            if (i >= size) {
                ErrorHandler.funcParamsNumErrorHandle(funcCallLine);
                break;
            }
            exp = exps.get(i);
            // 逐个检查形参类型，进行一一对应
            if (funcFParam.getIsArray()) {
                // 普通Exp应该只能代表单变量，array型无法代表;所以想要是array，必定是符号表中的ident（zy猜想
                if (!exp.isIdentArray()) {
                    ErrorHandler.funcParamsTypeErrorHandle(funcCallLine);
                    return;
                    // 问题在于，charArray和intArray的区分
                } else {
                    // 注意实参调用的时候，应利用RParam中的Exp判断是不是数组中的某一元素
                    rParamSym = exp.getIdentSymbol();
                    if (funcFParam.isInt()) {
                        if (!(rParamSym.getSymbolType().equals(SymbolType.IntArray)
                                || rParamSym.getSymbolType().equals(SymbolType.ConstIntArray))) {
                            ErrorHandler.funcParamsTypeErrorHandle(funcCallLine);
                            return;
                        }
                    } else { // 需要char数组但是传了int[]
                        if (rParamSym.getSymbolType().equals(SymbolType.IntArray)
                                || rParamSym.getSymbolType().equals(SymbolType.ConstIntArray)) {
                            ErrorHandler.funcParamsTypeErrorHandle(funcCallLine);
                            return;
                        }
                    }
                }
            } else {
                // 形参是变量，但是实参是数组
                if (exp.isIdentArray()) {
                    ErrorHandler.funcParamsTypeErrorHandle(funcCallLine);
                    return;
                }
            }
            i++;
        }
    }
}
