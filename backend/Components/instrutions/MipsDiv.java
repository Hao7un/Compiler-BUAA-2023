package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsDiv extends MipsInstruction {

    private Register rs;
    private Register rt;
    public MipsDiv(Register rs, Register rt) {
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("div");
        sb.append(" ");
        sb.append(rs.toString());
        sb.append(", ");
        sb.append(rt.toString());
        return sb.toString();
    }
}
