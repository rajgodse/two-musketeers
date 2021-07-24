package naiveversion2.common;

import aic2021.user.*;

import naiveversion2.MyUnit;

public class Nav {
    UnitController uc;
    MyUnit human;
    final int MAX = Integer.MAX_VALUE;
    final Direction[] dirs = Direction.values();

    Location dest;
    Direction currentWallHugDirection;
    int roundBlocked = -1;
    int bestDirection;

    public Direction lastExploreDir;
    public int canMoveSemaphor = 1;
    final int EXPLORE_BOREDOM = 20;
    int boredom;

    public static class RightLect {
        public static int RIGHT() {return 0;}
        public static int LEFT() {return 1;}
    }

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

    public Boolean testDirectionForMountains(Location l, Direction d) {
        Location sightLocation = l;
        while(human.uc.canSenseLocation(sightLocation)) {
            if(human.uc.hasMountain(sightLocation) || human.uc.isOutOfMap(sightLocation)) {
                return true;
            }
            sightLocation = sightLocation.add(d);
        }
        return false;
    }

    public Direction rotateBasedOnRightLeft(Direction d, int i) {
        if( i == 0 ) { return d.rotateRight(); }
        if( i == 1 ) { return d.rotateLeft(); }
        return null;
    }

    public Direction rotate90BasedOnRightLeft(Direction d, int i) {
        if( i == 0 ) { return d.rotateRight().rotateRight(); }
        if( i == 1 ) { return d.rotateLeft().rotateLeft(); }
        return null;
    }

    public Direction wallHug(Direction directionOfWall, int i) {
        Direction iterDirection = directionOfWall;
        int count = 0;
        while( count < 8) {
            if(uc.canMove(iterDirection)) { return iterDirection; }
            iterDirection = rotateBasedOnRightLeft(iterDirection, i);
            count++;
        }
        if (count != 8) { return iterDirection; }
        return null;
    }



    public int getBestDirection(Direction toLocation) {
        int rightCount = 0;
        int leftCount = 0;
        int count = 0;
        Direction iterDirection = toLocation;
        while (count < 8) {
            if (uc.canMove(iterDirection)) {
                rightCount = count;
                break;
            }
            count ++;
            iterDirection = iterDirection.rotateRight();
        }
        count = 0;
        iterDirection = toLocation;
        while (count < 8) {
            if (uc.canMove(iterDirection)) {
                leftCount = count;
                break;
            }
            count ++;
            iterDirection = iterDirection.rotateLeft();
        }
        if (rightCount <= leftCount) { return 0;}
        else { return 1;}
    }

    public Boolean canMoveAtAll() {
        for(Direction d: Direction.values()) {
            if(uc.canMove(d)) { return true; }
        }
        return false;
    }

    public Boolean canMoveAllDirs() {
        for(Direction d: Direction.values()) {
            if(!uc.canMove(d)) { return false; }
        }
        return true;
    }

    public int returnBestDirection() {
        return bestDirection;
    }

    Direction[] getNearestDirections(Direction Dir) {
        Direction[] nearestDirections = new Direction[]{Dir, Dir.rotateRight(), Dir.rotateLeft(), Dir.rotateRight().rotateRight(), Dir.rotateLeft().rotateLeft(), Dir.rotateRight().rotateRight().rotateRight(), Dir.rotateLeft().rotateLeft().rotateLeft()};
        return nearestDirections;
    }

    public Boolean closerTo(Direction main, Direction optionOne, Direction optionTwo) {
        Direction[] mainDirs = getNearestDirections(main);
        int count1 = 0;
        for (Direction d: mainDirs) {
            if(d.equals(optionOne)) {
                break;
            }
            count1++;
        }
        int count2 = 0;
        for (Direction d: mainDirs) {
            if(d.equals(optionTwo)) {
                break;
            }
            count2++;
        }
        if (count1 <= count2) { return true;}
        else {return false; }
    }

    public Boolean blockedByAWall(Direction toLocation){
        Boolean huggingEdgeOfMap = uc.isOutOfMap(uc.getLocation().add(currentWallHugDirection));
        Direction movementDirection = rotate90BasedOnRightLeft(currentWallHugDirection, bestDirection);
        Boolean goingWrongWay = closerTo(toLocation, movementDirection.opposite(), movementDirection);
        return huggingEdgeOfMap && goingWrongWay;
    }

	public Direction goToLocation(Location l) {
        Direction toLocation = uc.getLocation().directionTo(l);
        Boolean mountains = !testDirectionForMountains(uc.getLocation(),toLocation); //Checks if there are mountains directly in the path we want to take
        Boolean roundBlockedOrNotSet = (roundBlocked == -1 || uc.getRound() - roundBlocked >=15); //Once blocked, human hugs a wall for at least 15 rounds to avoid loops caused by low visibility
        uc.println(mountains + " " + roundBlockedOrNotSet + " " + canMoveAllDirs() + " ");
        if(uc.canMove(toLocation) && ((mountains && roundBlockedOrNotSet) || canMoveAllDirs())) {
            currentWallHugDirection = null;
            roundBlocked = -1;
            uc.println("Moving directly towards the goal");
            return toLocation;
        }
        else if(canMoveAtAll()) { //Check to make sure we arent in lag from moving on a previous turn
            uc.println("movement blocked");
            uc.println("hugging wall, difference is " + (uc.getRound() - roundBlocked));
            if (currentWallHugDirection == null) { //beginning to wall hug, best direction is whether we hug to the right or left
                roundBlocked = uc.getRound();
                bestDirection = getBestDirection(toLocation);
                Direction directionToMove = wallHug(toLocation, bestDirection);
                if (directionToMove != null) {
                    uc.println("direction to move: " + directionToMove + ", best direction: " + bestDirection);
                    currentWallHugDirection = rotate90BasedOnRightLeft(directionToMove, (bestDirection + 1) % 2);
                    uc.println("First time hugging, current wall hug direction set to: " + currentWallHugDirection + ", best direction: " + bestDirection);
                }
                return directionToMove;
            } else {
                if(blockedByAWall(toLocation)) {
                    uc.println("Blocked by a Wall, changing direction");
                    bestDirection = (bestDirection + 1)%2;
                }
                Direction directionToMove = wallHug(currentWallHugDirection, bestDirection);
                uc.println("direction to move: " + directionToMove + ", best direction: " + bestDirection);
                if (directionToMove != null) {
                    uc.println("current wall hug direction set to: " + currentWallHugDirection);
                    currentWallHugDirection = rotate90BasedOnRightLeft(directionToMove, (bestDirection + 1) % 2);
                }
                return directionToMove;
            }
        }
        else {
            uc.println("In lag, cannot move, best direction is: " + bestDirection);
            return Direction.ZERO;
        }
    }
}