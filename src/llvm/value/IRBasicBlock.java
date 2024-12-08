package llvm.value;

import llvm.IRGenerator;
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
//        setName("%" + IRGenerator.cur_func.getLocalValRegNum());
        setName(String.valueOf(IRGenerator.cur_func.getLocalValRegNumName())); // 换行符要加吗？
        // TODO: 2024/11/29 虽然暂时没整跳转，但是唯一一个label还是拥有一下自己的虚拟寄存器编号吧~
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
        if (instructions.isEmpty()) {
            return ""; // 如果基本块内部为空，label也可以不用打印！
        }
//        StringBuilder sb = new StringBuilder();
        // label要用basicBlock的编号就是name（但不能带冒号！
        StringBuilder sb = new StringBuilder("\n" + getName() + ":\n");
        // TODO: 2024/11/26 尚未实现基本块的label，只是简单在operands中记录了指令
        for (Instruction instruction: instructions) {
//            System.out.println("BBBBB:inst");
            sb.append("    ");
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
