package naiveversion2;

import aic2021.user.*;
import naiveversion2.MyUnit;

public class Worker extends MyUnit {
    public Location baseLocation;
    Worker(UnitController uc) {
        super(uc);
        UnitInfo[] possibleBases = uc.senseUnits(2, uc.getTeam());
        for(UnitInfo possibleBase: possibleBases) {
            if (possibleBase.getType().equals(UnitType.BASE)) {
                baseLocation = possibleBase.getLocation();
            }
        }
        uc.println("Base Location: " + baseLocation);
    }

    boolean torchLighted = false;
    boolean smoke = false;

    void playRound(){
        UnitInfo myInfo = uc.getInfo();
        if(uc.hasResearched(Technology.MILITARY_TRAINING, uc.getTeam())) {
            if(uc.canSpawn(UnitType.BARRACKS,Direction.NORTH)) {
                uc.spawn(UnitType.BARRACKS,Direction.NORTH);
            }
        }
        if(uc.getRound() % 200 == 0) {
            uc.println("round: " + Integer.toString(uc.getRound()) + ", ID: " + Integer.toString(myInfo.getID()));
        }
        if (uc.getRound() > 300 + myInfo.getID()%200 && !smoke){
            if (uc.canMakeSmokeSignal()){
                uc.makeSmokeSignal(0);
                smoke = true;
            }
        }
        moveRandom();
        if (!torchLighted && myInfo.getTorchRounds() <= 0){
            lightTorch();
        }
        myInfo = uc.getInfo();
        if (myInfo.getTorchRounds() < 70){
            randomThrow();
        }
        int[] signals = uc.readSmokeSignals();
        if (signals.length > 0){
            uc.drawPointDebug(uc.getLocation(), 0, 0, 0);
        }
    }
}
