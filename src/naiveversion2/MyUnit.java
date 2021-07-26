package naiveversion2;

import aic2021.user.*;
import naiveversion2.common.*;
import naiveversion2.common.fast.*;

public abstract class MyUnit {

    Direction[] dirs = Direction.values();

    public UnitController uc;
    public Comms comms;
    public Util util;
    public Nav nav;
    public UnitInfo[] Friendlies;
    public UnitInfo[] Enemies;
    public ResourceInfo[] Resources;
    public int[] Signals;
    public Location home;
    public FastLocIntMap locationBroadcastRoundMap;
    public FastIntIntMap idBroadcastRoundMap;
    public FasterQueue<ResourceInfo> resourceQueue;
    public FasterQueue<UnitTarget> unitTargetQueue;

    public Location Destination;
    public int currentState;
    public int resourceQueriesSeen;

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
                if (uc.canRead(possibleBase.getLocation())) {
                    int rockArt = uc.read(possibleBase.getLocation());
                    if (comms.getRockArtSmall(rockArt) == Comms.RockArtSmall.LOCATION()) {
                        uc.println("reading location from base");
                        int[] dXdY = comms.getDiffLocation(rockArt);
                        Destination = new Location(home.x + dXdY[0], home.y + dXdY[1]);
                        uc.println("Destination: " + Destination);
                    }
                }
            }
        }

        locationBroadcastRoundMap = new FastLocIntMap();
        idBroadcastRoundMap = new FastIntIntMap();
        resourceQueue = new FasterQueue<>();
        resourceQueriesSeen = 0;
    }
    Boolean keepItLight() {
        if(uc.getInfo().getTorchRounds() < 10) {
            dropTorch();
        }
        boolean torchLighted = lightTorch();
        return torchLighted;
    }
    void playRound(){
        Friendlies = uc.senseUnits(uc.getTeam());
        Enemies = uc.senseUnits(uc.getTeam().getOpponent());
        Resources = uc.senseResources();
        Signals = uc.readSmokeSignals();

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

    boolean spawn(UnitType t, int rockArt, Direction dir){
            int numTries = 0;
            uc.println("Round num:" + uc.getRound());
            while (!uc.canSpawn(t, dir) && numTries < 8) {
                uc.println("Trying to spawn in Direction: "+dir);
                dir = dir.rotateRight();
                numTries++;
            }
            if(numTries < 8) {
                if(uc.canDraw(rockArt)) {
                    uc.println("build successful, writing rock art");
                    uc.draw(rockArt);
                }
                uc.spawn(t, dir);
                uc.println("Spawned in Direction: " + dir);
                uc.println("Rock art: " + rockArt);
                return true;
            }
        return false;
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

    /**
     * Broadcast something if you can
     * @return true if something was broadcasted
     */
    boolean broadcast() {
        if(!uc.canMakeSmokeSignal()) {
            return false;
        }

        deleteStaleInfo();
        return broadcastResources() ||
                broadcastDeer();
    }

    /**
     * Broadcasts any resource that hasn't been broadcasted within the last 50 turns.
     * @return true if a resource was broadcasted
     */
    boolean broadcastResources() {
        ResourceInfo resourceInfo;
        ResourceInfo[] resourceInfos = uc.senseResources();
        for(int i = resourceInfos.length - 1; i >= 0; i--) {
            resourceInfo = resourceInfos[i];
            if(!locationBroadcastRoundMap.contains(resourceInfo.location)) {
                int locationType = -1;
                if(resourceInfo.resourceType == Resource.FOOD) {
                    locationType = Comms.LocationType.FOOD();
                } else if(resourceInfo.resourceType == Resource.WOOD) {
                    locationType = Comms.LocationType.WOOD();
                } else if(resourceInfo.resourceType == Resource.STONE) {
                    locationType = Comms.LocationType.STONE();
                }

                if(locationType != -1) {
                    broadcastLocation(locationType, resourceInfo.location);
                    return true;
                }
            }
        }

        return false;
    }

    boolean broadcastDeer() {
        UnitInfo unitInfo;
        UnitInfo[] unitInfos = uc.senseUnits(Team.NEUTRAL);
        for(int i = unitInfos.length - 1; i >= 0; i--) {
            unitInfo = unitInfos[i];
            if(!idBroadcastRoundMap.contains(unitInfo.getID())) {
                broadcastLocation(Comms.LocationType.DEER(), unitInfo.getLocation());
                return true;
            }
        }

        return false;
    }

    void broadcastLocation(int locationType, Location loc) {
        int flag = comms.createSmokeSignalLocation(locationType, loc);
        uc.makeSmokeSignal(flag);

        locationBroadcastRoundMap.add(loc, uc.getRound());
    }

    /**
     * Read all smoke signals for info
     */
    void processSmokeSignals() {
        if(uc.canReadSmokeSignals()) {
            int[] flags = uc.readSmokeSignals();
            for(int i = flags.length - 1; i >= 0; i--) {
                int flag = flags[i];
                int smokeSignal = comms.getSmokeSignal(flag);
                if(smokeSignal == Comms.SmokeSignal.LOCATION()) {
                    Location loc = comms.getLocation(flag);
                    locationBroadcastRoundMap.add(loc, uc.getRound() - 1);

                    int locationType = comms.getLocationType(flag);
                    int amount = -1;
                    Resource resource = null;
                    if(locationType == Comms.LocationType.FOOD()) {
                        resource = Resource.FOOD;
                    } else if(locationType == Comms.LocationType.STONE()) {
                        resource = Resource.STONE;
                    } else if(locationType == Comms.LocationType.WOOD()) {
                        resource = Resource.WOOD;
                    }

                    if(resource != null) {
                        resourceQueue.add(new ResourceInfo(resource, amount, loc));
                        resourceQueriesSeen++;
                        continue;
                    }

                    UnitType unitType = null;
                    if(locationType == Comms.LocationType.DEER()) {
                        unitType = UnitType.DEER;
                    } else if(locationType == Comms.LocationType.ENEMY_BASE()) {
                        unitType = UnitType.BASE;
                    }

                    if(unitType != null) {
                        unitTargetQueue.add(new UnitTarget(unitType, loc));
                        continue;
                    }
                }
            }
        }
    }

    final int BROADCAST_COOLDOWN = 50;

    /**
     * Clear location/id to round maps of old data
     */
    void deleteStaleInfo() {
        int currRound = uc.getRound();
        Location[] keys = locationBroadcastRoundMap.getKeys();
        for(int i = keys.length - 1; i >= 0; i--) {
            Location loc = keys[i];
            if(locationBroadcastRoundMap.getVal(loc) > currRound + BROADCAST_COOLDOWN) {
                locationBroadcastRoundMap.remove(loc);
            }
        }

        int[] ids = idBroadcastRoundMap.getKeys();
        for(int i = ids.length - 1; i >= 0; i--) {
            int id = ids[i];
            if(idBroadcastRoundMap.getVal(id) > currRound + BROADCAST_COOLDOWN) {
                idBroadcastRoundMap.remove(id);
            }
        }
    }
}
