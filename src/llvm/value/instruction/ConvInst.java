package llvm.value.instruction;

import llvm.value.IRValue;
import llvm.value.instruction.terminator.CallInst;

/**
 * @author 郑悦
 * @Description: 类型转化指令，i32 <-> i8
 * @date 2024/11/26 2:56
 */
public class ConvInst extends Instruction {

    // TODO: 2024/11/26 还没写到基本块：没用跳转和循环语句（除了ret
    public ConvInst(Operator op, IRValue irValue) {
        super(op);
        //
    }
}
