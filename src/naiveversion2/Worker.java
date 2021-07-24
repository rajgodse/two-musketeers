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
    Location testLocation;
    Worker(UnitController uc) {
        super(uc);
        testLocation = new Location(home.x -32, home.y - 32);

    }

    boolean torchLighted = false;
    boolean smoke = false;

    void lightTheWay(UnitInfo myInfo){
        return;
    }
    void gather(UnitInfo myInfo) { return; }

    void playRound(){
        UnitInfo myInfo = uc.getInfo();

        Boolean torchLighted = keepItLight();

        Location currLoc = uc.getLocation();
        int currentState = WorkerStates.NAVIGATING();
        if (currentState == WorkerStates.NAVIGATING()) {
            // testing navigation
            if(torchLighted || uc.senseIllumination(uc.getLocation()) == 16) {
            move(nav.goToLocation(testLocation));

            }
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
