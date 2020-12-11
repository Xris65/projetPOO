package fr.ubx.poo.model.go.character;

import fr.ubx.poo.game.Direction;
import fr.ubx.poo.game.Game;
import fr.ubx.poo.game.Position;
import fr.ubx.poo.game.World;
import fr.ubx.poo.model.Entity;
import fr.ubx.poo.model.Movable;
import fr.ubx.poo.model.decor.Bonus;
import fr.ubx.poo.model.go.GameObject;

import java.util.function.Function;

public class Monster extends Character {

    Direction direction = Direction.random();
    private boolean moveRequested = false;
    private int lives = 1;
    private final World world;

    public boolean isAlive() {
        return lives > 0;
    }

    public int getLives() {
        return lives;
    }

    public void removeHP(int hp) {
        this.lives -= hp;
    }

    // time to move in ms
    public Monster(Game game, Position position, int timeToMove, World w) {
        super(game, position);
        super.setTimeToAct(timeToMove);
        this.world = w;
    }

    public boolean isThereAValidMovement() {
        for (Direction d : Direction.values()) {
            if (canMove(d))
                return true;
        }
        return false;
    }

    public void requestMove() {
        if (!isThereAValidMovement())
            return;
        while (!canMove(direction))
            this.direction = Direction.random();
        moveRequested = true;
    }

    public void update(long now) {
        super.actionIfTime(now, this::requestMove);
        if (moveRequested) {
            if (canMove(direction)) {
                doMove(direction);
            }
        }
        moveRequested = false;
    }


    public void doMove(Direction direction) {
        Position nextPos = direction.nextPosition(getPosition());
        setPosition(nextPos);
    }

    public Direction getDirection() {
        return direction;
    }


}
