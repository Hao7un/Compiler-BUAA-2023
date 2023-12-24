package backend.Operands;

public class Imm {
    private int value;

    public Imm(int value) {
        this.value = value;
    }


    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
