package frontend.symbol;

import frontend.lexer.Token;
import frontend.parser.syntaxUnit.VarDef;

/**
 * @author 郑悦
 * @Description: 暂时将数组和单个变量都统一在varSymbol中
 * @date 2024/10/24 11:45
 */
public class VarSymbol extends Symbol {
    VarDef varDef;

    public VarSymbol(VarDef varDef, String ident_name) {
        super(ident_name);
        this.varDef = varDef;
    }

    public VarSymbol(VarDef varDef, Token ident_token, int id) {
        super(ident_token, id);
        this.varDef = varDef;
    }

    public VarSymbol(Token ident_token, int id) {
        // 服务于函数形参表的插表构造符号：FParam ——> bType + ident
        super(ident_token, id);
    }
}
