package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Base extends MyUnit {

    int unitCount = 0;
    int currState;
    Direction[] spawningDirections;
    boolean[] hasSpawned;

    Base(UnitController uc){
        super(uc);
        currState = State.INIT();
    }

    static class State {
        public static int numValues() { return 2; }
        public static int INIT() { return 0; }
        public static int IDLE() { return 1; }
    }

    Technology shouldResearchTechnology(){
        if (uc.canResearchTechnology(Technology.MILITARY_TRAINING)){
            uc.researchTechnology(Technology.MILITARY_TRAINING);
            return Technology.MILITARY_TRAINING;
        }
        return null;
    }

    Direction[] getSpawningDirections() {
        Location[] farthestSensibleLocations = getFarthestSensableLocations();
        int minObstacle = -1, maxObstacle = 9;
        for (int i = 7; i >= 0; i--) {
            Location l = farthestSensibleLocations[i];
            if(!uc.canSenseLocation(l)) {
                maxObstacle=i;
                break;
            }
        }
        if (maxObstacle == 9) {
            return new Direction[]{dirs[0], dirs[3], dirs[5]};
        }
        for (int i = 0; i < 8; i++) {
            Location l = farthestSensibleLocations[i];
            if (!uc.canSenseLocation(l)) {
                minObstacle=i;
                break;
            }
        }
        // max obstacle becomes lower bound of wrap-around free cell range
        int lowerBound = maxObstacle + 1;
        // min obstacle + 8 becomes upper bound of wrapped range
        int upperBound = minObstacle + 8;
        int diff = upperBound - lowerBound;
        
        // dealing with obstacle range that wraps around
        if (minObstacle == 0 && maxObstacle == 7) {
            lowerBound = minObstacle + 1;
            Location l = farthestSensibleLocations[lowerBound];
            while (!uc.canSenseLocation(l)) {
                lowerBound++;
                l = farthestSensibleLocations[lowerBound];
            }
            upperBound = maxObstacle;
            l = farthestSensibleLocations[upperBound - 1];
            while (!uc.canSenseLocation(l)) {
                upperBound--;
                l = farthestSensibleLocations[upperBound - 1];
            }
            diff = upperBound - lowerBound;
        }
                
        assert diff >= 3 : "Another edge case";
        
        // spacing them at the center of three equal sectors
        int first = (lowerBound + diff / 6) % 8;
        int second = (lowerBound + diff / 2) % 8;
        int third = (lowerBound + 5 * diff / 6) % 8;
        return new Direction[]{dirs[first], dirs[second], dirs[third]};
    }

    void firstRounds() {
        spawningDirections = getSpawningDirections();
        hasSpawned = new boolean[3];
        for(int i = 0; i < 3; i++) {
            if(!hasSpawned[i]) {
                Direction d = spawningDirections[i];
                boolean spawned = spawn(UnitType.EXPLORER, d);
                if(spawned) {
                    unitCount++;
                    hasSpawned[i] = true;
                }
            }
        }
        // Gets us to the next state one turn earlier
        if (unitCount >= 3)
            currState = State.IDLE();
    }

    void playRound(){
        if(currState == State.INIT()) {
            firstRounds();
        } else if(currState == State.IDLE()) {
            return;
        }
        Technology techResearched = shouldResearchTechnology();
    }

}
