package naiveversion2.common;

import aic2021.user.*;

import naiveversion2.MyUnit;

public class Nav {
    UnitController uc;
    MyUnit robot;

    final int MAX = Integer.MAX_VALUE;
    final int rows = 5;
    final int cols = 5;
    final int halfRows = rows / 2;
    final int halfCols = cols / 2;

    Location[][] map;
    double[][] costs;
    double[][] cooldownPenalties;

    Location dest;
    int closestDistanceToDest;
    int turnsSinceClosestDistanceDecreased;


    public Direction lastExploreDir;
    final int EXPLORE_BOREDOM = 20;
    int boredom;

    public Nav(MyUnit robot) {
        this.robot = robot;
        this.uc = robot.uc;
        costs = new double[rows][cols];
        cooldownPenalties = new double[rows][cols];
        map = new Location[rows][cols];
        dest = null;
        closestDistanceToDest = Integer.MAX_VALUE;
        turnsSinceClosestDistanceDecreased = 0;
        lastExploreDir = null;
    }

    void setDest(Location d) {
        dest = d;
        closestDistanceToDest = Integer.MAX_VALUE;
        turnsSinceClosestDistanceDecreased = 0;
    }

    void updateLocalMap() {
        Location currLoc = robot.uc.getLocation();
        Location loc;
        for(int i = rows - 1; i >= 0; i--) {
            int dy = halfRows - i;
            for(int j = cols - 1; j >= 0; j--) {
                int dx = j - halfCols;
                loc = currLoc.add(dx, dy);
                if(robot.uc.canSenseLocation(loc) && robot.uc.senseUnitAtLocation(loc) == null) {
                    map[i][j] = loc;
                } else {
                    map[i][j] = null;
                }
            }
        }
    }

    double heuristic(Location loc1, Location loc2) {
        if(loc1 == null || loc2 == null)
            return MAX;
        int dx = Math.abs(loc1.x - loc2.x);
        int dy = Math.abs(loc1.y - loc2.y);
        return Math.max(dx, dy);
    }

	public Direction explore() {
        uc.println("Exploring");
        if(!robot.uc.canMove())
            return null;
        
		if(lastExploreDir == null) {
            uc.println("changing last Explore Dir");
            Direction oppositeFromHome = robot.uc.getLocation().directionTo(robot.home).opposite();
            Direction []oppositeFromHomeDirs = {oppositeFromHome, oppositeFromHome.rotateLeft(), oppositeFromHome.rotateRight()};
            lastExploreDir = oppositeFromHomeDirs[(int)(Math.random() * 3)];
			boredom = 0;
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
			lastExploreDir = newDirChoices[(int) (Math.random() * newDirChoices.length)];
            return lastExploreDir;
		}
        boredom++;
        
		if(robot.uc.isOutOfMap(robot.uc.getLocation().add(lastExploreDir))) {
            uc.println("changing last Explore Dir because of a wall");
            // lastExploreDir = lastExploreDir.opposite();
            Direction tempExploreDir = null;
            if((int) (Math.random() * 2) == 0) {
                tempExploreDir = robot.util.turnLeft90(lastExploreDir);
                if(robot.uc.isOutOfMap(robot.uc.getLocation().add(tempExploreDir))) {
                    tempExploreDir = robot.util.turnRight90(lastExploreDir);
                }
            }
            else {
                tempExploreDir = robot.util.turnRight90(lastExploreDir);
                if(robot.uc.isOutOfMap(robot.uc.getLocation().add(tempExploreDir))) {
                    tempExploreDir = robot.util.turnLeft90(lastExploreDir);
                }
            lastExploreDir = tempExploreDir;
            }
            return lastExploreDir;
        }
        uc.println("Exploring regularly. Direction before descent: " + lastExploreDir);
		return lastExploreDir;
	}
}