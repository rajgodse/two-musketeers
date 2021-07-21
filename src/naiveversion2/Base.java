package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Base extends MyUnit {
    static class State {
        public static int numValues(){return 2;}
        public static int INIT(){return 0;}
        public static int IDLE(){return 1;}
    }
    int unitCount = 0;
    int currState;
    Technology shouldResearchTechnology(){
        if (uc.canResearchTechnology(Technology.MILITARY_TRAINING)){
            uc.researchTechnology(Technology.MILITARY_TRAINING);
            return Technology.MILITARY_TRAINING;
        }
        return null;
    }
    Base(UnitController uc){
        super(uc);
        currState = State.INIT();

    }

    void firstRounds(){
        if (unitCount < 3) {
            Location[] FarthestSensableLocations = getFarthestSensableLocations();
            int locCount = 0;
            for(Location loc: FarthestSensableLocations) {
                uc.println("round: " + uc.getRound() + loc);
                if(uc.canSenseLocation(loc)) {
                    uc.println("round: " + uc.getRound() + ", base can sense location" + loc);
                    Boolean spawned = spawn(UnitType.EXPLORER, Direction.values()[locCount]);
                    if(spawned) {unitCount ++;}
                }
                locCount ++;
            }

        }
        else {
            currState = State.IDLE();
        }

    }
    void playRound(){
        if(currState == State.INIT()) {
            firstRounds();
        }
        else if(currState == State.IDLE()) {
            return;
        }
        Technology techResearched = shouldResearchTechnology();
    }

}
