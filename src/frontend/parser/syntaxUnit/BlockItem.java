package frontend.parser.syntaxUnit;

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
}
