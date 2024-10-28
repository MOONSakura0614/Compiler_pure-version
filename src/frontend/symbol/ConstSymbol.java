package frontend.symbol;

import frontend.lexer.Token;
import frontend.parser.syntaxUnit.ConstDef;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/10/24 11:45
 */
public class ConstSymbol extends Symbol {
    private ConstDef constDef;
    private String constInitVal;
    private Boolean isChar; // 默认为const int
    private Boolean isArray; // 默认false, dimension = 0为变量；1为数组
//    private ConstExp constExp; // 数组大小 —— q其实constDef就包含了？
    // 要不要直接存数组大小（ConstExp转化出的值）

    static {
//        this.isConst =  // 由于不是类静态变量，只能在构造器里初始化
    }

    public ConstSymbol(ConstDef constDef, String ident_name) {
        super(ident_name);
        this.constDef = constDef;
        isConst = true;
    }

    public ConstSymbol(ConstDef constDef, Token ident_token, int id) {
        super(ident_token, id);
        this.constDef = constDef;
        isConst = true;
    }

    /*public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }*/
}
