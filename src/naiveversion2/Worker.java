package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;
import naiveversion2.common.Comms;
import naiveversion2.common.UnitTarget;
import sun.security.krb5.internal.crypto.Des;

public class Worker extends MyUnit {

    // TO DO: change to make sure that the square is free
    final Location resourceQueryCountLocation = new Location (home.x + 1, home.y + 1);

    static class WorkerStates {
        public static int GATHERING() { return 0; }
        public static int LIGHTINGTHEWAY() { return 1; }
        public static int NAVIGATING() { return 2; }
        public static int GETTINGTASK() { return 3; }
        public static int numValues() { return 4; }
    }
    Location prevDestination;
    Worker(UnitController uc) {
        super(uc);
        currentState = WorkerStates.NAVIGATING();
        Destination = resourceQueryCountLocation;
    }

    boolean torchLighted = false;
    boolean smoke = false;
    boolean hasSeenResourceQueries = false;
    int resourceQueriesSeen = 0;

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
        else {
            uc.println("All materials gathered at " + Destination.toString());
            uc.println("Looking home and looking for new tasks");
            prevDestination = resourceQueryCountLocation;
            Destination = home;
            currentState = WorkerStates.NAVIGATING();
            return;
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

    void handleSignals(){
        for(int sig: Signals) {
            if(comms.getSmokeSignal(sig) == Comms.SmokeSignal.LOCATION()) {
                int locationType = comms.getLocationType(sig);
                uc.println("receiving a signal, location type: " + locationType);
                if(comms.locationTypeIsResource(locationType)) {
                    uc.println("adding to the resource queue");
                    resourceQueue.add(new ResourceInfo(comms.locationTypeToResource(locationType), 0, comms.getLocation(sig)));
                }
            }
        }
    }

    void readResourceQueryCount(Location currLoc) {
        if (uc.canRead(currLoc)) {
            currentState = WorkerStates.GETTINGTASK();
            int queryCount = uc.read(currLoc);
            while (resourceQueriesSeen < queryCount) {
                resourceQueue.poll();
                resourceQueriesSeen++;
            }
            ResourceInfo info = resourceQueue.poll();
            if (info == null) {
                return;
            }
            if (uc.canDraw(queryCount + 1)) {
                uc.draw(queryCount + 1);
            }
            else {
                uc.println("Can't write resource query count");
            }
            currentState = WorkerStates.NAVIGATING();
            Destination = info.location;
        }
        else {
            uc.println("Can't read resource query count");
        }
    }

    void playRound(){
        // currentState = WorkerStates.LIGHTINGTHEWAY();
        UnitInfo myInfo = uc.getInfo();
        Boolean torchLighted = keepItLight();
        Location currLoc = uc.getLocation();
        handleSignals();
        uc.println("My current state is " + currentState);
        if(Destination != null) {
            uc.println("Destination set, Navigation set to true");
            currentState = WorkerStates.NAVIGATING();
        }
        if (currentState == WorkerStates.NAVIGATING()) {
            // testing navigation
            if(torchLighted || uc.senseIllumination(uc.getLocation()) == 16) {
                if(!uc.getLocation().equals(Destination)) {
                    uc.println("Navigating to " + Destination.x + " " + Destination.y);
                    uc.println("Moving in this direction: " + nav.goToLocation(Destination));
                    move(nav.goToLocation(Destination));
                }
                else if (Destination.distanceSquared(home) == 1) { // removed extra check for uc.getLocation().equals(Destination)
                    if(uc.canDeposit()) {
                        uc.println("depositing Resources, going to get more");
                        uc.deposit();
                        Destination = prevDestination;
                    }
                    else {
                        uc.println("cannot deposit Resources");
                    }
                }
                else if (Destination.distanceSquared(resourceQueryCountLocation) == 1) {
                    if (!hasSeenResourceQueries) {
                        hasSeenResourceQueries = true;
                        if (uc.canRead(currLoc)) {
                            resourceQueriesSeen = uc.read(currLoc);
                        }
                        else {
                            uc.println("Can't read resource query count after INIT");
                        }
                    }
                    else {
                        readResourceQueryCount(currLoc);
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
        } if (currentState == WorkerStates.GETTINGTASK()) {
            readResourceQueryCount(currLoc);
        }
        if(uc.hasResearched(Technology.MILITARY_TRAINING, uc.getTeam())) {
            if(uc.canSpawn(UnitType.BARRACKS,Direction.NORTH)) {
                uc.spawn(UnitType.BARRACKS,Direction.NORTH);
            }
        }
    }
}
