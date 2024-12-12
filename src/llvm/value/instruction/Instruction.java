package llvm.value.instruction;

import llvm.type.IRType;
import llvm.value.IRUser;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 20:24
 */
public class Instruction extends IRUser {
    protected Operator operator;
    protected int handler;

    public Instruction() {
        super();
    }

    public Instruction(Operator operator) {
        super(); // 通过父类的无参构造，记得初始化操作数list
        this.operator = operator;
    }

    public Instruction(Operator operator, String name) {
        super(name);
        this.operator = operator;
    }

    public Instruction(IRType type, Operator operator, String name) {
        super(type, name);
        this.operator = operator;
    }

    public Instruction(IRType type, Operator operator) {
        // 每个指令操作也占一个虚拟寄存器:%2 = load i32, i32* @x, align 4
        // 或者是存储指令结果：%10 = zext i1 %9 to i32  ；%1 = alloca i32, align 4   ；%6 = icmp ne i32 %5, 0
        // %12 = add nsw i32 %11, 1   ；br i1 %3, label %4, label %8   ；%7 = xor i1 %6, true,
        super(type, "%");
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

}
