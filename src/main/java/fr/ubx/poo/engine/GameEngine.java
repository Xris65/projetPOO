/*
 * Copyright (c) 2020. Laurent Réveillère
 */
package fr.ubx.poo.engine;

import fr.ubx.poo.game.*;
import fr.ubx.poo.model.decor.Bomb;
import fr.ubx.poo.model.decor.Door;
import fr.ubx.poo.model.go.BombObject;
import fr.ubx.poo.model.go.character.Monster;
import fr.ubx.poo.model.go.character.Player;
import fr.ubx.poo.view.sprite.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


/**
 * The type Game engine.
 */
public final class GameEngine {

    private static AnimationTimer gameLoop;
    private final String windowTitle;
    private final Game game;
    private final Player player;
    private final List<Sprite> sprites = new ArrayList<>();
    private StatusBar statusBar;
    private Pane layer;
    private Input input;
    private Stage stage;
    private Sprite spritePlayer;
    private final ArrayList<Sprite> spriteMonsters = new ArrayList<>();
    private final ArrayList<Sprite> spriteBombs = new ArrayList<>();
    private final ArrayList<SpriteExplosion> spriteExplosions = new ArrayList<>();


    /**
     * Instantiates a new Game engine.
     *
     * @param windowTitle the window title
     * @param game        the game
     * @param stage       the stage
     */
    public GameEngine(final String windowTitle, Game game, final Stage stage) {
        this.windowTitle = windowTitle;
        this.game = game;
        this.player = game.getPlayer();
        initialize(stage, game);
        buildAndSetGameLoop();
    }

    /**
     * Initializes the environment of the game, including scene,status bar,sprites,etc.
     * @param stage the stage.
     * @param game the game.
     */
    private void initialize(Stage stage, Game game) {
        this.stage = stage;
        Group root = new Group();
        layer = new Pane();

        int height = game.getWorld().dimension.height;
        int width = game.getWorld().dimension.width;
        int sceneWidth = width * Sprite.size;
        int sceneHeight = height * Sprite.size;
        Scene scene = new Scene(root, sceneWidth, sceneHeight + StatusBar.height);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

        stage.setTitle(windowTitle);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        input = new Input(scene);
        root.getChildren().add(layer);
        statusBar = new StatusBar(root, sceneWidth, sceneHeight);
        // Create decor sprites
        game.getWorld().forEach((pos, d) -> sprites.add(SpriteFactory.createDecor(layer, pos, d)));
        spritePlayer = SpriteFactory.createPlayer(layer, player);
        World w = game.getWorld();
        // Create the monsters of the world
        for (Monster m : w.getMonsters()) {
            spriteMonsters.add(SpriteFactory.createMonster(layer, m));
        }
    }

    /**
     * Builds and set game loop.
     */
    protected final void buildAndSetGameLoop() {
        gameLoop = new AnimationTimer() {
            public void handle(long now) {
                // Check keyboard actions
                processInput(now);

                // Do actions
                update(now);

                // Graphic update
                render();
                statusBar.update(game);
            }
        };
    }

    /**
     * Processes the keyboard input of the player.
     * @param now actual time
     */
    private void processInput(long now) {
        if (input.isExit()) {
            gameLoop.stop();
            Platform.exit();
            System.exit(0);
        }
        if (input.isMoveDown()) {
            player.requestMove(Direction.S);
        }
        if (input.isMoveLeft()) {
            player.requestMove(Direction.W);
        }
        if (input.isMoveRight()) {
            player.requestMove(Direction.E);
        }
        if (input.isMoveUp()) {
            player.requestMove(Direction.N);
        }
        if (input.isBomb()) {
            World world = game.getWorld();
            if (world.get(player.getPosition()) == null && !world.isThereAMonsterAt(player.getPosition())) {
                if (player.getNumberOfBombs() < player.getBombCapacity()) {

                    BombObject bomb = new BombObject(game, player.getPosition(), player.getBombRange(), now);
                    game.getWorld().getBombs().add(bomb);
                    spriteBombs.add(new SpriteBomb(layer, bomb));
                    game.getWorld().set(player.getPosition(), new Bomb());
                    //game.createExplosions(explosions,layer,player);
                    player.removeBomb();
                }
            }
        }
        if (input.isKey()) {
            Position playerPos = player.getPosition();
            World world = game.getWorld();
            for (Direction d : Direction.values()) {
                if (world.get(d.nextPosition(playerPos)) instanceof Door
                        && d == player.getDirection()
                        && (((Door) world.get(d.nextPosition(playerPos))).isClosed())) {
                    if (player.getNumberOfKeys() > 0) {
                        player.removeKey();
                        game.setToChange(true);
                        world.set(d.nextPosition(playerPos), new Door(false, false));
                        game.getWorldManager().changeWorld(true,game);
                        break;
                    }
                }
            }
        }
        input.clear();
    }

