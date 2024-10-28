package frontend.symbol;

import errors.ErrorHandler;
import utils.IOUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 郑悦
 * @Description: 符号表——注意后面生成中间代码/LLVM使用
 * @date 2024/10/17 10:39
 */
public class SymbolTable {
    private int scope; // 符号表的作用域 编号
    private SymbolTable fatherTable; // pre指针，指向逻辑外层
    // 在这里疑惑，直接用引用对象持有，还是说用在Tables的list中的index
    private Map<String, Symbol> symbolMap; // 当前符号表中的symbol
    // 注意输出的时候要按照添加顺序（或者说在代码中出现的先后顺序？行号也不靠谱的感觉，同行的多个VarDef

    public SymbolTable() {
        symbolMap = new LinkedHashMap<>();
        scope = 0; // 防止未初始化（但是非对象应该默认0了，也不会null
    }
    /*public SymbolTable(int current_scope) { // 实际上curScope并没有增加
        current_scope++;
        scope = current_scope;
        //关于作用域序号，即进入该作用域之前进入的作用域数量加1。
        //进入全局作用域时进入的作用域数量为0，因此全局作用域序号为1。

        symbolMap = new HashMap<>();
    }*/

    public boolean insertSymbol(Symbol symbol) {
//        System.out.println("insertSym");
        // 检查符号重定义错误——这一步在遍历AST的时候就处理了，现在可以直接插入
        if (symbolMap == null)
            return false;
        if (symbolMap.containsKey(symbol.identName)) {
            if (symbol.identToken != null)
                ErrorHandler.redefineErrorHandle(symbol.identToken.getLineNum());
            /*if (symbolTable.isSymbolExist(ident_name)) {
                // 重定义-错误处理
                ErrorHandler.redefineErrorHandle(this.ident_token.getLineNum());
                return;
            }*/
            return false;
        }
        else
            symbolMap.put(symbol.identName, symbol);
        return true;
    }

    // 感觉如果查询符号方法放在这，可能更像树状：一层层指针；所以外层查询放Visitor
    public Symbol findInCurSymTable(String value) {
        SymbolTable curTable = this;
        if (symbolMap != null) {
            if (symbolMap.get(value) != null) {
                return symbolMap.get(value);
            } else {
                curTable = curTable.fatherTable;
                while (curTable != null) {
                    if (curTable.symbolMap.containsKey(value))
                        return curTable.symbolMap.get(value);
                    else
                        curTable = curTable.fatherTable;
                }
            }
//            return symbolMap.get(value); // 没找到键值对应该会返回null
        }

        // return null基本就是符号未定义的错误
        // error handle
        return null;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public int getScope() {
        return scope;
    }

    public void setFatherTable(SymbolTable symbolTable) {
        fatherTable = symbolTable;
    }

    public SymbolTable getFatherTable() {
        return fatherTable;
    }

    public boolean isSymbolExist(String identValue) { // 按照标识符查询
        if (symbolMap != null)
            return symbolMap.containsKey(identValue);
        return true; // 在map未初始化的时候肯定不存在，但未初始化就是一种错误，不会出现
    }

    public boolean isSymbolTableEmpty() {
        return symbolMap == null || symbolMap.isEmpty();
    }

    public void print() {
        if (symbolMap == null || symbolMap.isEmpty()) {
//            System.out.println("null");
            return;
        }
        for (Map.Entry<String, Symbol> entry: symbolMap.entrySet()) {
//            IOUtils.writeSymbol(String.valueOf(scope));
            IOUtils.writeSymbol(String.valueOf(scope) + ' ' + entry.getKey() + ' ' + entry.getValue().symbolType + '\n');
//            System.out.println(String.valueOf(scope) + ' ' + entry.getKey() + ' ' + entry.getValue().symbolType + '\n');
//            System.out.println(scope);
        }
    }

    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();
        map.put("smile", 1);
        System.out.println(map.get("oop"));
        System.out.println(map.get("smile"));

        {
            int a = 0;
        }
//        a =  --> 一个block一个作用域


        SymbolTable table = new SymbolTable();
        table.insertSymbol(new Symbol("09"));
        System.out.println(table.symbolMap.containsKey(null)); // 不管是exist方法，还是containsKey对null都是false
        // 除非在map中投入了key为null?
        table.insertSymbol(new Symbol((String) null)); // 不行传入null（重载方法无法匹配参数,除非强转）
        System.out.println(table.symbolMap.containsKey(null)); // 由于上面那一行，就变成true
    }
}
