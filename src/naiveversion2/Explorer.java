package naiveversion2;

import aic2021.user.*;

public class Explorer extends MyUnit {
    int lastRoundSmokeSignaled;
    Explorer(UnitController uc) {
        super(uc);
    }

    @Override
    void playRound() {
        super.playRound();
        boolean torchLighted = keepItLight();
        broadcast();
        if(torchLighted || uc.getLocation().distanceSquared(home) > 2) {
            uc.println("torch lighted or far from home");
            if (home != null) {
                move(nav.explore());
            }
        }
    }
}
