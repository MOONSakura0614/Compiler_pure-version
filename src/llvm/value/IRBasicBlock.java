package llvm.value;

import frontend.symbol.Symbol;
import llvm.value.instruction.Instruction;

import java.util.ArrayList;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/13 22:56
 */
public class IRBasicBlock extends IRValue {
    private IRBasicBlock prevBlock;
    private IRBasicBlock nextBlock;
    private IRFunction inFunc;
    ArrayList<Instruction> instructions;
//    private int reg_num = 0; // 函数内部的
    // 或者统一用irFunction（cur_func）中的reg_num

    public IRBasicBlock(IRFunction function) {
        inFunc = function;
//        reg_num = function.getReg_num() + 1;
        instructions = new ArrayList<>();
    }

    public void setPrevBlock(IRBasicBlock prevBlock) {
        this.prevBlock = prevBlock;
    }

    public void setNextBlock(IRBasicBlock nextBlock) {
        this.nextBlock = nextBlock;
    }

    public void addInst(Instruction instruction) {
        instructions.add(instruction);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // TODO: 2024/11/26 尚未实现基本块的label，只是简单在operands中记录了指令
        for (Instruction instruction: instructions) {
//            System.out.println("BBBBB:inst");
            sb.append(' ');
            sb.append(instruction);
            sb.append('\n');
        }
        return sb.toString();
    }

    public Instruction getLastInst() {
        // 基本块的最后一句指令（可用于补充void func没有显示生成ret void语句）
        if (instructions.isEmpty())
            return null;
        return instructions.get(instructions.size() - 1);
    }
}
