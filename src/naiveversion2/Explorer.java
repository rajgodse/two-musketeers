package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Explorer extends MyUnit {
    int lastRoundSmokeSignaled = 0;
    Explorer(UnitController uc) {
        super(uc);
    }

    void playRound() {
        super.playRound();
        Boolean torchLighted = keepItLight();
        broadcast();
        if(torchLighted || uc.getLocation().distanceSquared(home) > 2) {
            uc.println("torch lighted or far from home");
            if (home != null) {
                move(nav.explore());
            }
        }
    }
}
