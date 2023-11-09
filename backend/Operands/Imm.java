package backend.Operands;

public class Imm {
    private int value;

    public Imm(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
