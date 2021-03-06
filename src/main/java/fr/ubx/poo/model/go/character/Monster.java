package fr.ubx.poo.model.go.character;

import fr.ubx.poo.game.Direction;
import fr.ubx.poo.game.Game;
import fr.ubx.poo.game.Position;
import fr.ubx.poo.game.World;

import java.util.ArrayList;
import java.util.Random;

public class Monster extends Character {

    /**
     * Instantiates a new Monster.
     * @param game the game.
     * @param position spawn position.
     * @param timeToMove time period of monsters' every movement.
     */
    public Monster(Game game, Position position, int timeToMove) {
        super(game, position, Direction.random());
        super.setTimeToAct(timeToMove);
        setLives(1);
    }

    /**
     * Checks if the monster can move on the given direction in the given world.
     * @param direction the direction.
     * @param world     the world.
     * @return true if monster can move with given direction, false if not.
     */
    public boolean canMove(Direction direction, World world) {
        if (super.canMove(direction, world)) {
            Position nextPosition = direction.nextPosition(getPosition());
            boolean existsMonster = false;
            for (Monster monster : world.getMonsters()) {
                if (monster.getPosition().equals(nextPosition)) {
                    existsMonster = true;
                    break;
                }
            }
            return !existsMonster;
        }
        return false;
    }

    /**
     * Returns an array list containing the valid movements from current position in the given world.
     * @param world the world.
     * @return an array list containing the valid movements.
     */
    public ArrayList<Direction> validMovements(World world) {
        ArrayList<Direction> possibleMoveDirections = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (canMove(d, world)) {
                possibleMoveDirections.add(d);
            }
        }
        return possibleMoveDirections;
    }

    /**
     * Requests a monster to move if it can.
     * @param world the world.
     */
    public void requestMove(World world) {
        // creates a list of possible movements
        ArrayList<Direction> possibleMoves = validMovements(world);
        int movesSize = possibleMoves.size();
        if (movesSize != 0) { // to prevent out of bound in empty array
            // gets a random move in possible moves
            // prevents losing time with random
            this.direction = possibleMoves.get(new Random().nextInt(movesSize));
        }
        this.moveRequested = true;
    }

    /**
     * Updates the position of the monster, ensuring it moves every (timeToMove) time.
     * @param now actual time.
     * @param world the world.
     */
    public void update(long now, World world) {
        // Every getTimeToAct(), here 1 s, request a move
        super.actionIfTime(now, () -> {
            this.requestMove(world);
            setLastActionTime(now);
        });
        super.update(now);
    }

    /**
     * Returns the current direction of the monster.
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }


}
