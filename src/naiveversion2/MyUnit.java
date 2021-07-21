package naiveversion2;

import aic2021.user.*;

public abstract class MyUnit {

    Direction[] dirs = Direction.values();

    UnitController uc;

    MyUnit(UnitController uc){
        this.uc = uc;
    }

    abstract void playRound();

    boolean spawn(UnitType t, Direction dir){
            int numTries = 0;
            uc.println("Round num:" + uc.getRound());
            while (!uc.canSpawn(t, dir) && numTries < 8) {
                uc.println("Trying to spawn in Direction: "+dir);
                dir = dir.rotateRight();
                numTries++;
            }
            if(numTries < 8) {
                uc.spawn(t, dir);
                uc.println("Spawned in Direction: " + dir);
                return true;
            }
        return false;
    }

    boolean move(Direction Dir){
        int tries = 10;
        Direction dir = Dir;
        while (uc.canMove() && tries-- > 0){
            if (uc.canMove(dir)){
                uc.move(dir);
                return true;
            }
            dir = dir.rotateRight();
        }
        return false;
    }

    boolean lightTorch(){
        if (uc.canLightTorch()){
            uc.lightTorch();
            return true;
        }
        return false;
    }

    boolean randomThrow(){
        Location[] locs = uc.getVisibleLocations(uc.getType().getTorchThrowRange(), false);
        int index = (int)(uc.getRandomDouble()*locs.length);
        if (uc.canThrowTorch(locs[index])){
            uc.throwTorch(locs[index]);
            return true;
        }
        return false;
    }

}
