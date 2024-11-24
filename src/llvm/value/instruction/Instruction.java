package llvm.value.instruction;

import llvm.value.IRUser;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 20:24
 */
public class Instruction extends IRUser {
    protected Operator operator;
    protected int handler;
}
