package naiveversion2.common;

import aic2021.user.*;

public class Comms {
    UnitController uc;

    // Each message salt is 6 bits long and is SALT ^ currRound
    int SALT;
    final int INITIAL_SALT = 22;
    final int SALT_MASK = 0x3F;
    final int BIT_SALT_OFFSET = 26;

    final int BIT_ROCK_ART_SMALL_OFFSET = 14;
    final int BIT_MASK_ROCK_ART_SMALL = ~0 << BIT_ROCK_ART_SMALL_OFFSET;
    final int BIT_MESSAGE_TYPE_OFFSET = 22;
    final int BIT_MESSAGE_TYPE_MASK = 0xF << BIT_MESSAGE_TYPE_OFFSET;
    final int COORD_BIAS = 64;
    final int BIT_DX_OFFSET = 7;
    final int BIT_MASK_COORD = 0x7F;
    final int BIT_MASK_COORDS = 0x3FFF;
    final int BIT_MASK_SUBROBOTTYPE = 0xF;
    final int BIT_X_LOCATION_OFFSET = 11;
    final int BIT_LOCATION_MASK = 0x3FFFFF;
    final int BIT_Y_LOCATION_MASK = 0x7FF;

    public Comms(UnitController uc) {
        this.uc = uc;
        SALT = INITIAL_SALT + uc.getTeam().ordinal();
    }

    public static class RockArtSmall {
        public static int numValues() { return 2; }
        public static int ROBOT_TYPE() { return 0; }
        public static int LOCATION() { return 1; }
    }

    // Smoke signal message types. Max 16 types
    public final int WOOD = 1;
    public final int FOOD = 2;
    public final int STONE = 3;
    public final int DEER = 4;
    public final int ENEMY_BASE = 5;
    public final int RESOURCE_DEPLETED = 6;

    private int getNewSalt() {
        return (SALT ^ uc.getRound()) << BIT_SALT_OFFSET;
    }

    private int salt(int signal) {
        return getNewSalt() | signal;
    }

    // The unit could receive the smoke signal the same turn or the turn after.
    private boolean isValidSignal(int signal) {
        return ((((signal >>> BIT_SALT_OFFSET) ^ (uc.getRound() - 1)) & SALT_MASK) == SALT) ||
                ((((signal >>> BIT_SALT_OFFSET) ^ uc.getRound()) & SALT_MASK) == SALT);
    }

    public int[] getValidSignals(int[] numSignals) {
        if(!uc.canReadSmokeSignals()) {
            numSignals[0] = 0;
            return null;
        }

        int[] smokeSignals = uc.readSmokeSignals();
        int lastValid = 0;
        for(int i = 0; i < smokeSignals.length; i++) {
            if(isValidSignal(smokeSignals[i])) {
                smokeSignals[lastValid] = smokeSignals[i];
                lastValid++;
            }
        }

        numSignals[0] = lastValid;
        return smokeSignals;
    }

    public int getRockArtSmall(int flag) {
        return flag >> BIT_ROCK_ART_SMALL_OFFSET;
    }

    public int createRockArtSmallLocation(int dx, int dy) {
        return (RockArtSmall.LOCATION() << BIT_ROCK_ART_SMALL_OFFSET) |
                ((dx + COORD_BIAS) << BIT_DX_OFFSET) |
                (dy + COORD_BIAS);
    }


    public int getSubRobotType(int signal) {
        return signal & BIT_MASK_SUBROBOTTYPE;
    }

    public int createRockArtSmallRobotType(int subRobotType) {
        return (RockArtSmall.ROBOT_TYPE() << BIT_ROCK_ART_SMALL_OFFSET) | subRobotType;
    }

    public int getMessageType(int signal) {
        return (signal & BIT_MESSAGE_TYPE_MASK) >>> BIT_MESSAGE_TYPE_OFFSET;
    }

    public int resourceToMessageType(Resource R){
        if(R == Resource.FOOD) { return FOOD; }
        if(R == Resource.WOOD) { return WOOD; }
        if(R == Resource.STONE) { return STONE; }
        return 0;
    }

    public boolean messageTypeIsResource(int messageType) {
        return WOOD == messageType ||
                STONE == messageType ||
                FOOD == messageType;
    }

    public Resource messageTypeToResource(int messageType) {
        if(messageType == WOOD) { return Resource.WOOD; }
        if(messageType == FOOD) { return Resource.FOOD; }
        if(messageType == STONE) { return Resource.STONE; }
        return null;
    }

    public UnitType messageTypeToUnitType(int messageType) {
        if(messageType == DEER) { return UnitType.DEER; }
        if(messageType == ENEMY_BASE) { return UnitType.BASE; }
        return null;
    }

    public boolean isLocationMessageType(int messageType) {
        return WOOD <= messageType && messageType <= RESOURCE_DEPLETED;
    }

    public int createSmokeSignalLocation(int messageType, Location loc) {
        return salt((messageType << BIT_MESSAGE_TYPE_OFFSET) |
                    (loc.x << BIT_X_LOCATION_OFFSET) |
                    loc.y);
    }

    public Location getLocation(int signal) {
        int x = (signal & BIT_LOCATION_MASK) >>> BIT_X_LOCATION_OFFSET;
        int y = (signal & BIT_Y_LOCATION_MASK);
        return new Location(x, y);
    }

    public int[] getDiffLocation(int rockArt) {
        int x = ((rockArt >>> BIT_DX_OFFSET) & BIT_MASK_COORD) - COORD_BIAS;
        int y = (rockArt & BIT_MASK_COORD) - COORD_BIAS;
        return new int[]{x, y};
    }
}
