package midend.types;

public class ArrayType implements ValueType {
    private int intContains;
    private ValueType elementType;
    private int elementsNumber;
    public ArrayType(ValueType elementType, int elementsNumber) {
        this.elementType = elementType;
        this.elementsNumber = elementsNumber;
        if (elementType instanceof IntegerType) {
            /* [2 x i32]*/
            intContains = elementsNumber;
        } else {
            /* [2 x [2 x i32]]*/
            intContains = ((ArrayType) elementType).intContains * elementsNumber;
        }
    }

    public ValueType getElementType() {
        return elementType;
    }

    public int getIntContains() {
        return intContains;
    }

    public int getElementsNumber() {
        return getElementsNumber();
    }

    @Override
    public String toString() {
        return "[" + elementsNumber + " x " + elementType.toString() + "]";
    }

    @Override
    public int getSize() {
        return elementType.getSize() * elementsNumber;
    }
}
