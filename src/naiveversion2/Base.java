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
    Base(UnitController uc){
        super(uc);
        currState = State.INIT();
    }
    Technology shouldResearchTechnology(){
        if (uc.canResearchTechnology(Technology.MILITARY_TRAINING)){
            uc.researchTechnology(Technology.MILITARY_TRAINING);
            return Technology.MILITARY_TRAINING;
        }
        return null;
    }
    void firstRounds(){
        if (unitCount < 8) {
            Boolean spawned = spawn(UnitType.EXPLORER, Direction.values()[unitCount]);
            if(spawned) {unitCount ++;}
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
