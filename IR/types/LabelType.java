package IR.types;

public class LabelType implements ValueType {
    /*
    * 标签类型，用作代码标签
    * */
    private static int counter;  // 记录label个数

    private final int labelCount;

    public LabelType() {
        this.labelCount = counter++;
    }

    @Override
    public String toString() {
        return "label_" + labelCount;
    }

    @Override
    public int getSize() {
        /*Should not reach here*/
        return -1;
    }
}
