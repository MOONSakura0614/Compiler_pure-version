package frontend.parser.syntaxUnit;

import frontend.visitor.Visitor;

/**
 * @author 郑悦
 * @Description: 语句块项
 * BlockItem → Decl | Stmt
 */
public class BlockItem extends SyntaxNode {
    private Decl decl;
    private Stmt stmt;
    private Boolean isDecl;

    public BlockItem() {
        super("BlockItem");
        isDecl = Boolean.FALSE;
    }

    @Override
    public void unitParser() {
        if (isDecl()) {
            isDecl = Boolean.TRUE;
            decl = new Decl();
            decl.unitParser();
        } else if (isStmt()) {
            stmt = new Stmt();
            stmt.unitParser();
        }
    }

    @Override
    public void print() {
        if (isDecl) {
            if (decl != null)
                decl.print();
        } else {
            if (stmt != null)
                stmt.print();
        }

        // <BlockItem>也不输出
    }

    @Override
    public void visit() {
        // 遇到Decl就用decl的添加符号
        if (isDecl) {
            if (decl != null)
                decl.insertSymbol(Visitor.curTable);
        } else {
            if (stmt != null)
                stmt.visit();
        }
    }

    public Boolean getIsDecl() {
        return isDecl;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public Boolean isReturn0() {
        if (isDecl) {
            return Boolean.FALSE;
        }
        // 如果是Stmt
        if (stmt == null)
            return Boolean.FALSE;
        return stmt.isReturn0();
    }
}
