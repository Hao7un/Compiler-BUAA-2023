package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsLi extends MipsInstruction{
    private Register reg;
    private Imm imm;
    public MipsLi(Register reg, Imm imm) {
        this.reg = reg;
        this.imm = imm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("li");
        sb.append(" ");
        sb.append(reg.toString());
        sb.append(", ");
        sb.append(imm.toString());
        return sb.toString();
    }

}
