package llvm.value.instruction;

/**
 * @author 郑悦
 * @Description:
 * @date 2024/11/15 20:24
 */
public enum Operator {
    Add, Sub, Mul, Div, Mod, And, Or, // 二元运算
    Lt, Le, Ge, Gt, Eq, Ne, // 关系运算符
    Zext, Bitcast, // 类型转换
    Alloca, Load, Store, GEP, // 内存操作
    Phi, MemPhi, LoadDep, // Phi指令
    Br, Call, Ret, // 跳转指令
    Not // 非运算符-一元4
}
