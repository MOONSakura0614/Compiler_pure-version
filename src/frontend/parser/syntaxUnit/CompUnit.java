package frontend.parser.syntaxUnit;

import frontend.lexer.Token;
import frontend.symbol.SymbolTable;
import utils.IOUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 郑悦
 * @Description: 根节点，编译单元
 * CompUnit → {Decl} {FuncDef} MainFuncDef // 1.是否存在Decl 2.是否存在FuncDef
 */
public class CompUnit extends SyntaxNode { // 根节点要不要implements SyntaxNode
//    ArrayList<SyntaxNode> children; // 是不是每个节点都应该有？
    // 是否需要把每个语法成分对应的token存起来？？
    ArrayList<Token> tokens; // 关于此语法成分，不涉及递归向下的（向下的到时候按链表往下读取？

    ArrayList<Decl> declList;
    ArrayList<FuncDef> funcDefList;
    MainFuncDef mainFuncDef;

    public CompUnit() {
        super("CompUnit");
//        children = new ArrayList<>();

        // 推导式 蕴含语法成分初始化
        declList = new ArrayList<>();
        funcDefList = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        // Decl FuncDef MainFuncDef
        Decl decl;
        while (isDecl()) {
            // 进入Decl的判断
            decl = new Decl();
            decl.unitParser();
            declList.add(decl);
        }
        FuncDef funcDef;
        while (isFuncDef()) {
            //
            funcDef = new FuncDef();
            funcDef.unitParser();
            funcDefList.add(funcDef);
        }
        // main函数解析
        if (isMainFuncDef()) {
            mainFuncDef = new MainFuncDef();
            mainFuncDef.unitParser();
        } else {
            // 注意SysY要求必须有main函数
            throw new RuntimeException("CompUnit解析失败：无法找到MainFuncDef");
        }
    }

    @Override
    public void print() {
        if (!declList.isEmpty()) {
            for (Decl decl: declList) {
                decl.print();
            }
        }
        if (!funcDefList.isEmpty()) {
            for (FuncDef funcDef: funcDefList) {
                funcDef.print();
            }
        }
        mainFuncDef.print();
        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() { // 深搜：后序遍历AST
        SymbolTable table = initSymbolTable(); // 函数中已经包括add to symbolTableList
//        Visitor.symbolTableList.add(table); // 第一个table，scope=1（全局变量）

        // 为了父节点嵌套
        /*Visitor.curTable = table;
        Visitor.curScope = Visitor.scope;*/ // 在上一步init的时候完成符号表迭代

        // 添加新符号
        // Decl
        for (Decl decl: declList) {
            decl.insertSymbol(table); // 是否需要传入对应的SymbolTable
        }
        // FuncDef
        for (FuncDef funcDef: funcDefList) {
            funcDef.insertSymbol(table);
            // 每个FuncDef有自己的作用域（包括每个Block）
            funcDef.visit();
            // 每个FuncDef内部作用域需要赋值外层符号表（fatherSymbolTable
        }
        // MainFuncDef
        if (mainFuncDef != null) {
            mainFuncDef.visit();
        }
    }

    public static void main(String[] args) {
        CompUnit compUnit = new CompUnit();
        if (compUnit.children != null) {
            System.out.println("new ArrayList so not null");
//            System.out.println("new ArrayList but real null");
            // 错误的，new了就不是null
            // 如果ArrayList<SyntaxNode> children = new ArrayList<>()就不是null，就是实例化了，只有不赋值的最开始才是？
        } else {
            System.out.println("new ArrayList but real null");
        }

        Map<String, String> stuNumMap_unorder = new HashMap<>();
        Map<String, String> stuNumMap_order = new LinkedHashMap<>();
        stuNumMap_unorder.put("sxq", "22373640");
        stuNumMap_order.put("sxq", "22373640");
        stuNumMap_unorder.put("zy", "22373100");
        stuNumMap_order.put("zy", "22373100");
        stuNumMap_unorder.put("rwm", "22373140");
        stuNumMap_order.put("rwm", "22373140");
        stuNumMap_unorder.put("zyh", "22373089");
        stuNumMap_order.put("zyh", "22373089");
        // print
        System.out.println(stuNumMap_order);
        System.out.println(stuNumMap_unorder);
        // result: linkedHashMap维护添加顺序
    }
}
