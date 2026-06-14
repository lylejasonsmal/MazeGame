package lylesmal.openworldgame.domain;

import com.almasb.fxgl.entity.Entity;

public class MazeGameCharacterEntity {
    public Entity character;
    public GameEntityOrientation orientation;

    public MazeGameCharacterEntity(Entity character, GameEntityOrientation orientation) {
        this.character = character;
        this.orientation = orientation;
    }

    public Entity getCharacter() {
        return character;
    }

    public GameEntityOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(GameEntityOrientation orientation) {
        this.orientation = orientation;
    }
}
