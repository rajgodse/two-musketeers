package naiveversion1;

import aic2021.user.*;
import naiveversion1.MyUnit;

public class Base extends MyUnit {

    int workers = 0;

    Base(UnitController uc){
        super(uc);
    }

    void playRound(){
        if (workers < 5){
            if (spawnRandom(UnitType.WORKER)) ++workers;
        }
    }

}
