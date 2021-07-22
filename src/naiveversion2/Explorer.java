package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Explorer extends MyUnit {

    Explorer(UnitController uc) {
        super(uc);
    }

    void playRound() {
        super.playRound();
        if(uc.getInfo().getTorchRounds() < 10) {
            dropTorch();
        }
        boolean torchLighted = lightTorch();
        if(torchLighted || uc.getLocation().distanceSquared(home) > 2) {
            if (home != null) {
                move(nav.explore());
            }
        }
    }
}
