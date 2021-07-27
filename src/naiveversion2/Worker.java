package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;
import naiveversion2.common.Comms;
import naiveversion2.common.UnitTarget;

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
    int lastQueriesAccepted;

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

    void readResourceQueryCount(Location currLoc) {
        if (uc.canRead(currLoc)) {
            currentState = WorkerStates.GETTINGTASK();
            int queriesAccepted = uc.read(currLoc);
            while (queriesAccepted > lastQueriesAccepted) {
                resourceQueue.poll();
                lastQueriesAccepted++;
            }
            if (resourceQueriesSeen <= queriesAccepted) {
                uc.println("No new queries");
                return;
            }
            ResourceInfo info = resourceQueue.poll();
            if (info == null) {
                uc.println("Query count wrong");
                return;
            }
            uc.println("Read the following info: " + info);
            if (uc.canDraw(queriesAccepted + 1)) {
                uc.draw(queriesAccepted + 1);
            }
            else {
                uc.println("Can't write resource query count");
            }
            currentState = WorkerStates.NAVIGATING();
            if(info.location == null) {
                uc.println("Smoke signal location not detected");
            }
            Destination = info.location;
            uc.println("Got new instructions: go to " + Destination.x + ", " + Destination.y);
        }
        else {
            uc.println("Can't read resource query count");
        }
    }

    void navigate(Location currLoc) {
        uc.println("Navigating to " + Destination.x + " " + Destination.y);
        uc.println("Curr loc is " + currLoc.x + ", " + currLoc.y);
        uc.println("Moving in this direction: " + nav.goToLocation(Destination));
        move(nav.goToLocation(Destination));
    }

    void playRound(){
        uc.println("I am a worker");
        // currentState = WorkerStates.LIGHTINGTHEWAY();
        if(Destination != null && !Destination.isEqual(resourceQueryCountLocation)) {
            uc.println("My destination is " + Destination.x + ", " + Destination.y);
        }
        UnitInfo myInfo = uc.getInfo();
        Boolean torchLighted = keepItLight();
        Location currLoc = uc.getLocation();

        uc.println("My current state is " + currentState);
        if(Destination != null) {
            uc.println("Destination set, Navigation set to true");
            currentState = WorkerStates.NAVIGATING();
            if(!Destination.isEqual(resourceQueryCountLocation)) {
                uc.println("My destination is still " + Destination.x + ", " + Destination.y);
                if(!Destination.isEqual(currLoc)) {
                    uc.println("That is not equal to my current location");
                }
            }
        }
        if (currentState == WorkerStates.NAVIGATING()) {
            // testing navigation
            if (torchLighted || uc.senseIllumination(uc.getLocation()) == 16) {
                // uc.println("Currently " + Destination.distanceSquared(resourceQueryCountLocation) + " away from rqc");
                if (Destination.isEqual(home)) { // removed extra check for uc.getLocation().equals(Destination)
                    uc.println("Homeward bound");
                    if (currLoc.distanceSquared(home) <= 1) {
                        if (uc.canDeposit()) {
                            uc.println("depositing Resources, going to get more");
                            uc.deposit();
                            Destination = prevDestination;
                        } else {
                            uc.println("cannot deposit Resources");
                        }
                    } else {
                        navigate(currLoc);
                    }
                } else if (!currLoc.isEqual(Destination)) {  // .equals was causing bugs
                    uc.println("On the road again");
                    navigate(currLoc);
                } else if (Destination.isEqual(resourceQueryCountLocation)) {
                    uc.println("Reached the rqc square!");
                    Destination = null;
                    if (!hasSeenResourceQueries) {
                        hasSeenResourceQueries = true;
                        if (uc.canRead(currLoc)) {
                            int resourceQueriesAccepted = uc.read(currLoc);
                            resourceQueriesSeen += resourceQueriesAccepted;
                            lastQueriesAccepted = resourceQueriesAccepted;
                        } else {
                            uc.println("Can't read resource query count after INIT");
                        }
                    }
                    readResourceQueryCount(currLoc);
                    if (Destination != null)
                        uc.println("My destination was " + Destination.x + ", " + Destination.y);
                } else {
                    currentState = WorkerStates.GATHERING();
                }
            }
            else {
                uc.println("it's kinda dim, ngl");
            }
        }
        if(currentState == WorkerStates.LIGHTINGTHEWAY()) {
            lightTheWay(myInfo);
        } if(currentState == WorkerStates.GATHERING()) {
            gather(myInfo);
        } if (currentState == WorkerStates.GETTINGTASK()) {
            readResourceQueryCount(currLoc);
            if(Destination != null)
                uc.println("My destination was " + Destination.x + ", " + Destination.y);
        }
        if(uc.hasResearched(Technology.MILITARY_TRAINING, uc.getTeam())) {
            if(uc.canSpawn(UnitType.BARRACKS,Direction.NORTH)) {
                uc.spawn(UnitType.BARRACKS,Direction.NORTH);
            }
        }
    }
}
