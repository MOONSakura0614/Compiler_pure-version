package frontend.parser.syntaxUnit;

/**
 * @author 郑悦
 * @Description: 声明，有两种推导规则
 * Decl → ConstDecl | VarDecl
 */
public class Decl extends SyntaxNode {
    /*Decl declNode;*/

    ConstDecl constDecl;
    VarDecl varDecl;
    Boolean isConst;

    public Decl() {
        super("Decl");
//        isConst = false;
    }

    @Override
    public void unitParser() {
        /*declNode = new Decl();*/
        // 给每个成员变量赋值
        if (isConstDecl()) {
            isConst = Boolean.TRUE;
            constDecl = new ConstDecl();
            constDecl.unitParser();
        } else {
            isConst = Boolean.FALSE;
            varDecl = new VarDecl();
            varDecl.unitParser();
        }
    }

    @Override
    public void print() {
        if (isConst == null) {
            return;
        }
        if (isConst) {
            if (constDecl != null) {
                constDecl.print();
            }
        } else {
            if (varDecl != null) {
                varDecl.print();
            }
        }
    }

    /*public Decl getDeclNode() {
        return declNode;
    }*/

    /*@Override
    void setSyntaxName() {
        syntaxName = "Decl";
    }*/
}
