package llvm.value.instruction.memory;

import llvm.IRGenerator;
import llvm.type.IRArrayType;
import llvm.type.IRIntType;
import llvm.type.IRPointerType;
import llvm.type.IRType;
import llvm.value.IRGlobalVar;
import llvm.value.IRValue;
import llvm.value.constVar.IRConst;
import llvm.value.instruction.Instruction;
import llvm.value.instruction.Operator;

/**
 * @author 郑悦
 * @Description: GetElementPtr指令——数组相关
 * @date 2024/12/5 20:19
 */
public class GEPInst extends Instruction {
    private IRType elementType;
    private IRValue array_pointer; // 对应的数组指针
    private int indice = -1; // 初始化为-1，若是-1则不合法不打印，打印对应的偏移的寄存器
    private IRValue indice_reg;

    /* %2 = getelementptr [20 x i32], [20 x i32]* %1, i32 0, i32 1【这个1就是1维数组存的indice】 */

    public GEPInst(IRValue pointer, int indice) { // 传入的pointer是一维数组的指针，其IRType是IRArrayType
        super(Operator.GEP, "%" + IRGenerator.cur_func.getLocalValRegNumName());
        if (pointer instanceof AllocaInst) {
            // 局部数组
            elementType = ((IRArrayType) ((IRPointerType) pointer.getIrType()).getElement_type()).getElementType();
        } else if (pointer instanceof IRGlobalVar){
            elementType = ((IRArrayType) ((IRGlobalVar) pointer).getIrValue().getIrType()).getElementType();
        } else {
            // todo: Array 是形参中的数组
            elementType = ((IRPointerType) pointer.getIrType()).getElement_type();
        }
        array_pointer = pointer; //
        this.addOperand(pointer);
        this.irType = new IRPointerType(elementType);
        this.indice = indice;
    }

    public GEPInst(IRValue pointer, IRValue indice) { // 传入的pointer是一维数组的指针，其IRType是IRArrayType
        super(Operator.GEP, "%" + IRGenerator.cur_func.getLocalValRegNumName());
        if (pointer instanceof AllocaInst) {
            // 局部数组
            elementType = ((IRArrayType) ((IRPointerType) pointer.getIrType()).getElement_type()).getElementType(); // 取出的元素类型，但是整个GEP指令是个指针，不是元素值
        } else if (pointer instanceof IRGlobalVar){
            // 全局数组
//            elementType = ((IRArrayType) pointer.getIrType()).getElementType();
            // 又包了一层globalVar封装，所以不是直接IRArrayType!需要通过globalVar的成员变量irValue进行索引
            elementType = ((IRArrayType) ((IRGlobalVar) pointer).getIrValue().getIrType()).getElementType();
        } else {
            // todo: Array 是形参中的数组 --> 使用loadInst保存的地址
            elementType = ((IRPointerType) pointer.getIrType()).getElement_type();
        }
        array_pointer = pointer;
        this.addOperand(pointer);
        this.irType = new IRPointerType(elementType);
        this.indice_reg = indice;
    }

