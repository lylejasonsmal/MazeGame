package lylesmal.openworldgame.domain;

import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.play;
import static com.almasb.fxgl.dsl.FXGLForKtKt.image;

public class GameEntityFactory implements EntityFactory {

    @Spawns("character")
    public Entity createCharacter() {
        return new EntityBuilder()
                .type(GameEntityTypes.PLAYER)
                .at(75,60)
                .view("character.png")
                .bbox(new HitBox("CHARACTER", BoundingShape.box(16, 16)))
                .with(new CollidableComponent(true))
                .zIndex(10)
                .buildAndAttach();
    }

    @Spawns("loot")
    public Entity createLoot(double x, double y) {
        return new EntityBuilder()
                .type(GameEntityTypes.LOOT)
                .at(x, y)
                .viewWithBBox("loot.png")
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    @Spawns("cannon")
    public Entity createCannon(double x, double y) {

        return new EntityBuilder()
                .type(GameEntityTypes.CANNON)
                .at(x, y)
                .viewWithBBox("cannon.png")
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    @Spawns("bullet")
    public void createBullet(double x, double y, double bulletSpeed, double dirX, double dirY) {
        ProjectileComponent projectileComponent = new ProjectileComponent(new Point2D(dirX,dirY),bulletSpeed);

        new EntityBuilder()
                .type(GameEntityTypes.BULLET)
                .at(x, y)
                .viewWithBBox("blast.png")
                .with(new CollidableComponent(true))
                .with(projectileComponent)
                .buildAndAttach();
    }


    @Spawns("land")
    public Entity createLand(double x, double y, String fileName) {
        Image image = image(fileName); // FXGL method to load image
        double width = image.getWidth();
        double height = image.getHeight();

        return new EntityBuilder()
                .type(GameEntityTypes.WALL)
                .at(x, y)
                .view(fileName)
                .bbox(new HitBox(new Point2D(0, -11), BoundingShape.box(width, height)))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    @Spawns("plants")
    public Entity createPlants(double x, double y, String fileName) {
        Image image = image(fileName); // FXGL method to load image
        double width = image.getWidth();
        double height = image.getHeight();

        return new EntityBuilder()
                .type(GameEntityTypes.PLANTS)
                .at(x, y)
                .view(fileName)
                .bbox(new HitBox(new Point2D(0, -11), BoundingShape.box(width, height)))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    @Spawns("brick")
    public Entity createBrick(double x, double y, String fileName) {
        Image image = image(fileName); // FXGL method to load image
        double width = image.getWidth();
        double height = image.getHeight();

        return new EntityBuilder()
                .type(GameEntityTypes.BRICK)
                .at(x, y)
                .view(fileName)
                .bbox(new HitBox(new Point2D(0, -11), BoundingShape.box(width, height)))
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    @Spawns("animatedObject")
    public Entity createStationaryAnimatedObject(double x, double y, String fileName, int frames, int frameWidth, int frameHeight) {
        Image image = image(fileName); // FXGL method to load image

        AnimationChannel channel = new AnimationChannel(image, frames, frameWidth, frameHeight, Duration.seconds(0.8), 0, frames-1);

        // Create the animated texture
        AnimatedTexture texture = new AnimatedTexture(channel);
        texture.loop();

        return new EntityBuilder()
                .type(GameEntityTypes.ANIMATED_OBJECT)
                .at(x, y)
                .view(texture)
                .bbox(new HitBox(new Point2D(0, 0), BoundingShape.box(frameWidth, frameHeight)))
                .with(new CollidableComponent(true))
                .zIndex(2)
                .buildAndAttach();
    }
}

