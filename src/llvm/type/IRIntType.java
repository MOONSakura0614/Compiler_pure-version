package llvm.type;

/**
 * @author 郑悦
 * @Description: 整数类型是基本类型，数组、函数、指针等类型是组合类型
 * 基本类型就是自己本身；组合类型是使用组合来进行类型之间的嵌套
 * 但是所有类型都继承自Type
 * @date 2024/11/15 18:59
 */
public class IRIntType implements IRType {
    @Override
    public String toString() {
        return "i32";
    }
}
