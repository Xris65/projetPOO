package fr.ubx.poo.game;

import fr.ubx.poo.model.decor.Door;
import fr.ubx.poo.model.go.character.Monster;
import fr.ubx.poo.model.go.character.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

/**
 * The type World manager.
 */
public class WorldManager {
    private final ArrayList<World> worlds = new ArrayList<>();
    private int maxWorldsReached = 0;
    private int currentWorldIndex = -1;
    private final String worldPath;
    private String prefix;
    private int maxLevel;

    /**
     * Gets worlds.
     *
     * @return the worlds
     */
    public ArrayList<World> getWorlds() {
        return worlds;
    }

    /**
     * Sets max level.
     *
     * @param maxLevel the max level
     */
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * Sets prefix of the world file.
     *
     * @param prefix the prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Instantiates a new World manager.
     *
     * @param worldPath the world path
     */
    public WorldManager(String worldPath) {
        this.worldPath = worldPath;
    }

    /**
     * Adds a world to the worlds array, increasing the value of max worlds reached.
     *
     * @param world the world
     */
    public void addWorld(World world) {
        worlds.add(world);
        maxWorldsReached++;
    }

    /**
     * Gets next world.
     *
     * @param game the game
     * @return the next world
     */
    public World getNextWorld(Game game) {
        // load next world and give it to user
        // and add it to arraylist
        currentWorldIndex++;
        if (currentWorldIndex < maxWorldsReached) {
            return worlds.get(currentWorldIndex);
        } else if (currentWorldIndex >= maxLevel) {
            currentWorldIndex--;
        } else {
            World nextWorld;
            nextWorld = readFromFile(String.format("%s%d.txt", prefix, maxWorldsReached + 1));
            nextWorld.setMonsters(nextWorld.findMonsters(game));
            addWorld(nextWorld);
            return nextWorld;
        }
        return null;
    }

    /**
     * Gets previous world.
     *
     * @return the previous world
     */
    public World getPreviousWorld() {
        currentWorldIndex--;
        return worlds.get(currentWorldIndex);
    }


    /**
     * Changes world.
     *
     * @param goingUp boolean that confirms if we go to next or previous world.
     */
    public void changeWorld(boolean goingUp, Game game) {
        if (goingUp) {
            World nextWorld = getNextWorld(game);
            if (nextWorld == null) {
                return;
            } else {
                game.setWorld(nextWorld);
            }
        } else {
            game.setWorld(getPreviousWorld());
        }
        World world = game.getWorld();
        Dimension dimension = world.dimension;
        for (int x = 0; x < dimension.width; x++) {
            for (int y = 0; y < dimension.height; y++) {
                if (world.get(new Position(x, y)) instanceof Door) {
                    Door door = (Door) world.get(new Position(x, y));
                    if (!door.isClosed()) {
                        if ((goingUp && door.isPrev())
                                || !(goingUp) && !(door.isPrev())) { //Si c'est la bonne porte
                            game.getPlayer().setPosition(new Position(x, y));
                            break;
                        }
                    }
                }
            }
        }
        world.setChanged(true);
    }

    /**
     * Updates monsters on all worlds.
     *
     * @param now the actual time.
     */
    public void updateMonstersOnWorlds(long now) {
        for (World w : worlds) {
            w.updateMonsters(now);
        }
    }

    /**
     * Verify monster collisions with player.
     *
     * @param now the actual time.
     */
    public void verifyMonsterCollisionsWithPlayer(long now) {
        World w = worlds.get(currentWorldIndex);
        for (Monster m : w.getMonsters()) {
            Game game = m.getGame();
            Player player = game.getPlayer();
            if (m.getPosition().equals(game.getPlayer().getPosition()))
                if (player.isVulnerable())
                    player.loseLife(now);
        }
    }

    /**
     * Gets current world index.
     *
     * @return the current world index
     */
    public int getCurrentWorldIndex() {
        return currentWorldIndex;
    }

    private World readFromFile(String filename) {
        WorldEntity[][] read;
        try {
            File myObj = new File(worldPath, filename);
            Scanner measuresReader = new Scanner(myObj);

            // Get map height and width from file
            int height = 0;
            int width = -1;
            for (; measuresReader.hasNextLine(); height++) {
                int tmp = measuresReader.nextLine().length();
                if (width != -1) {
                    if (tmp != width) {
                        throw new RuntimeException("Map width is not consistent");
                    }
                }
                width = tmp;
            }
            if (height == 0) {
                throw new RuntimeException("Can't read empty file");
            }
            measuresReader.close();

            // Get map content
            Scanner myReader = new Scanner(myObj);

            read = new WorldEntity[height][width];
            String line;
            for (int y = 0; myReader.hasNextLine(); y++) {
                line = myReader.nextLine();
                for (int x = 0; x < line.length(); x++) {
                    Optional<WorldEntity> readInFileToWorldEntity = WorldEntity.fromCode(line.charAt(x));
                    if (readInFileToWorldEntity.isPresent()) {
                        read[y][x] = readInFileToWorldEntity.get();
                    } else {
                        throw new RuntimeException(String.format("Character %c is invalid\n", line.charAt(x)));
                    }
                }

            }
            myReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("File %s not found, its path: %s\n", filename, worldPath + '/' + filename));
        }
        return new World(read);
    }

    /**
     * Gets the current world number.
     *
     * @return the number of the world
     */
    public int getWorldNumber(){
        return getCurrentWorldIndex() + 1;
    }

}

