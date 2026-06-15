package lylesmal.openworldgame.domain;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;

import java.util.UUID;

public class MazeGameCharacterEntity {
    public static final String ENTITY_ID_PROPERTY = "entityId";

    private String entityId;
    private Entity entity;
    private GameEntityOrientation orientation;
    private double responseRadius;
    private double healthPoints;
    private Point2D lastRecordedPosition;
    public final double playerSpeed = 1.5;

    public MazeGameCharacterEntity(Entity entity, GameEntityOrientation orientation, double responseRadius) {
        this.entityId = UUID.randomUUID().toString();
        this.entity = entity;
        this.orientation = orientation;
        this.responseRadius = responseRadius;
        this.lastRecordedPosition = entity.getPosition();
        this.entity.setProperty(ENTITY_ID_PROPERTY, entityId);

        if(entity.isType(GameEntityTypes.ZOMBIE)){
            healthPoints = 25;
        }
        if(entity.isType(GameEntityTypes.CONTROLLABLE_PLAYER)){
            healthPoints = 100;
        }

    }

    public String getEntityId() {
        return entityId;
    }

    public double getHealthPoints() {
        return healthPoints;
    }

    public void takeDamage(double amount) {
        healthPoints -= amount;
    }

    public boolean isDead() {
        return healthPoints <= 0;
    }

    public Entity getCharacterEntity() {
        return entity;
    }

    public GameEntityOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(GameEntityOrientation orientation) {
        this.orientation = orientation;
    }

    public double getResponseRadius() {
        return responseRadius;
    }

    public void setResponseRadius(double responseRadius) {
        this.responseRadius = responseRadius;
    }

    public Point2D getLastRecordedPosition() {
        return lastRecordedPosition;
    }

    public void setLastRecordPosition() {
        this.lastRecordedPosition = entity.getPosition();
    }
}
