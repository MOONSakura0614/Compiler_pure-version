package frontend.parser.syntaxUnit;

import errors.ErrorHandler;
import frontend.lexer.LexType;
import frontend.lexer.Token;
import frontend.symbol.SymbolTable;
import frontend.visitor.Visitor;
import utils.IOUtils;

import java.util.AbstractList;
import java.util.ArrayList;

import static frontend.parser.Parser.lexIterator;

/**
 * @author 郑悦
 * @Description: 语句块
 * Block → '{' { BlockItem } '}'
 */
public class Block extends SyntaxNode {
    private Token lBrace_token;
    private Token rBrace_token;
    private ArrayList<BlockItem> blockItem_list;

    public Block() {
        super("Block");
        blockItem_list = new ArrayList<>();
    }

    @Override
    public void unitParser() {
        BlockItem blockItem;
        if (isArrayInit()) {
            // 函数体有点像BlockItem的list
            if (lexIterator.iterator().hasNext()) {
                lBrace_token = lexIterator.iterator().next();

                while (isBlockItem()) {
                    blockItem = new BlockItem();
                    blockItem.unitParser();
                    blockItem_list.add(blockItem); // 花括号中间可重复多次
                }

                Token token;
                if (lexIterator.iterator().hasNext()) {
                    token = lexIterator.tokenList.get(lexIterator.curPos);
                    if (token.getTokenType() == LexType.RBRACE) {
                        rBrace_token = lexIterator.iterator().next();
                    } else {
                        System.out.println(lexIterator.nowToken().getLineNum() + ":" + lexIterator.nowToken().getTokenType() + lexIterator.nowToken().getTokenValue());
                        throw new RuntimeException("Block解析失败：没有}");
                    }
                }
            }
        }
    }

    @Override
    public void print() {
        if (lBrace_token != null)
            IOUtils.writeCorrectLine(lBrace_token.toString());
        if (!blockItem_list.isEmpty()) {
            for (BlockItem blockItem: blockItem_list) {
                blockItem.print();
            }
        }
        if (rBrace_token != null)
            IOUtils.writeCorrectLine(rBrace_token.toString());

        IOUtils.writeCorrectLine(toString());
    }

    @Override
    public void visit() {
        SymbolTable blockSymTable = initSymbolTable();
        // 遇到blockItem
        for (BlockItem blockItem: blockItem_list) {
            blockItem.visit();
        }
        exitCurScope();
    }

    public void visitInFunc(LexType funcType) {
        // 那么main应该也算一种函数，但是没有新建符号表（所以说明在mainDef中的visit函数就要建新表
        SymbolTable funcSymTable = Visitor.curTable;

        // 注意只有void需要一直判断return exp的错误
        Visitor.inVoidFunc = funcType .equals(LexType.VOIDTK);

        // 按照输入文法规则：判断Return相关的错误↓
        // 应该先是void只需要判断有Exp就是错的
        if (blockItem_list.isEmpty()) {
            // 判断是不是void，不是的话空body的block必报错的
            if (funcType != LexType.VOIDTK)
                ErrorHandler.funcLackReturnValueErrorHandle(rBrace_token !=null ? rBrace_token.getLineNum() : 0);
            /*只需要考虑函数末尾是否存在return语句。
            报错行号为函数结尾的’}’所在行号。*/
        } else {
            BlockItem lastBlockItem = blockItem_list.get(blockItem_list.size() - 1);
            if (lastBlockItem.getIsDecl()) {
                // 如果最后一句是声明
                if (funcType != LexType.VOIDTK)
                    ErrorHandler.funcLackReturnValueErrorHandle(rBrace_token !=null ? rBrace_token.getLineNum() : 0);
            } else {
                Stmt stmt = lastBlockItem.getStmt();
                // 判断stmt是不是return EXP类型
                if (stmt.getChosen_plan().equals(7)) { // 是return类型的
                    if (!funcType.equals(LexType.VOIDTK)) {
                        if (!stmt.getHasReturnExp()) {
                            ErrorHandler.funcLackReturnValueErrorHandle(rBrace_token !=null ? rBrace_token.getLineNum() : 0);
                        }
                    }
                } else {
                    // 没有return语句
                    if (funcType != LexType.VOIDTK)
                        ErrorHandler.funcLackReturnValueErrorHandle(rBrace_token !=null ? rBrace_token.getLineNum() : 0);
                }
            }
        }
        // 不新建符号表，并判断return value是否正确
        for (BlockItem blockItem: blockItem_list) {
            blockItem.visit();
        }

        Visitor.inVoidFunc = Boolean.FALSE;
    }

    public void checkReturn0() {
        if (blockItem_list.isEmpty()) {
            if (rBrace_token == null)
                return;
            ErrorHandler.funcLackReturnValueErrorHandle(rBrace_token.getLineNum()); // 因为没有缺少右括号的错误，所以肯定不为null吧
            return; // 防止下面的get发生越界
        }
        if (!blockItem_list.get(blockItem_list.size() - 1).isReturn0()) {
            if (rBrace_token == null)
                return;
            ErrorHandler.funcLackReturnValueErrorHandle(rBrace_token.getLineNum()); // 因为没有缺少右括号的错误，所以肯定不为null吧
        }
    }

    public ArrayList<BlockItem> getBlockItem_list() {
        return blockItem_list;
    }
}
