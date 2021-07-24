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
        if (Resources.length > 0 && uc.getRound() - lastRoundSmokeSignaled >= 30 && uc.canMakeSmokeSignal()) {
            uc.println("Sending out a signal");
            lastRoundSmokeSignaled = uc.getRound();
            uc.println(Resources[0].getResourceType());
            int smokeSignal = comms.createSmokeSignalLocation(comms.resourceToLocationType(Resources[0].getResourceType()), Resources[0].getLocation());
            uc.makeSmokeSignal(smokeSignal);
        }
        if (Signals.length > 0){
            uc.println("Receiving a signal");
            uc.drawPointDebug(uc.getLocation(), 0, 0, 0);
        }
        if(torchLighted || uc.getLocation().distanceSquared(home) > 2) {
            uc.println("torch lighted or far from home");
            if (home != null) {
                move(nav.explore());
            }
        }
    }
}
