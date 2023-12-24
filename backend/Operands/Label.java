package backend.Operands;

public class Label {
    private String labelName;
    public Label(String labelName) {
        this.labelName = labelName;
    }

    @Override
    public String toString() {
        return labelName;
    }
}
