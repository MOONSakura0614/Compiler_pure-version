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

    public boolean insertSymbol(Symbol symbol) {
        if (symbolMap == null)
            return false;
        if (symbolMap.containsKey(symbol.identName)) {
            if (symbol.identToken != null)
                ErrorHandler.redefineErrorHandle(symbol.identToken.getLineNum());
            return false;
        }
        else
            symbolMap.put(symbol.identName, symbol);
        return true;
    }
    
    public void insertIRSymbol(Symbol symbol) {
        // 注意：进入中间代码生成阶段，默认词法、语法、语义合规——不判断是否有
        // IR阶段的符号表更适合存全局变量和函数——局部变量如何判断使用？？
        // 常量需要计算出对应的数字/字母
        // 字符串的翻译和声明是不是应该移到llvm ir的最前面
        // 全局是@name形式，其他的%4之类代表的局部变量，记录的应该是store的对应指针还是load出的值？
        if (symbolMap == null)
            return;
        symbolMap.put(symbol.identName, symbol);
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
        }

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
            return;
        }
        for (Map.Entry<String, Symbol> entry: symbolMap.entrySet()) {
            IOUtils.writeSymbol(String.valueOf(scope) + ' ' + entry.getKey() + ' ' + entry.getValue().symbolType + '\n');
        }
    }
}
