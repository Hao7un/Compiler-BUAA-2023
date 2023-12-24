package backend.optimize;

import backend.Components.MipsModule;

public class BackOptimizer {

    public BackOptimizer() {

    }

    public void optimize(MipsModule module) {
        MoveOptimize moveOptimize = new MoveOptimize(module);
        moveOptimize.run();
    }
}
