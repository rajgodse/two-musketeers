package naiveversion2;

import aic2021.user.*;
import naiveversion2.*;

public class Barracks extends MyUnit {

    static class State {
        public static int numValues() { return 2; }
        public static int INIT() { return 0; }
        public static int IDLE() { return 1; }
    }

    final double SPEARMAN_GUARD_RATE = 0.8;
    final double AXEMAN_GUARD_RATE = 0.4;

    int currState;
    int guardAxemen = 0;
    int axemen = 0;
    int guardSpearmen = 0;
    int spearmen = 0;

    Barracks(UnitController uc){
        super(uc);
        currState = State.INIT();
    }

    int total_units() {
        return guardAxemen + axemen + guardSpearmen + spearmen;
    }

    boolean shouldSpawnGuard(UnitType ut) {
        if(ut == UnitType.AXEMAN) {
            return uc.getRandomDouble() < AXEMAN_GUARD_RATE;
        }
        else {
            return uc.getRandomDouble() < SPEARMAN_GUARD_RATE;
        }
    }

    boolean shouldKeepSpawning() {
        return total_units() < 10;
    }

    void spawn() {
        boolean axemanSpawned = false;
        for(int i = 0; i < 8 && shouldKeepSpawning(); i++) {
            Direction d = dirs[i];
            if (!axemanSpawned && spawn(UnitType.AXEMAN, d)) {
                if (shouldSpawnGuard(UnitType.AXEMAN)) {
                    // tell him he's a guard
                    guardAxemen++;
                } else {
                    // tell him he's an attacker
                    axemen++;
                }
                axemanSpawned=true;
                continue;
            }
            if (spawn(UnitType.SPEARMAN, d)) {
                if (shouldSpawnGuard(UnitType.SPEARMAN)) {
                    // tell him he's a guard
                    guardSpearmen++;
                } else {
                    // tell him he's an attacker
                    spearmen++;
                }
                axemanSpawned=false;
            }
        }
        if (!shouldKeepSpawning()) {
            currState = State.IDLE();
        }
    }

    void playRound() {
        if (currState == State.INIT()) {
            spawn();
        }
        else { // currState == State.IDLE()
            return;
        }
    }

}
