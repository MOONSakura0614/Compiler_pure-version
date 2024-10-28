package frontend.symbol;

import frontend.lexer.Token;
import frontend.parser.syntaxUnit.Decl;
import frontend.parser.syntaxUnit.FuncDef;
import frontend.parser.syntaxUnit.SyntaxNode;

/**
 * @author 郑悦
 * @Description: 符号表管理中识别出的符号
 * @date 2024/10/16 22:31
 */
public class Symbol {
    /*public enum Type {
        ConstChar,
        ConstInt,
        ConstCharArray,
        ConstIntArray,
        Char,
        Int,
        CharArray,
        IntArray,
        VoidFunc,
        CharFunc,
        IntFunc
    }*/

    // 符号：主要是识别标识符
//    protected int id;
    protected int tableId; // 当前单词所在的符号表编号（多个符号表？）：是不是作用域序号
    protected String identName;
    // 由于错误处理（重定义）需要lineNum
    protected Token identToken;
    // 注意，作为父类给子类继承使用的话，只能protect
//    protected Type type;
    protected Boolean isConst;
//    protected LexType bType; // int, char ————> 直接用symbolType表示了
    protected Boolean isArray; // 注意SysY最多数组只支持一维
//    protected static Map<LexType, Type> typeMap = new HashMap<>();
    protected SymbolType symbolType;
    // TODO: 2024/10/26 是否需要将Exp表示的值计算出来，防止数组越界？

    /*static {
        // 用于init静态成员变量
        typeMap.put(LexType.CHRCON, Type.ConstChar);
        typeMap.put(LexType.INTCON, Type.ConstInt);
        typeMap.put(LexType.CHARTK, Type.Char); // 不对啊，应该是一整条decl才可以判断
        // symbol对应的token只是LexType.IDENFR
    }*/

    public Symbol() {
//        type = Type.IntFunc;
        symbolType = SymbolType.Int;
    }

    public Symbol(String ident_name) {
        identName = ident_name;
        isArray = Boolean.FALSE;
        symbolType = SymbolType.Int;
    }

    public Symbol(Token ident_token, int id) {
        this.identToken = ident_token;
        isArray = Boolean.FALSE;
        symbolType = SymbolType.Int;
        tableId = id; // 感觉每个符号还是有自己的id比较好（区分不同作用域的同名符号）
        // 注意不能让string做符号表key的标识符为null
        if (ident_token != null)
            identName = ident_token.getTokenValue();
    }

    public Symbol(SyntaxNode node) { // Decl 或 FuncDef
        // 在MainFuncDef之前 或者在其中的Block中作为一个blockItem（不是另一个Stmt）
        // declaim<Decl>中会出现新的标识符
        // 注意Decl中可能有多条VarDef / ConstDef  ……所以还是传入都市Def的稳妥一点吧（就是会丢失类型？？？
        if (!(node instanceof Decl || node instanceof FuncDef)) {
            throw new RuntimeException("创建符号失败，不应传入" + node.getSyntaxName() + "语法成分进行创建符号");
        }
        symbolType = SymbolType.Int;
//        type = Type.Int;
    }

    public void setSymbolType(SymbolType type) {
        this.symbolType = type;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public boolean isConstSymbol() {
        return symbolType.equals(SymbolType.ConstChar) || symbolType.equals(SymbolType.ConstInt)
                || symbolType.equals(SymbolType.ConstCharArray) || symbolType.equals(SymbolType.ConstIntArray);
    }

    public boolean isFunc() {
        return symbolType.equals(SymbolType.CharFunc) || symbolType.equals(SymbolType.IntFunc) || symbolType.equals(SymbolType.VoidFunc);
    }

    public void setIsArray() { // 默认false，调用就是设为true
        isArray = Boolean.TRUE;
    }

    public Boolean getIsArray() {
        return isArray;
    }

    // 对于一个Decl中多条Def：逐个分析

    // 函数符号需要返回值等多种选项

    /*@Override
    public String toString() {
        // enum的类直接打印就是enum的类名
        return tableLd + ' ' + identName + ' ' + symbolType + '\n';
    }*/

    public boolean match(String name) {
        return name.equals(identName);
    }

    public static void main(String[] args) {
//        System.out.println("www".equals(null));
        System.out.println(new Symbol().symbolType);

        SymbolTable table = new SymbolTable();
        table.insertSymbol(new Symbol("09"));
        System.out.println(table.isSymbolExist(null));
    }
}
