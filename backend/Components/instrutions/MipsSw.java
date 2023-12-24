package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsSw extends MipsInstruction{

    private Register rs; //base
    private Register rt;
    private Imm offset;

    public MipsSw(Register rs,Register rt,Imm offset) {
        this.rs = rs;
        this.rt = rt;
        this.offset = offset;
        if (rt == null) {
            rt = rt;
        }
    }

    @Override
    public String toString() {
        // sw $1, 10($2)
        return "sw" +" " +rt.toString() + ", " + offset.toString()+"("+rs.toString()+")";
    }
}
