package midend;

import midend.values.User;
import midend.values.Value;

public class Use {
    private Value value;
    private User user;

    public Use(Value value, User user) {
        this.value = value;
        this.user = user;
    }
    public User getUser() {
        return user;
    }


}