    private void showMessage(String msg, Color color) {
        Text waitingForKey = new Text(msg);
        waitingForKey.setTextAlignment(TextAlignment.CENTER);
        waitingForKey.setFont(new Font(60));
        waitingForKey.setFill(color);
        StackPane root = new StackPane();
        root.getChildren().add(waitingForKey);
        Scene scene = new Scene(root, 400, 200, Color.WHITE);
        stage.setTitle(windowTitle);
        stage.setScene(scene);
        input = new Input(scene);
        stage.show();
        new AnimationTimer() {
            public void handle(long now) {
                processInput(now);
            }
        }.start();
    }

    /**
     * Updates the game environment, including monsters,player position and status,game map and status,etc.
     *
     * @param now actual time.
     */
    private void update(long now) {
        player.update(now);
        game.getWorldManager().updateMonstersOnWorlds(now);
        game.getWorldManager().verifyMonsterCollisionsWithPlayer(now);
        if (!player.isAlive()) {
            gameLoop.stop();
            showMessage("Perdu!", Color.RED);
        }
        Iterator<Monster> monsterIterator = game.getWorld().getMonsters().iterator();
        while (monsterIterator.hasNext()) {
            Monster m = monsterIterator.next();
            if (!m.isAlive()) {
                Iterator<Sprite> spriteMonstersIterator = spriteMonsters.iterator();
                while (spriteMonstersIterator.hasNext()) {
                    SpriteMonster spriteMonster = (SpriteMonster) spriteMonstersIterator.next();
                    if (spriteMonster.isToRemove()) {
                        spriteMonster.remove();
                        spriteMonstersIterator.remove();
                    }
                }
                //spriteMonsters.removeIf(Sprite::isToRemove);
                monsterIterator.remove();
            }
        }
        if (player.isWinner()) {
            gameLoop.stop();
            showMessage("Gagné", Color.BLUE);
        }
        if (game.isToChange()) {
            game.setToChange(false);
            spriteMonsters.removeIf(Objects::nonNull);
            initialize(stage, game);
        }

        // For every bomb in every world, check if any is at bombphase 5 (ready to explode)
        // if any, make them explode.
        for (World gameWorld : game.getWorldManager().getWorlds()) {
            for (BombObject bomb : gameWorld.getBombs()) {
                bomb.update(now);
                if (bomb.getBombPhase() == 5) {
                    ArrayList<Position> zone = bomb.getBombZone();
                    bomb.explode(now, zone);
                    for (Position position : zone) {
                        // For each position the bomb exploded, add an explosion sprite
                        spriteExplosions.add(new SpriteExplosion(layer, position, bomb.getWorld()));
                    }
                }
            }
        }

        // For every world, clear bombs that are phase 5
        for (World gameWorld : game.getWorldManager().getWorlds()) {
            Iterator<BombObject> bombObjectIterator = gameWorld.getBombs().iterator();
            while (bombObjectIterator.hasNext()) {
                BombObject bomb = bombObjectIterator.next();
                if (bomb.getBombPhase() == 5) {
                    gameWorld.clear(bomb.getPosition());
                    bombObjectIterator.remove();
                }
            }
        }
    }

    /**
     * Renders the graphical interface of the game, including the world,monsters,decorations,player,bombs,etc.
     */
    private void render() {
        if (game.getWorld().isChanged()) {

            game.getWorld().setChanged(false);
            sprites.forEach(Sprite::remove);
            sprites.removeIf(self -> self.getImageView() == null);
            game.getWorld().forEach((pos, d) -> {
                if (!(d instanceof Bomb)) { // Doesn't reload bomb textures (not needed)
                    sprites.add(SpriteFactory.createDecor(layer, pos, d));
                }
            });
        }
        sprites.forEach(Sprite::render);
        // last rendering to have player in the foreground
        spritePlayer.render();
        ((SpritePlayer) spritePlayer).updatePlayerTransparency();

        for (Sprite monster : spriteMonsters) {
            monster.render();
        }
        spriteMonsters.forEach(self -> {
            if (self.isToRemove()) {
                self.remove();
            }
        });
        spriteMonsters.removeIf(Sprite::isToRemove);


        // BOMB
        for (Sprite bomb : spriteBombs) {
            bomb.render();
            if (bomb.isToRemove()) {
                player.addBomb();
                bomb.remove();
            }
        }

        spriteBombs.removeIf(Sprite::isToRemove);

        spriteExplosions.forEach(self -> {
            if (self.isToRemove()) {
                self.remove();
            }
            if (self.getWorld().equals(game.getWorld())) {
                self.render();
                self.adjustOpacity();
            }

        });
        spriteExplosions.removeIf(Sprite::isToRemove);
    }

    /**
     * Starts the game loop.
     */
    public void start() {
        gameLoop.start();
    }
}
