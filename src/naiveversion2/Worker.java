package naiveversion2;

import aic2021.user.*;

public class Worker extends MyUnit {

    final Location resourceQueryCountLocation;
    Location prevDestination;
    
    boolean torchLighted;
    boolean smoke;
    boolean hasSeenResourceQueries;
    int lastQueriesAccepted;

    static class WorkerStates {
        public static int GATHERING() { return 0; }
        public static int LIGHTINGTHEWAY() { return 1; }
        public static int NAVIGATING() { return 2; }
        public static int GETTINGTASK() { return 3; }
        public static int numValues() { return 4; }
    }

    Worker(UnitController uc) {
        super(uc);

        // TO DO: change to make sure that the square is free
        resourceQueryCountLocation = new Location(home.x + 1, home.y + 1);
        currentState = WorkerStates.NAVIGATING();
        destination = resourceQueryCountLocation;
    }

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
            uc.println("All materials gathered at " + destination.toString());
            uc.println("Looking home and looking for new tasks");
            prevDestination = resourceQueryCountLocation;
            destination = home;
            currentState = WorkerStates.NAVIGATING();
            return;
        }

        boolean food = uc.senseResources(0, Resource.FOOD).length == 0;
        boolean wood = uc.senseResources(0, Resource.WOOD).length == 0;
        boolean stone = uc.senseResources(0,Resource.STONE).length == 0;
        int totalResourcesCarried = getTotalResourcesCarried();
        if(totalResourcesCarried >= 100 || (food && wood && stone) ) {
            uc.println(totalResourcesCarried + " " + (food && wood && stone));
            uc.println("have materials, heading home");
            prevDestination = destination;
            destination = home;
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
            destination = info.location;
            uc.println("Got new instructions: go to " + destination.x + ", " + destination.y);
        }
        else {
            uc.println("Can't read resource query count");
        }
    }

    void navigate(Location currLoc) {
        uc.println("Navigating to " + destination.x + " " + destination.y);
        uc.println("Curr loc is " + currLoc.x + ", " + currLoc.y);
        uc.println("Moving in this direction: " + nav.goToLocation(destination));
        move(nav.goToLocation(destination));
    }

    @Override
    void playRound(){
        super.playRound();
        uc.println("I am a worker");
        // currentState = WorkerStates.LIGHTINGTHEWAY();
        if(destination != null && !destination.isEqual(resourceQueryCountLocation)) {
            uc.println("My destination is " + destination.x + ", " + destination.y);
        }
        UnitInfo myInfo = uc.getInfo();
        boolean torchLighted = keepItLight();
        Location currLoc = uc.getLocation();

        uc.println("My current state is " + currentState);
        if(destination != null) {
            uc.println("Destination set, Navigation set to true");
            currentState = WorkerStates.NAVIGATING();
            if(!destination.isEqual(resourceQueryCountLocation)) {
                uc.println("My destination is still " + destination.x + ", " + destination.y);
                if(!destination.isEqual(currLoc)) {
                    uc.println("That is not equal to my current location");
                }
            }
        }

        if (currentState == WorkerStates.NAVIGATING()) {
            // testing navigation
            if (torchLighted || uc.senseIllumination(uc.getLocation()) == 16) {
                // uc.println("Currently " + Destination.distanceSquared(resourceQueryCountLocation) + " away from rqc");
                if (destination.isEqual(home)) { // removed extra check for uc.getLocation().equals(Destination)
                    uc.println("Homeward bound");
                    if (currLoc.distanceSquared(home) <= 1) {
                        if (uc.canDeposit()) {
                            uc.println("depositing Resources, going to get more");
                            uc.deposit();
                            destination = prevDestination;
                        } else {
                            uc.println("cannot deposit Resources");
                        }
                    } else {
                        navigate(currLoc);
                    }
                } else if (!currLoc.isEqual(destination)) {  // .equals was causing bugs
                    uc.println("On the road again");
                    navigate(currLoc);
                } else if (destination.isEqual(resourceQueryCountLocation)) {
                    uc.println("Reached the rqc square!");
                    destination = null;
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
                    if (destination != null)
                        uc.println("My destination was " + destination.x + ", " + destination.y);
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
            if(destination != null)
                uc.println("My destination was " + destination.x + ", " + destination.y);
        }
        if(uc.hasResearched(Technology.MILITARY_TRAINING, uc.getTeam())) {
            if(uc.canSpawn(UnitType.BARRACKS,Direction.NORTH)) {
                uc.spawn(UnitType.BARRACKS,Direction.NORTH);
            }
        }
    }
}
