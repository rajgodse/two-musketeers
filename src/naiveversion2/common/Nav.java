package naiveversion2.common;

import aic2021.user.*;

import naiveversion2.MyUnit;

public class Nav {
    UnitController uc;
    MyUnit human;
    final int MAX = Integer.MAX_VALUE;
    final Direction[] dirs = Direction.values();

    Location dest;
    int closestDistanceToDest;
    int turnsSinceClosestDistanceDecreased;
    int delx, dely;


    public Direction lastExploreDir;
    public int canMoveSemaphor = 1;
    final int EXPLORE_BOREDOM = 20;
    int boredom;

    public Nav(MyUnit human) {
        this.uc = human.uc;
        this.human = human;
        dest = null;
        lastExploreDir = null;
    }


	public Direction explore() {
        uc.println("Exploring");
        if(!uc.canMove())
            return null;
        
		if(lastExploreDir == null) {
            uc.println("changing last Explore Dir");
            Direction opposite = uc.getLocation().directionTo(human.home).opposite();

            Direction []oppositeFromHomeDirs = {opposite, opposite.rotateLeft(), opposite.rotateRight()};

            lastExploreDir = oppositeFromHomeDirs[(int)(human.uc.getRandomDouble() * 3)];
			boredom = 0;
            return lastExploreDir;
        }
        Location possibleFriendly = null;
        if(!human.uc.canSenseLocation(human.home)) {
            for(UnitInfo unit: human.Friendlies) {
                if(unit.getType().equals(UnitType.EXPLORER)) {
                    possibleFriendly = unit.getLocation();
                }
            }
        }
        if(possibleFriendly != null && uc.getRound() >= 50) {
            Direction opposite = uc.getLocation().directionTo(possibleFriendly).opposite();
            Direction []oppositeFromFriendlyDirs = {opposite, opposite.rotateLeft(), opposite.rotateRight()};

            lastExploreDir = oppositeFromFriendlyDirs[(int)(human.uc.getRandomDouble() * 3)];
            boredom = 0;
            uc.println("Running away from other Explorer. Direction: " + opposite);
            return lastExploreDir;
        }
		if(boredom >= EXPLORE_BOREDOM) {
            uc.println("changing last Explore Dir because of boredom");
            boredom = 0;
            // Direction[] newDirChoices = {
            //     lastExploreDir.rotateLeft().rotateLeft(),
            //     lastExploreDir.rotateLeft(),
            //     lastExploreDir,
            //     lastExploreDir.rotateRight(),
            //     lastExploreDir.rotateRight().rotateRight()};
            Direction[] newDirChoices = {
                lastExploreDir.rotateLeft(),
                lastExploreDir,
                lastExploreDir.rotateRight(),};
			lastExploreDir = newDirChoices[(int) (human.uc.getRandomDouble() * newDirChoices.length)];
            return lastExploreDir;
		}
        boredom++;
        Boolean wallMiddle = !human.uc.canMove(lastExploreDir);
        Boolean wallRight = !human.uc.canMove(lastExploreDir.rotateLeft());
        Boolean wallLeft =  !human.uc.canMove(lastExploreDir.rotateRight());
        if(wallMiddle && wallRight && wallLeft) {
            if(canMoveSemaphor == 0) {
                canMoveSemaphor = 1;
                uc.println("changing last Explore Dir because of a wall or mountain");
                // lastExploreDir = lastExploreDir.opposite();
                Direction tempExploreDir = null;
                if ((int) (human.uc.getRandomDouble() * 2) == 0) {
                    tempExploreDir = human.util.turnLeft90(lastExploreDir);
                    if (human.uc.isOutOfMap(human.uc.getLocation().add(tempExploreDir))) {
                        tempExploreDir = human.util.turnRight90(lastExploreDir);
                    }
                } else {
                    tempExploreDir = human.util.turnRight90(lastExploreDir);
                    if (human.uc.isOutOfMap(human.uc.getLocation().add(tempExploreDir))) {
                        tempExploreDir = human.util.turnLeft90(lastExploreDir);
                    }
                    lastExploreDir = tempExploreDir;
                }
                return lastExploreDir;
            }
        else {
            canMoveSemaphor--;
            }
        }
        uc.println("Exploring regularly. Direction before descent: " + lastExploreDir);
		return lastExploreDir;
	}

    int indexOfDirection() {
        // Assuming for now that +x = East and +y = North
        if(delx > 0) {
            if(dely > 0) {
                return 7;
            }
            else if (dely == 0) {
                return 6;
            }
            else {
                return 5;
            }
        }
        else if (delx == 0) {
            if(dely > 0) {
                return 0;
            }
            else {
                return 4;
            }
        }
        else {
            if(dely > 0) {
                return 1;
            }
            else if (dely == 0) {
                return 2;
            }
            else {
                return 3;
            }
        }
    }

    // untested
	public Direction goToLocation(Location l) {
        uc.println("Navigating");
        // if(!uc.canMove())
        //    return null;
        Location curr = uc.getLocation();
        delx = l.x - curr.x;
        dely = l.y - curr.y;
        int i = indexOfDirection();
        if(uc.canMove(dirs[i])) return dirs[i];
        for(int j = (i + 1) % 8; j != i; j = (j + 1) % 8) {
            if(uc.canMove(dirs[j])) return dirs[j];
        }
        return null;
    }
}