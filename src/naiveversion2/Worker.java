package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;
import sun.security.krb5.internal.crypto.Des;

public class Worker extends MyUnit {

    static class WorkerStates {
        public static int numValues() { return 3; }
        public static int GATHERING() { return 0; }
        public static int LIGHTINGTHEWAY() { return 1; }
        public static int NAVIGATING() { return 2; }
    }
    Location prevDestination;
    Worker(UnitController uc) {
        super(uc);
    }

    boolean torchLighted = false;
    boolean smoke = false;

    void lightTheWay(UnitInfo myInfo){
        return;
    }

    public int getTotalResourcesCarried() {
        int[] resourceList = uc.getResourcesCarried();
        return resourceList[0] + resourceList[1] + resourceList[2];
    }

    void gather(UnitInfo myInfo) {
        if(uc.canGatherResources()){
            uc.gatherResources();
            uc.println("gathering resources");
        }
        Boolean food = uc.senseResources(0, Resource.FOOD).length == 0;
        Boolean wood = uc.senseResources(0, Resource.WOOD).length == 0;
        Boolean stone = uc.senseResources(0,Resource.STONE).length == 0;
        int totalResourcesCarried = getTotalResourcesCarried();
        if(totalResourcesCarried >= 100 || (food && wood && stone) ) {
            uc.println(totalResourcesCarried + " " + (food && wood && stone));
            uc.println("have materials, heading home");
            prevDestination = Destination;
            Destination = home;
            currentState = WorkerStates.NAVIGATING();
        }
    }

    void playRound(){
        currentState = WorkerStates.LIGHTINGTHEWAY();
        UnitInfo myInfo = uc.getInfo();
        Boolean torchLighted = keepItLight();
        Location currLoc = uc.getLocation();
        if(Destination != null) {
            uc.println("Destination set, Navigation set to true");
            currentState = WorkerStates.NAVIGATING();
        }
        if (currentState == WorkerStates.NAVIGATING()) {
            // testing navigation
            if(torchLighted || uc.senseIllumination(uc.getLocation()) == 16) {
                if(!uc.getLocation().equals(Destination)) {
                    move(nav.goToLocation(Destination));
                }
                else if (uc.getLocation().equals(Destination) && Destination.distanceSquared(home) == 1) {
                    if(uc.canDeposit()) {
                        uc.println("depositing Resources, going to get more");
                        uc.deposit();
                        Destination = prevDestination;
                    }
                    else {
                        uc.println("cannot deposit Resources");
                    }
                }
                else {
                    currentState = WorkerStates.GATHERING();
                }
            }
        } if(currentState == WorkerStates.LIGHTINGTHEWAY()) {
            lightTheWay(myInfo);
        } if(currentState == WorkerStates.GATHERING()) {
            gather(myInfo);
        }
        if(uc.hasResearched(Technology.MILITARY_TRAINING, uc.getTeam())) {
            if(uc.canSpawn(UnitType.BARRACKS,Direction.NORTH)) {
                uc.spawn(UnitType.BARRACKS,Direction.NORTH);
            }
        }
    }
}
