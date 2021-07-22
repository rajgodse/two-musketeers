package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Explorer extends MyUnit {

    Explorer(UnitController uc) {
        super(uc);
    }

    void playRound() {
        boolean torchLighted = lightTorch();
        if(torchLighted || uc.getLocation().distanceSquared(home) > 2) {
            if (home != null) {
                move(uc.getLocation().directionTo(home).opposite());
            }
        }
    }

}
