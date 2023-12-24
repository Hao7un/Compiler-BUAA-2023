package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsSle extends MipsInstruction{
    private Register rd;
    private Register rs;
    private Register rt;
    public MipsSle(Register rd,Register rs,Register rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        /*
        * sle $d, $s, $t
        *
        * slt $d, $t, $s  // 如果 $t < $s，则 $d = 1
        * xori $d, $d, 1  // 取反，如果 $s <= $t，则 $d = 1
         * */
        //return "sle "+rd.toString()+", "+rs.toString()+", "+rt.toString();

        String slt =  "slt "+ rd.toString()+", " + rt.toString()+", "+rs.toString();
        String xori = "xori " + rd.toString()+", "+rd.toString()+", "+"1";

        return slt + "\n" + xori;
    }
}