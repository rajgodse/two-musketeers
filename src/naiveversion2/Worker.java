package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Worker extends MyUnit {

    static class WorkerStates {
        public static int numValues() { return 3; }
        public static int GATHERING() { return 0; }
        public static int LIGHTINGTHEWAY() { return 1; }
        public static int NAVIGATING() { return 2; }
    }

    Worker(UnitController uc) {
        super(uc);
    }

    boolean torchLighted = false;
    boolean smoke = false;

    void lightTheWay(UnitInfo myInfo){
        return;
    }
    void gather(UnitInfo myInfo) { return; }

    void playRound(){
        UnitInfo myInfo = uc.getInfo();
        UnitInfo[] nearbyFriendlies = uc.senseUnits(uc.getTeam());
        UnitInfo[] nearbyEnemies = uc.senseUnits(uc.getTeam().getOpponent());

        Location currLoc = uc.getLocation();
        Location testLocation = new Location(currLoc.x + 5, currLoc.y + 5);
        int currentState = WorkerStates.NAVIGATING();
        if (currentState == WorkerStates.NAVIGATING()) {
            // testing navigation
            nav.goToLocation(testLocation);
        } else if(currentState == WorkerStates.LIGHTINGTHEWAY()) {
            lightTheWay(myInfo);
        } else if(currentState == WorkerStates.GATHERING()) {
            gather(myInfo);
        }
        if(uc.hasResearched(Technology.MILITARY_TRAINING, uc.getTeam())) {
            if(uc.canSpawn(UnitType.BARRACKS,Direction.NORTH)) {
                uc.spawn(UnitType.BARRACKS,Direction.NORTH);
            }
        }
    }
}
