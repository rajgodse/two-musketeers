package naiveversion2.common;

import aic2021.user.*;

public class Util {
    public Util() {}

    public static class RotationDirection {
        public static int numValues() { return 2; }
        public static int CLOCKWISE() { return 0; }
        public static int COUNTERCLOCKWISE() { return 1; }
    }

    public final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public final Direction[] orthogonalDirs = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST,
    };

    public final Direction[] diagonalDirs = {
        Direction.NORTHWEST,
        Direction.NORTHEAST,
        Direction.SOUTHEAST,
        Direction.SOUTHWEST,
    };

    public final Direction[] scoutDirs = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST,
        Direction.NORTHWEST,
        Direction.SOUTHEAST,
        Direction.NORTHEAST,
        Direction.SOUTHWEST,
    };

    public static class DirectionPreference {
        public static int numValues() { return 3; }
        public static int RANDOM() { return 0; }
        public static int ORTHOGONAL() { return 1; }
        public static int DIAGONAL() { return 2; }
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    Direction randomDirection() {
        return directions[(int)(Math.random() * directions.length)];
    }

    Direction randomOrthogonalDirection() {
        return orthogonalDirs[(int)(Math.random() * orthogonalDirs.length)];
    }

    Direction randomDiagonalDirection() {
        return diagonalDirs[(int)(Math.random() * diagonalDirs.length)];
    }

    Direction[] getOrderedDirections(int pref) {
        Direction dir = pref == DirectionPreference.ORTHOGONAL() ? randomOrthogonalDirection() :
                        pref == DirectionPreference.DIAGONAL() ? randomDiagonalDirection() :
                                                            randomDirection();

        return new Direction[]{dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(), dir.opposite().rotateRight(), dir.opposite(),
                dir.opposite().rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateRight()};
    }

    Direction[] getOrderedDirections(Direction dir) {
        return new Direction[]{dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(), dir.opposite().rotateRight(), dir.opposite(),
                dir.opposite().rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateRight()};
    }

    Direction rotateInSpinDirection(int rotationDirection, Direction dir) {
        return rotationDirection == RotationDirection.COUNTERCLOCKWISE() ? dir.rotateLeft() : dir.rotateRight();
    }

    Direction rotateOppositeSpinDirection(int rotationDirection, Direction dir) {
        return rotationDirection == RotationDirection.COUNTERCLOCKWISE() ? dir.rotateRight() : dir.rotateLeft();
    }

    int switchSpinDirection(int rotationDirection) {
        return rotationDirection == RotationDirection.COUNTERCLOCKWISE()
                ? RotationDirection.CLOCKWISE() : RotationDirection.COUNTERCLOCKWISE();
    }
    
    Direction turnLeft90(Direction dir) {
        return dir.rotateLeft().rotateLeft();
    }

    Direction turnRight90(Direction dir) {
        return dir.rotateRight().rotateRight();
    }

    Direction[] getAboutToDieBuildOrder(Direction dir) {
        if(dir == Direction.NORTH || dir == Direction.SOUTH ||
            dir == Direction.EAST || dir == Direction.WEST) {
            return new Direction[]{dir, dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(), dir.opposite()};
        } else {
            return new Direction[]{dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().opposite(), dir.rotateRight().opposite()};
        }
    }
}