/*
 * Copyright (c) 2020. Laurent Réveillère
 */

package fr.ubx.poo.engine;

import static fr.ubx.poo.view.image.ImageResource.*;

import fr.ubx.poo.game.Game;
import fr.ubx.poo.model.go.character.Player;
import fr.ubx.poo.view.image.ImageFactory;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * The type Status bar.
 */
public class StatusBar {
    /**
     * The constant height.
     */
    public static final int height = 55;
    private final HBox hBox = new HBox();
    private final Text liveValue = new Text();
    private final Text bombsValue = new Text();
    private final Text rangeValue = new Text();
    private final Text keyValue = new Text();
    private final HBox level = new HBox();
    private int gameLevel = 1;
    private final DropShadow ds = new DropShadow();


    /**
     * Instantiates a new Status bar.
     *
     * @param root        the root
     * @param sceneWidth  the scene width
     * @param sceneHeight the scene height
     */
    public StatusBar(Group root, int sceneWidth, int sceneHeight) {
        // Status bar

        level.getStyleClass().add("level");
        level.getChildren().add(new ImageView(ImageFactory.getInstance().getDigit(gameLevel)));

        ds.setRadius(5.0);
        ds.setOffsetX(3.0);
        ds.setOffsetY(3.0);
        ds.setColor(Color.color(0.5f, 0.5f, 0.5f));


        HBox status = new HBox();
        status.getStyleClass().add("status");
        HBox live = statusGroup(ImageFactory.getInstance().get(HEART), this.liveValue);
        HBox bombs = statusGroup(ImageFactory.getInstance().get(BANNER_BOMB), bombsValue);
        HBox range = statusGroup(ImageFactory.getInstance().get(BANNER_RANGE), rangeValue);
        HBox key = statusGroup(ImageFactory.getInstance().get(KEY), keyValue);
        status.setSpacing(40.0);
        status.getChildren().addAll(live, bombs, range, key);

        hBox.getChildren().addAll(level, status);
        hBox.getStyleClass().add("statusBar");
        hBox.relocate(0, sceneHeight);
        hBox.setPrefSize(sceneWidth, height);
        root.getChildren().add(hBox);
    }

    /**
     * Updates the game level digit image everytime the game level is changed
     * @param n game level
     */
    private void updateLevel(int n) {
        if (n != gameLevel) {
            gameLevel = n;
            level.getChildren().clear();
            level.getChildren().add(new ImageView(ImageFactory.getInstance().getDigit(n)));
        }
    }

    private HBox statusGroup(Image kind, Text number) {
        HBox group = new HBox();
        ImageView img = new ImageView(kind);
        group.setSpacing(4);
        number.setEffect(ds);
        number.setCache(true);
        number.setFill(Color.BLACK);
        number.getStyleClass().add("number");
        group.getChildren().addAll(img, number);
        return group;
    }

    /**
     * Updates the status bar every frame.
     *
     * @param game the game
     */
    public void update(Game game) {
        updateLevel(game.getWorldManager().getWorldNumber());
        Player player = game.getPlayer();
        liveValue.setText(String.valueOf(player.getLives()));
        rangeValue.setText(String.valueOf(player.getBombRange()));
        bombsValue.setText(String.valueOf(player.getBombCapacity()));
        keyValue.setText(String.valueOf(player.getNumberOfKeys()));
    }


}
