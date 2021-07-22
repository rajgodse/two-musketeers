package naiveversion2.common;

import aic2021.user.*;

public class Comms {
    final int BIT_ROCK_ART_SMALL_OFFSET = 14;
    final int BIT_MASK_ROCK_ART_SMALL = ~0 << BIT_ROCK_ART_SMALL_OFFSET;
    final int COORD_BIAS = 64;
    final int BIT_DX_OFFSET = 7;
    final int BIT_MASK_COORD = 0x7F;
    final int BIT_MASK_COORDS = 0x3FFF;
    final int BIT_MASK_SUBROBOTTYPE = 0xF;
    final int BIT_SMOKE_SIGNAL_OFFSET = 26;

    public Comms() {}

    public static class RockArtSmall {
        public static int numValues() { return 2; }
        public static int ROBOT_TYPE() { return 0; }
        public static int LOCATION() { return 1; }
    }

    public static class SmokeSignal {
        public static int numValues() { return 1; }
        public static int LOCATION() { return 0; }
    }

    public static class SubRobotType {
        public static int numValues() { return 1; }
        public static int LOCATION() { return 0; }
    }

    public int getRockArtSmall(int flag) {
        return flag >> BIT_ROCK_ART_SMALL_OFFSET;
    }

    public int createRockArtSmallLocation(int dx, int dy) {
        return (RockArtSmall.LOCATION() << BIT_ROCK_ART_SMALL_OFFSET) |
                ((dx + COORD_BIAS) << BIT_DX_OFFSET) |
                (dy + COORD_BIAS);
    }

    public int getSubRobotType(int flag) {
        return flag & BIT_MASK_SUBROBOTTYPE;
    }

    public int createRockArtSmallRobotType(int subRobotType) {
        return (RockArtSmall.ROBOT_TYPE() << BIT_ROCK_ART_SMALL_OFFSET) | subRobotType;
    }

    public int createSmokeSignalLocation(int smokeSignal, int dx, int dy) {
        return (smokeSignal << BIT_SMOKE_SIGNAL_OFFSET) |
                ((dx + COORD_BIAS) << BIT_DX_OFFSET) |
                (dy + COORD_BIAS);
    }

    public int[] getLocation(int flag) {
        int[] res = new int[2];
        res[0] = (flag >>> BIT_DX_OFFSET) & BIT_MASK_COORD;
        res[1] = flag & BIT_MASK_COORD;
        return res;
    }
}