    // TODO: 2024/12/9 留下的拓展接口：支持高维数组（有多高维就传多少偏移量参数）
    /* <result> = getelementptr <ty>, <ty>* <ptrval>, [inrange] <ty> <idx>
        其中各个部分的含义如下：
        <ty>：目标类型，表示数组或结构体的元素类型。
        <ptrval>：指向目标类型的指针。
        <idx>：索引，用于指定访问的元素在数组或结构体中的位置，支持多个维度。*/
    /* 实例：(1) 一维数组
        对于一维数组，getelementptr 用来访问数组的元素。假设我们有一个一维数组，定义为：
        %array = alloca [10 x i32], align 4
        这是一个包含 10 个 i32 类型元素的数组，alloca 分配了栈上的内存。此时，%array 是一个指向 [10 x i32] 类型的指针。
        要访问数组中的第 i 个元素，使用 GEP 指令： %element = getelementptr [10 x i32], [10 x i32]* %array, i32 0, i32 i
        第一个 i32 0 表示在数组的第一个维度（即数组本身）上偏移 0，表示选择数组的起始地址。
        第二个 i32 i 表示在数组的第二个维度上，根据索引 i 来选择数组的元素。*/
    /* 实例*：(3) 高维数组
        对于更高维度的数组，例如三维数组 [3 x [4 x [5 x i32]]]，GEP 指令会继续增加维度偏移：
        %array3d = alloca [3 x [4 x [5 x i32]]], align 4
        这是一个 3x4x5 的三维数组。访问三维数组的第 i 行、第 j 列、第 k 层的元素：
        %element = getelementptr [3 x [4 x [5 x i32]]], [3 x [4 x [5 x i32]]]* %array3d, i32 0, i32 i, i32 j, i32 k
        第一个 i32 0 表示选择数组的起始地址。
        第二个 i32 i 表示在第 i 行偏移。
        第三个 i32 j 表示在第 j 列偏移。
        第四个 i32 k 表示在第 k 层偏移。*/
    /*public GEPInst(IRBasicBlock basicBlock, IRValue pointer, List<IRValue> indices) { // 这里之所以传IRValue的list难道是因为，比如二维数组是一维数组的数组这样？（但是也不用list，只要一维数组做元素就行了吧？？
        super(new IRPointerType(getElementType(pointer, indices)), Operator.GEP, basicBlock);
        this.setName("%" + IRGenerator.cur_func.getLocalValRegNumName());
        if (pointer instanceof GEPInst) {
            target = ((GEPInst) pointer).target;
        } else if (pointer instanceof AllocaInst) {
            target = pointer;
        } else if (pointer instanceof IRGlobalVar) {
            target = pointer;
        }
        this.addOperand(pointer);
        for (IRValue value : indices) {
            this.addOperand(value);
        }
        this.elementType = getElementType(pointer, indices);
    }

    private static IRType getElementType(IRValue pointer, List<IRValue> indices) {
        IRType type = pointer.getIrType();
        for (IRValue ignored : indices) {
            if (type instanceof IRArrayType) {
                type = ((IRArrayType) type).getElementType();
            } else if (type instanceof IRPointerType) {
                type = ((IRPointerType) type).getTargetType();
            } else {
                break;
            }
        }
        return type;
    }

    public GEPInst(IRBasicBlock basicBlock, IRValue pointer, int offset) {
        this(basicBlock, pointer, ((IRArrayType) ((IRPointerType) pointer.getIrType()).getTargetType()).offset2Index(offset));
        // 方法在IRArrayType里
//        public List<Value> offset2Index(int offset) {
//            List<Value> index = new ArrayList<>();
//            Type type = this;
//            while (type instanceof ArrayType) {
//                index.add(new ConstInt(offset / ((ArrayType) type).getCapacity()));
//                offset %= ((ArrayType) type).getCapacity();
//                type = ((ArrayType) type).getElementType();
//            }
//            index.add(new ConstInt(offset));
//            return index;
//        }
    }

    public List<Integer> getGEPIndex() {
        List<Integer> index = new ArrayList<>();
        for (int i = 1; i < getOperands().size(); i++) {
            index.add(((IRConstInt) getOperand(i)).getValue());
        }
        return index;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getName()).append(" = getelementptr ");
        // 如果是字符串，需要加 inbounds
        if (getPointer().getIrType() instanceof IRPointerType && ((IRPointerType) getPointer().getIrType()).isString()) {
            s.append("inbounds ");
        }
        s.append(((IRPointerType) getPointer().getIrType()).getTargetType()).append(", ");
        for (int i = 0; i < getOperands().size(); i++) {
            if (i == 0) {
                s.append(getPointer().getIrType()).append(" ").append(getPointer().getName());
            } else {
                s.append(", ").append(getOperands().get(i).getType()).append(" ").append(getOperands().get(i).getName());
            }
        }
        return s.toString();
    }

    private static Type getElementType(Value pointer, List<Value> indices) {
        Type type = pointer.getType();
        for (Value ignored : indices) {
            if (type instanceof ArrayType) {
                type = ((ArrayType) type).getElementType();
            } else if (type instanceof PointerType) {
                type = ((PointerType) type).getTargetType();
            } else {
                break;
            }
        }
        return type;
    }*/

    public IRValue getPointer() {
        return getOperandByIndex(0); // 没有高维，其实就是第一个操作数
    }

    public IRType getElementType() {
        return elementType;
    }

    @Override
    public String toString() { // 只会在函数中才有取元素的操作，所以一定是regName：
        StringBuilder s = new StringBuilder();
        s.append(getName()).append(" = getelementptr ");
        // 如果是字符串，需要加 inbounds
        if (getPointer().getIrType() instanceof IRPointerType && ((IRPointerType) getPointer().getIrType()).isString()) {
            s.append("inbounds ");
        }
        s.append(((IRPointerType) array_pointer.getIrType()).getElement_type()).append(", "); // alloca的类型也是[len x i32/i8]*，这里只需要大小
        s.append(array_pointer.getIrType()).append(" ").append(array_pointer.getName()).append(", "); // 指针元素:只需要地址的regName，所以alloca和globalVar是一样的
        /*if (indice != -1) {
            s.append(elementType).append(" 0, "); // 如i32 0或者i8 0的第一个默认indice，偏移基准
            s.append(elementType).append(" ").append(indice); // 自身偏移取值（只有一维数组，就不冗余实现List<Int> indices了
        } else {
            *//* todo: Array 这里有可能传常量，导致少了上面的第一个i32 0，变成了取数组地址[number x i32]*类型，而不是i32*(用寄存器的时候才能只用一个) *//*
            *//* todo: Array 如果是从形参load出来的数组，那么得到的是指针！不是整个数组，所以GEP只需要一个索引！不需要两个 *//*
            if (indice_reg instanceof IRConst && !(array_pointer instanceof LoadInst)) {
                s.append(elementType).append(" 0, ");
            }
            s.append(elementType).append(" ").append(indice_reg.getName());
        }*/
        if (!(array_pointer instanceof LoadInst)) {
            /* todo: 疑似不是指针的就是代表原始数组（全局或者局部alloca的）==>不是形参指针load出来的都要有最前面的i32基准 */
//            s.append(elementType).append(" 0, ");
            // TODO: 2024/12/13 偏移只能都是i32
            s.append(IRIntType.intType).append(" 0, "); // 如i32 0或者i8 0的第一个默认indice，偏移基准
        }
        if (indice != -1) {
//            s.append(elementType).append(" ").append(indice); // 自身偏移取值（只有一维数组，就不冗余实现List<Int> indices了
            s.append(IRIntType.intType).append(" ").append(indice);
        } else {
            // indice_reg需要保证是i32，在外面可以需先convInst
//            s.append(elementType).append(" ").append(indice_reg.getName());
            s.append(IRIntType.intType).append(" ").append(indice_reg.getName());
        }
        return s.toString();
    }
}
