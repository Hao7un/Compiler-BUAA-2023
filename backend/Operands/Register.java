package backend.Operands;

import java.util.ArrayList;
import java.util.Objects;

public class Register {
    private static ArrayList<String>  regNames = new ArrayList<>();

    static {
        regNames.add("$zero");
        regNames.add("$at");
        regNames.add("$v0");
        regNames.add("$v1");
        for (int i = 0; i < 4; i++) {
            regNames.add("$a" + i);
        }
        for (int i = 0; i < 8; i++) {
            regNames.add("$t" + i);
        }
        for (int i = 0; i < 8; i++) {
            regNames.add("$s" + i);
        }
        regNames.add("$t8");
        regNames.add("$t9");
        regNames.add("$k0");
        regNames.add("$k1");
        regNames.add("$gp");
        regNames.add("$sp");
        regNames.add("$fp");
        regNames.add("$ra");
    }
    private int regNumber;

    private String regName;

    public Register(int regNumber) {
        this.regNumber = regNumber;
    }

    public Register(String regName) {
        assert regNames.contains(regName);
        this.regName = regName;
    }

    public String getRegName() {
        return regName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(regName);
    }

    @Override
    public boolean equals(Object o) {
        //自反性
        if (this == o) return true;
        //任何对象不等于null，比较是否为同一类型
        if (!(o instanceof Register reg)) return false;
        return Objects.equals(getRegName(), reg.getRegName());
    }

    @Override
    public String toString() {
        if (regName != null) {
            return regName;
        }
        return regNames.get(regNumber);
    }
}
