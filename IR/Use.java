package IR;

import IR.values.User;
import IR.values.Value;

public class Use {
    private Value value;
    private User user;
    private final int pos;  // value在user的operandList中的位置

    public Use(Value value, User user,int pos) {
        this.value = value;
        this.user = user;
        this.pos = pos;
    }

}
