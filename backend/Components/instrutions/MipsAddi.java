package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsAddi extends MipsInstruction {

    private Register rs;
    private Register rt;
    private Imm imm;
    public MipsAddi(Register rs, Register rt , Imm imm) {
        this.rs = rs;
        this.rt = rt;
        this.imm = imm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("addi");
        sb.append(" ");
        sb.append(rt.toString());
        sb.append(", ");
        sb.append(rs.toString());
        sb.append(", ");
        sb.append(imm.toString());
        return sb.toString();
    }
}