package lylesmal.openworldgame.domain;

import com.almasb.fxgl.entity.Entity;
import javafx.geometry.Point2D;
import lylesmal.openworldgame.domain.classification.DeadAcresDirection;
import lylesmal.openworldgame.domain.classification.DeadAcresEntityType;

import java.util.*;

public class DeadAcresCharacterEntity {
    private static final BaseGameEntityFactory gameEntityFactory = new BaseGameEntityFactory();
    private static final Random random = new Random();
    public static final String ENTITY_ID_PROPERTY = "entityId";
    
    //region #character attributes
    private final String entityId;
    private final Entity character;
    private DeadAcresDirection orientation;
    private final double responseRadius; //This is the radius at which the character will react to the player, either by chasing or fleeing depending on the character type
    private double healthPoints;
    private Point2D lastRecordedPosition;
    private DeadAcresDirection wanderDirection = DeadAcresDirection.SOUTH;
    private int wanderTicksRemaining = 0;
    public double playerSpeed;
    //endregion
    //region #character factory
    public static DeadAcresCharacterEntity PlayerOne() {
            return new DeadAcresCharacterEntity(
                gameEntityFactory.createControllablePlayer(),
                DeadAcresDirection.SOUTH,
                0);}

    public static DeadAcresCharacterEntity ZoneOneZombie() {
        return new DeadAcresCharacterEntity(
                gameEntityFactory.createZombieCharacter(random.nextDouble(64, 200), random.nextDouble(350, 525)),
                DeadAcresDirection.SOUTH,
                random.nextDouble(100, 175));
    }

    public static DeadAcresCharacterEntity ZoneTwoZombie() {
        return new DeadAcresCharacterEntity(
                gameEntityFactory.createZombieCharacter(random.nextDouble(600, 728), random.nextDouble(225, 450)),
                DeadAcresDirection.SOUTH,
                random.nextDouble(10, 100));
    }
    //endregion

    private DeadAcresCharacterEntity(Entity character, DeadAcresDirection orientation, double responseRadius) {
        entityId = UUID.randomUUID().toString();
        this.character = character;
        this.orientation = orientation;
        this.responseRadius = responseRadius;
        this.lastRecordedPosition = character.getPosition();
        this.character.setProperty(ENTITY_ID_PROPERTY, entityId);

        if(character.isType(DeadAcresEntityType.ZOMBIE)){
            healthPoints = 25;
            playerSpeed = random.nextDouble(0.25, 2.0);
        }
        if(character.isType(DeadAcresEntityType.CONTROLLABLE_PLAYER)){
            healthPoints = 100;
            playerSpeed = 1.5;
        }

    }
    
    //region #setters
    public void setOrientation(DeadAcresDirection orientation) {
        this.orientation = orientation;
    }

    public void setLastRecordedPosition() {
        this.lastRecordedPosition = character.getPosition();
    }

    public void setWanderDirection(DeadAcresDirection direction, int ticks) {
        this.wanderDirection = direction;
        this.wanderTicksRemaining = ticks;
    }
    //endregion
    //region #getters
    public String getEntityId() {
        return entityId;
    }

    public double getResponseRadius() {
        return responseRadius;
    }

    public Point2D getLastRecordedPosition() {
        return lastRecordedPosition;
    }

    public Entity getCharacterEntity() {
        return character;
    }

    public DeadAcresDirection getOrientation() {
        return orientation;
    }

    public double getHealthPoints() {
        return healthPoints;
    }

    public DeadAcresDirection getWanderDirection() {
        return wanderDirection;
    }
    //endregion
    //region #helpers
    public void takeDamage(double amount) {
        healthPoints -= amount;
    }

    public boolean isDead() {
        return healthPoints <= 0;
    }

    public boolean shouldRerollWander() {
        return wanderTicksRemaining <= 0;
    }

    public void tickWander() {
        if (wanderTicksRemaining > 0) {
            wanderTicksRemaining--;
        }
    }
    //endregion
}
