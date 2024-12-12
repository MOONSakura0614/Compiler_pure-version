package llvm.value.constVar;

import com.sun.jdi.Value;
import llvm.type.IRArrayType;
import llvm.type.IRType;
import llvm.value.IRValue;

import java.util.List;

/**
 * @author 郑悦
 * @Description: 感觉像Array Initializor 不一定是Const，只是初始化存
 * [符号表中对应的IRValue，全局存这个名字@name，局部申请的数组地址，直接存alloca命令的reg]
 * 只是init Array，所以全局非常量数组也可以用，其中的setElementValue只是用于初始化置值的
 * @date 2024/11/15 22:51
 */
public class IRConstArray extends IRConst {
    private IRType elementType;
    private List<IRValue> array; // 这里可以是向下递归的（比如IRValue用IRConstInt就是一维，还可以用IRConstArray达到高维效果
    private Boolean isInit = Boolean.FALSE;
    private int length; // 数组容量capacity
    private int[] arrayVal;

    public IRConstArray(IRType type, String name, int capacity) {
        super(type, name);
        this.elementType = ((IRArrayType) type).getElementType();
        this.length = capacity;
    }

    public IRConstArray(IRType type, String name, int[] initVals) {
        super(type, name);
        this.elementType = ((IRArrayType) type).getElementType();
        arrayVal = initVals;
        this.length = initVals.length;
    }

    /*public void setElementValue(int offset, IRValue value) {
    }*/

    public void setElementValue(int index, int value) { // char和int数组的元素都用i32存具体的值
        if (index < arrayVal.length) {
            arrayVal[index] = value;
        } else {
            throw new RuntimeException("数组元素赋值越界");
        }
    }

    public Boolean isInit() {
        return isInit;
    }

    public void setArrayVal(int[] arrayVal) {
        this.arrayVal = arrayVal;
    }

    public boolean isAllZero() {
        // 每次都遍历，就不用检查后期的元素赋值改变？
        for (int i: arrayVal) {
            if (i != 0) {
                return false;
            }
        }
        return true;
    }

    /*public boolean allZero() { // 递归解决高维数组问题
        for (Value value : array) {
            if (value instanceof ConstInt) {
                if (((ConstInt) value).getValue() != 0) {
                    return false;
                }
            } else {
                if (!((ConstArray) value).allZero()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void storeValue(int offset, Value value) {
        // recursion
        if (elementType instanceof ArrayType) {
            ((ConstArray) (array.get(offset / ((ArrayType) elementType).getCapacity()))).storeValue(offset % ((ArrayType) elementType).getCapacity(), value);
        } else {
            array.set(offset, value);
        }
    }*/

    @Override
    public String toString() { // 这里只用于全局变量
        // 局部变量时alloca数组对应的irType（大小[ x ]），之后分别store（这里应该可以简化bitcast数组地址到i8*再call memset初始化0，再转<{...回i32}>*的操作？
        // 只有全局变量声明的时候才打印数组（申请的类型和元素值）；局部变量直接打印alloca指令和GEP赋值
        /* @zz = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0], align 4 */
        if (isAllZero()) { // 注意常量数组一定有初始化
            return this.getIrType() + " " + "zeroinitializer";
        } else {
            StringBuilder sb = new StringBuilder();
//            sb.append(this.getIrType()).append(" ").append(arrayVal); // 注意初始化构造IRConstArray的时候都提前赋值
            // 不能像上面一样直接打印数组[ , , ]虽然也是一致格式，但是少了元素的类型
            sb.append(irType).append(" [");
            for (int i = 0; i < arrayVal.length; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(elementType).append(" ").append(arrayVal[i]); // 注意可能是i8，但是store的时候存值使用ascii码，存和解析的参数不超过256即可
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    // 局部数组
    /*%2 = alloca [20 x i32], align 4
      %3 = alloca [5 x i8], align 1
      store i32 0, i32* %1, align 4
      call void @llvm.dbg.declare(metadata [20 x i32]* %2, metadata !33, metadata !DIExpression())
      %4 = bitcast [20 x i32]* %2 to i8*
      call void @llvm.memset.p0i8.i32(i8* align 4 %4, i8 0, i32 80, i1 false)
      %5 = bitcast i8* %4 to <{ i32, i32, i32, i32, i32, [15 x i32] }>*
      %6 = getelementptr inbounds <{ i32, i32, i32, i32, i32, [15 x i32] }>, <{ i32, i32, i32, i32, i32, [15 x i32] }>* %5, i32 0, i32 0
      store i32 1, i32* %6, align 4
      %7 = getelementptr inbounds <{ i32, i32, i32, i32, i32, [15 x i32] }>, <{ i32, i32, i32, i32, i32, [15 x i32] }>* %5, i32 0, i32 1
      store i32 2, i32* %7, align 4
      %8 = getelementptr inbounds <{ i32, i32, i32, i32, i32, [15 x i32] }>, <{ i32, i32, i32, i32, i32, [15 x i32] }>* %5, i32 0, i32 2
      store i32 3, i32* %8, align 4
      %9 = getelementptr inbounds <{ i32, i32, i32, i32, i32, [15 x i32] }>, <{ i32, i32, i32, i32, i32, [15 x i32] }>* %5, i32 0, i32 3
      store i32 100023, i32* %9, align 4
      %10 = getelementptr inbounds <{ i32, i32, i32, i32, i32, [15 x i32] }>, <{ i32, i32, i32, i32, i32, [15 x i32] }>* %5, i32 0, i32 4
      store i32 3441, i32* %10, align 4
      */
}
