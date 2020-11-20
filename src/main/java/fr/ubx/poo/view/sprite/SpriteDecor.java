package fr.ubx.poo.view.sprite;

import fr.ubx.poo.game.Game;
import fr.ubx.poo.game.Position;
import fr.ubx.poo.game.World;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;


public class SpriteDecor extends Sprite {
    private Position position;

    public SpriteDecor(Pane layer, Image image, Position position) {
        super(layer, image);
        this.position = position;
    }


    @Override
    public void updateImage() {

    }

    public boolean updateImage(Game game) {
        World w = game.getWorld();
        if (w.get(position) == null) {
            remove();
            return true;
        }
        return false;
    }



    @Override
    public Position getPosition() {
        return position;
    }
}
