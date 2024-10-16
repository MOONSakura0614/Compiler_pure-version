package frontend.parser.syntaxUnit;

import frontend.lexer.LexType;
import frontend.lexer.Token;
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
    private AbstractList<BlockItem> blockItem_list;

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
}
