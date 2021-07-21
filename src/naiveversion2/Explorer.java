package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Explorer extends MyUnit {
    public Location baseLocation;

    Explorer(UnitController uc){
        super(uc);
        UnitInfo[] possibleBases = uc.senseUnits(2, uc.getTeam());
        for(UnitInfo possibleBase: possibleBases) {
            if (possibleBase.getType().equals(UnitType.BASE)) {
                baseLocation = possibleBase.getLocation();
            }
        }
    }

    void playRound(){
        if (baseLocation != null) {
            move(uc.getLocation().directionTo(baseLocation).opposite());
        }
    }

}
