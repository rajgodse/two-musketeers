package naiveversion1;

import aic2021.user.*;
import naiveversion1.MyUnit;

public class Base extends MyUnit {

    int workers = 0;

    Base(UnitController uc){
        super(uc);
    }
    Technology shouldResearchTechnology(){
        if (uc.canResearchTechnology(Technology.MILITARY_TRAINING)){
            uc.researchTechnology(Technology.MILITARY_TRAINING);
            return Technology.MILITARY_TRAINING;
        }
        return null;
    }
    void playRound(){
        Technology techResearched = shouldResearchTechnology();
        if (workers < 5){
            if (spawnRandom(UnitType.WORKER)) ++workers;
        }
    }

}
