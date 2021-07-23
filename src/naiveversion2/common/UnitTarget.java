package naiveversion2.common;

import aic2021.user.*;

public class UnitTarget {
    public UnitType unitType;
    public Location loc;

    public UnitTarget(UnitType unitType, Location loc) {
        this.unitType = unitType;
        this.loc = loc;
    }
}
