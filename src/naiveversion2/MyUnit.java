package naiveversion2;

import aic2021.user.*;
import naiveversion2.common.*;

public abstract class MyUnit {

    Direction[] dirs = Direction.values();

    public UnitController uc;
    public Comms comms;
    public Util util;
    public Nav nav;
    public UnitInfo[] Friendlies;
    public UnitInfo[] Enemies;
    public Location home;

    MyUnit(UnitController uc) {
        this.uc = uc;
        this.comms = new Comms();
        this.util = new Util();
        this.nav = new Nav(this);

        // TODO: Think about base versus home
        UnitInfo[] possibleBases = uc.senseUnits(2, uc.getTeam());
        for(UnitInfo possibleBase: possibleBases) {
            if (possibleBase.getType().equals(UnitType.BASE)) {
                home = possibleBase.getLocation();
            }
        }
    }

    void playRound(){
        Friendlies = uc.senseUnits(uc.getTeam());
        Enemies = uc.senseUnits(uc.getTeam().getOpponent());
    }

    Location[] getFarthestSensableLocations(){
        Location[] allLocations = new Location[8];
        uc.println("round: " + uc.getRound() + ", " + allLocations);
        int count = 0;
        for(Direction dir: Direction.values()) {
            if(dir != Direction.ZERO) {
                uc.println(dir);
                Location loc = uc.getLocation();
                while (loc.distanceSquared(uc.getLocation()) <= uc.getInfo().getType().getVisionRange()) {
                    loc = new Location(loc.x + dir.dx, loc.y + dir.dy);
                }
                Direction newdir = dir.opposite();
                loc = new Location(loc.x + newdir.dx, loc.y + newdir.dy);
                allLocations[count] = loc;
                count++;
            }
        }
        uc.println("round: " + uc.getRound() + ", " + allLocations);
        return allLocations;
    }
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
    Direction[] getNearestDirections(Direction Dir) {
        Direction[] nearestDirections = new Direction[] {Dir, Dir.rotateRight(), Dir.rotateLeft(), Dir.rotateRight().rotateRight(), Dir.rotateLeft().rotateLeft(), Dir.rotateRight().rotateRight().rotateRight(), Dir.rotateLeft().rotateLeft().rotateLeft()};
        return nearestDirections;
    }
    boolean move(Direction Dir){
        if(Dir != null) {
            Direction[] nearestDirections = getNearestDirections(Dir);
            for (Direction currDir : nearestDirections) {
                if (uc.canMove(currDir)) {
                    uc.move(currDir);
                    return true;
                }
            }
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
    boolean dropTorch(){
        if(uc.canThrowTorch(uc.getLocation())) {
            uc.throwTorch(uc.getLocation());
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
