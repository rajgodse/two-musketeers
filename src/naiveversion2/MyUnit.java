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
    public UnitInfo[] friendlies;
    public UnitInfo[] enemies;
    public ResourceInfo[] resources;
    public Location home;

    public FastLocIntMap locationBroadcastRoundMap;
    public FastIntIntMap idBroadcastRoundMap;
    public FastQueue<ResourceInfo> resourceQueue;
    public FastQueue<UnitTarget> unitTargetQueue;

    public Location destination;
    public int currentState;
    public int resourceQueriesSeen;

    final int BROADCAST_COOLDOWN = 30;
    final int BROADCAST_EXPIRATION = 50;
    public int lastRoundBroadcasted;

    MyUnit(UnitController uc) {
        this.uc = uc;
        this.comms = new Comms(uc);
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
                        destination = new Location(home.x + dXdY[0], home.y + dXdY[1]);
                        uc.println("Destination: " + destination);
                    }
                }
            }
        }

        locationBroadcastRoundMap = new FastLocIntMap();
        idBroadcastRoundMap = new FastIntIntMap();
        resourceQueriesSeen = 0;
        resourceQueue = new FastQueue<>();
        unitTargetQueue = new FastQueue<>();

        lastRoundBroadcasted = -BROADCAST_COOLDOWN - 1;
    }
    
    boolean keepItLight() {
        if(uc.getInfo().getTorchRounds() < 10) {
            dropTorch();
        }
        boolean torchLighted = lightTorch();
        return torchLighted;
    }

    void playRound(){
        friendlies = uc.senseUnits(uc.getTeam());
        enemies = uc.senseUnits(uc.getTeam().getOpponent());
        resources = uc.senseResources();
        processSmokeSignals();
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

    Direction[] getNearestDirections(Direction dir) {
        Direction[] nearestDirections = new Direction[] {dir, dir.rotateRight(), dir.rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft()};
        return nearestDirections;
    }

    boolean move(Direction dir){
        if(dir != null) {
            Direction[] nearestDirections = getNearestDirections(dir);
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
        uc.println("Trying to broadcast");
        if(!uc.canMakeSmokeSignal() || lastRoundBroadcasted + BROADCAST_COOLDOWN > uc.getRound()) {
            return false;
        }

        deleteStaleInfo();
        if(broadcastResources()) {
            lastRoundBroadcasted = uc.getRound();
            return true;
        }
        return false;
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
                int messageType = comms.resourceToMessageType(resourceInfo.resourceType);

                if(messageType > 0) {
                    broadcastLocation(messageType, resourceInfo.location);

                    uc.println("Broadcasting resource");
                    uc.drawPointDebug(resourceInfo.location, 0, 200, 0);
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
                broadcastLocation(comms.DEER, unitInfo.getLocation());
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
            int[] numSignals = new int[1];
            int[] signals = comms.getValidSignals(numSignals);
            uc.println("Reading smoke signals: " + numSignals[0]);
            if(uc.readSmokeSignals().length != numSignals[0]) {
                uc.println("Signal discrepancy");
            }

            for(int i = numSignals[0] - 1; i >= 0; i--) {
                int signal = signals[i];
                int messageType = comms.getMessageType(signal);
                if(comms.isLocationMessageType(messageType)) {
                    uc.println("Found location message type");
                    Location loc = comms.getLocation(signal);
                    locationBroadcastRoundMap.add(loc, uc.getRound() - 1);

                    int amount = -1;
                    Resource resource = comms.messageTypeToResource(messageType);
                    if(resource != null) {
                        resourceQueue.add(new ResourceInfo(resource, amount, loc));
                        resourceQueriesSeen++;
                        uc.println("Added to resource queue");
                        uc.drawPointDebug(loc, 0, 0, 200);
                        continue;
                    }

                    UnitType unitType = comms.messageTypeToUnitType(messageType);
                    if(unitType != null) {
                        unitTargetQueue.add(new UnitTarget(unitType, loc));
                        uc.println("Added to unit queue");
                        uc.drawPointDebug(loc, 200, 0, 0);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Clear location/id to round maps of old data
     */
    void deleteStaleInfo() {
        int currRound = uc.getRound();
        Location[] keys = locationBroadcastRoundMap.getKeys();
        for(int i = keys.length - 1; i >= 0; i--) {
            Location loc = keys[i];
            if(locationBroadcastRoundMap.getVal(loc) > currRound + BROADCAST_EXPIRATION) {
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
