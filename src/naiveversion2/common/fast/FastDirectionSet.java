package naiveversion2.common.fast;

import aic2021.user.*;

public class FastDirectionSet {
    public int set;

    public FastDirectionSet() {
        set = 0;
    }

    public void add(Direction dir) {
        set |= 1 << dir.ordinal();
    }

    public void remove(Direction dir) {
        set &= ~(1 << dir.ordinal());
    }

    public boolean contains(Direction dir) {
        return (set & dir.ordinal()) != 0;
    }
}
