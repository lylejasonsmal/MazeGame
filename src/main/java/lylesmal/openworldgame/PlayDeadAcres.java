package lylesmal.openworldgame;

//region #imports
import com.almasb.fxgl.app.*;
import com.almasb.fxgl.entity.*;
import com.almasb.fxgl.input.*;
import com.almasb.fxgl.physics.*;
import com.almasb.fxgl.texture.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.util.*;
import lylesmal.openworldgame.domain.*;
import lylesmal.openworldgame.domain.classification.KeyboardKeys;
import lylesmal.openworldgame.domain.classification.DeadAcresDirection;
import lylesmal.openworldgame.domain.classification.DeadAcresEntityType;
import lylesmal.openworldgame.domain.classification.DeadAcresVariable;
import lylesmal.openworldgame.util.*;
import java.util.*;
import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.image;
//endregion

public class PlayDeadAcres extends GameApplication {
    private DeadAcresCharacterEntity playerOne;
    private final List<DeadAcresCharacterEntity> zombies = new ArrayList<>();
    private Entity cannon;
    private final BaseGameEntityFactory gameEntity;
    private final int yValue;
    private static final double KEY_SIZE = 28;
    private static final double IDLE_KEY_OPACITY = 0.55;
    private final Random random = new Random();
    Font font = new Font(Helper.fontName, 14);
    Font font_2 = new Font(Helper.fontName, 30);

    //region #on-screen ui elements
    private final Text RoundIndicatorValue;
    private final Text playerHealthPointsValue;
    private final Text killScoreValue;
    private final Text lblPlayerHealthPoints;
    private final Text lblKillScore;
    private final Text stepCounterValue;
    private final Text lblStepCounter;
    private final Text txtXPos;
    private final Text txtYPos;
    private final Map<KeyboardKeys, ImageView> keyViews = new EnumMap<>(KeyboardKeys.class);
    //endregion

    public PlayDeadAcres() {
        gameEntity = new BaseGameEntityFactory();
        yValue = 630;
        RoundIndicatorValue = new Text();
        playerHealthPointsValue = new Text();
        lblPlayerHealthPoints = new Text();
        killScoreValue = new Text();
        lblKillScore = new Text();
        stepCounterValue = new Text();
        lblStepCounter = new Text();
        txtXPos = new Text();
        txtYPos = new Text();
    }

    public static void main(String[] args) {
        launch(args);
    }

    //This method damages the zombie that was hit by a bullet and removes it once its health is depleted
    private void HandleZombieHit(Entity zombieEntity) {
        String entityId = zombieEntity.getString(DeadAcresCharacterEntity.ENTITY_ID_PROPERTY);
        zombies.stream()
                .filter(zombie -> zombie.getEntityId().equals(entityId))
                .findFirst()
                .ifPresent(zombie -> {
                    zombie.takeDamage(5);
                    play("damage.wav");
                    SpawnBloodSplatter(zombie.getCharacterEntity().getX(),zombie.getCharacterEntity().getY());
                    if (zombie.isDead()) {
                        double number = random.nextInt();

                        if(number % 3 == 0){
                        SpawnLootBox(zombie.getCharacterEntity().getX(), zombie.getCharacterEntity().getY());
                        }
                        zombie.getCharacterEntity().removeFromWorld();
                        zombies.remove(zombie);
                        play("death.wav");
                        inc(DeadAcresVariable.KillScore, +10);
                    }
                });
    }

    //This method ensures the character can explore the entire map while remaining in the viewport
    private void FollowMainCharacter() {
        double distanceX = getAppWidth() / 2.0 - playerOne.getCharacterEntity().getWidth() / 2.0;
        double distanceY = getAppHeight() / 2.0 - playerOne.getCharacterEntity().getHeight() / 2.0;

        getGameScene().getViewport().bindToEntity(playerOne.getCharacterEntity(), distanceX, distanceY);
        getGameScene().getViewport().setBounds(0, 0, 800, 640);
        getGameScene().getViewport().setLazy(true);
    }

    private void HandlePlayerInRestrictedArea(DeadAcresCharacterEntity character) {
        boolean inRestrictedArea = getGameWorld().getEntitiesByType(DeadAcresEntityType.WALL, DeadAcresEntityType.BRICK)
                .stream()
                .anyMatch(land ->
                        character.getCharacterEntity().getBoundingBoxComponent()
                                .isCollidingWith(land.getBoundingBoxComponent())
                );

        if (inRestrictedArea) {
            character.getCharacterEntity().setPosition(character.getLastRecordedPosition());
        } else {
            character.setLastRecordedPosition();
        }
    }

    private void MovePlayer(DeadAcresDirection requestedDirection) {
        HandlePlayerInRestrictedArea(playerOne);
        UpdatePlayerOneCoordinatesDisplay(requestedDirection);
        switch (requestedDirection) {
            case NORTH:
                playerOne.getCharacterEntity().translateY(-playerOne.playerSpeed);
                HandleCharacterMovementAnimations("character-up.png");
                break;
            case SOUTH:
                playerOne.getCharacterEntity().translateY(playerOne.playerSpeed);
                HandleCharacterMovementAnimations("character-down.png");
                break;
            case WEST:
                playerOne.getCharacterEntity().translateX(-playerOne.playerSpeed);
                HandleCharacterMovementAnimations("character-left.png");
                break;
            case EAST:
                playerOne.getCharacterEntity().translateX(playerOne.playerSpeed);
                HandleCharacterMovementAnimations("character-right.png");
                break;
        }
    }

    //region #initialization
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(800);
        gameSettings.setHeight(640);
        gameSettings.setTitle(DeadAcresVariable.GameTitle);
        gameSettings.setVersion(DeadAcresVariable.GameVersion);
        gameSettings.setAppIcon("island-wall.png");
        gameSettings.setGameMenuEnabled(true);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put(DeadAcresVariable.HealthPoints, 100);
        vars.put(DeadAcresVariable.KillScore, 0);
        vars.put(DeadAcresVariable.StepCounter, 0);
        vars.put(DeadAcresVariable.Round, 1);
    }

    @Override
    protected void initGame() {
        play("game-music.mp3");
        InitializeMap();

        SpawnMainCharacter();
        int round = getWorldProperties().getInt(DeadAcresVariable.Round);
        getGameTimer().runAtInterval(() -> {
            if(round > 10) return;
            SpawnZombies(round);
            inc(DeadAcresVariable.Round, +1);;
        }, Duration.seconds(20));
        ZombiesFollowAndAttackAlgorithm();
    }

    @Override
    protected void initUI() {
        RoundIndicatorValue.setFont(font_2);
        RoundIndicatorValue.setX(380);
        RoundIndicatorValue.setY(30);
        RoundIndicatorValue.setText("Round " + getWorldProperties().getInt(DeadAcresVariable.Round));

        lblPlayerHealthPoints.setText("HP:");
        lblPlayerHealthPoints.setFont(font);
        lblPlayerHealthPoints.setX(10);
        lblPlayerHealthPoints.setY(100);

        playerHealthPointsValue.setFont(font);
        playerHealthPointsValue.setTranslateX(40);
        playerHealthPointsValue.setTranslateY(100);

        lblKillScore.setText("Score:");
        lblKillScore.setTranslateX(10);
        lblKillScore.setTranslateY(120);
        lblKillScore.setFont(font);

        killScoreValue.setFont(font);
        killScoreValue.setTranslateX(70);
        killScoreValue.setTranslateY(120);

        lblStepCounter.setText("Steps:");
        lblStepCounter.setTranslateX(10);
        lblStepCounter.setTranslateY(140);
        lblStepCounter.setFont(font);

        stepCounterValue.setFont(font);
        stepCounterValue.setTranslateX(70);
        stepCounterValue.setTranslateY(140);

        UpdatePlayerOneCoordinatesDisplay(playerOne.getOrientation());
        txtXPos.setFont(font);
        txtXPos.setTranslateX(10);
        txtXPos.setTranslateY(10);

        txtYPos.setFont(font);
        txtYPos.setTranslateX(100);
        txtYPos.setTranslateY(10);


        playerHealthPointsValue.textProperty().bind(getWorldProperties().intProperty(DeadAcresVariable.HealthPoints).asString());
        killScoreValue.textProperty().bind(getWorldProperties().intProperty(DeadAcresVariable.KillScore).asString());
        stepCounterValue.textProperty().bind(getWorldProperties().intProperty(DeadAcresVariable.StepCounter).asString());

        getGameScene().addUINodes(lblPlayerHealthPoints, playerHealthPointsValue);
        getGameScene().addUINodes(lblKillScore, killScoreValue);
        getGameScene().addUINodes(lblStepCounter, stepCounterValue);
        getGameScene().addUINode(RoundIndicatorValue);
        getGameScene().addUINode(txtXPos);
        getGameScene().addUINode(txtYPos);

        Button menuButton1 = getUIFactoryService().newButton("Inventory");
        Button menuButton2 = getUIFactoryService().newButton("Settings");

        menuButton1.setTranslateX(10);
        menuButton1.setTranslateY(10);

        menuButton2.setTranslateX(10);
        menuButton2.setTranslateY(50);

        menuButton1.setDisable(true);
        menuButton2.setDisable(true);

        //getGameScene().addUINodes(menuButton1, menuButton2);

        //Show the movement keys laid out like a real keyboard, on the bottom bar next to the HP/score
        ShowKeyboardKeys(640, yValue - 50);

        getGameScene().getViewport().setZoom(1.5);
    }

    private void InitializeMap() {
        entityBuilder()
                .at(0, 0)
                .view(texture("map.png", 800, 640)) // adjust size to match your large map
                .buildAndAttach();

        SpawnLootBox();
        spawnStationaryObject(32, 32, "left-border.png");
        spawnStationaryObject(736, 32, "right-border.png");
        spawnStationaryObject(64, 32, "bottom-top-border.png");
        spawnStationaryObject(64, 576, "bottom-top-border.png");
        spawnStationaryObject(112, 240, "wall.png");
        spawnStationaryObject(304, 288, "wall.png");
        spawnStationaryObject(256, 448, "wall-2.png");
        spawnStationaryObject(240, 352, "wall.png");
        spawnStationaryObject(372, 256, "wall.png");
        spawnStationaryObject(372, 352, "wall.png");
        spawnStationaryObject(112, 112, "wall-1.png");
        spawnStationaryObject(144, 336, "wall-2.png");
        spawnStationaryObject(624, 480, "wall-2.png");
        spawnStationaryObject(176, 160, "island-wall.png");
        spawnStationaryObject(320, 160, "wall-2.png");
        SpawnAnimatedObject(224, 192, "water-fountain.png", 3,48, 48);
        SpawnAnimatedObject(672, 64, "pond.png", 2,64, 64);

        SpawnAnimatedObject(0, 0, "waves.png", 5,160, 32);
        SpawnAnimatedObject(160, 0, "waves.png", 4,160, 32);
        SpawnAnimatedObject(320, 0, "waves.png", 4,160, 32);
        SpawnAnimatedObject(480, 0, "waves.png", 3,160, 32);
        SpawnAnimatedObject(640, 0, "waves.png", 3,160, 32);

        SpawnAnimatedObject(0, 608, "waves.png", 5,160, 32);
        SpawnAnimatedObject(160, 608, "waves.png", 4,160, 32);
        SpawnAnimatedObject(320, 608, "waves.png", 4,160, 32);
        SpawnAnimatedObject(480, 608, "waves.png", 3,160, 32);
        SpawnAnimatedObject(640, 608, "waves.png", 3,160, 32);

        spawnStationaryObject(0,32, "no-waves.png");
        spawnStationaryObject(768,32, "no-waves.png");
    }
    //endregion
    //region #controls (keyboard/x-box controller binding)
    @Override
    protected void initInput() {
        //Player navigation
        getInput().addAction(new UserAction("player-move-up") {
            @Override
            protected void onActionBegin() {
                HighlightKey(KeyboardKeys.UP, true);
            }

            @Override
            protected void onAction() {
                MovePlayer(DeadAcresDirection.NORTH);
            }

            @Override
            protected void onActionEnd() {
                HighlightKey(KeyboardKeys.UP, false);
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("player-move-down") {
            @Override
            protected void onActionBegin() {
                HighlightKey(KeyboardKeys.DOWN, true);
            }

            @Override
            protected void onAction() {
                MovePlayer(DeadAcresDirection.SOUTH);
            }

            @Override
            protected void onActionEnd() {
                HighlightKey(KeyboardKeys.DOWN, false);
            }
        }, KeyCode.DOWN);

        getInput().addAction(new UserAction("player-move-left") {
            @Override
            protected void onActionBegin() {
                HighlightKey(KeyboardKeys.LEFT, true);
            }

            @Override
            protected void onAction() {
                MovePlayer(DeadAcresDirection.WEST);
            }

            @Override
            protected void onActionEnd() {
                HighlightKey(KeyboardKeys.LEFT, false);
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("player-move-right") {
            @Override
            protected void onActionBegin() {
                HighlightKey(KeyboardKeys.RIGHT, true);
            }

            @Override
            protected void onAction() {
                MovePlayer(DeadAcresDirection.EAST);
            }

            @Override
            protected void onActionEnd() {
                HighlightKey(KeyboardKeys.RIGHT, false);
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("player-jump") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(playerOne);
                playerOne.getCharacterEntity().translateY(-10);
                getGameTimer().runOnceAfter(() -> playerOne.getCharacterEntity().translateY(10), Duration.seconds(0.2));
            }
        }, KeyCode.SPACE);

        //Player actions
        getInput().addAction(new UserAction("player-spawns-brick") {
            @Override
            protected void onActionBegin() {
                PlaceBrick();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("player-spawns-flag") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(playerOne);
                SpawnAnimatedObject(Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX(), Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY(), "flag.png", 4,32, 32);
            }
        }, KeyCode.Z);

        getInput().addAction(new UserAction("player-spawns-bullet") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(playerOne);
                ShootBullet();
            }
        }, KeyCode.F);

        getInput().addAction(new UserAction("player-spawns-fence-vertical") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(playerOne);
                spawnStationaryObject(Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX(), Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY(), "fence-vertical.png");
            }
        }, KeyCode.V);

        getInput().addAction(new UserAction("player-spawns-fence-horizontal") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(playerOne);
                spawnStationaryObject(Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX(), Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY(), "fence-horizontal.png");
            }
        }, KeyCode.H);

        getInput().addAction(new UserAction("player-plants") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(playerOne);
                String[] plants = {"shrubs.png", "strawberries.png", "granadilla.png"};

                SpawnPlants(Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX(), Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY(), plants[random(0, 2)]);
            }
        }, KeyCode.P);

        getInput().addAction(new UserAction("player-spawns-cannon") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(playerOne);

                SpawnCannon(Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX(), Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY());
            }
        }, KeyCode.C);

        getInput().addAction(new UserAction("player-bombs") {
            @Override
            protected void onActionBegin() {
                play("bomb.wav");
                play("zombie_groan.wav");
                Iterator<DeadAcresCharacterEntity> listOfCurrentZombies = zombies.iterator();
                while (listOfCurrentZombies.hasNext()) {
                    DeadAcresCharacterEntity zombie = listOfCurrentZombies.next();
                    Entity zombieEntity = zombie.getCharacterEntity();
                    double distanceX = playerOne.getCharacterEntity().getX() - zombieEntity.getX();
                    double distanceY = playerOne.getCharacterEntity().getY() - zombieEntity.getY();
                    double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                    if (distance < 50){
                        SpawnBloodSplatter(zombieEntity.getX(),zombieEntity.getY());
                        SpawnBombSmoke(zombieEntity.getX(),zombieEntity.getY());
                        inc(DeadAcresVariable.KillScore, +10);
                        zombieEntity.removeFromWorld();
                        listOfCurrentZombies.remove();
                    }
                }

                //Blow up any bricks within 50 units of the player
                for (Entity brickEntity : getGameWorld().getEntitiesByType(DeadAcresEntityType.BRICK)) {
                    double distanceX = playerOne.getCharacterEntity().getX() - brickEntity.getX();
                    double distanceY = playerOne.getCharacterEntity().getY() - brickEntity.getY();
                    double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
                    if (distance < 50) {
                        brickEntity.removeFromWorld();
                    }
                }
            }
        }, KeyCode.B);
    }
    //endregion
    //region #physics (collision handling)
    @Override
    protected void initPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        physicsWorld.addCollisionHandler(new CollisionHandler(DeadAcresEntityType.CONTROLLABLE_PLAYER, DeadAcresEntityType.LOOT) {
            @Override
            protected void onCollisionBegin(Entity player, Entity loot) {
                play("loot-pick-up.wav");
                loot.removeFromWorld();
                inc(DeadAcresVariable.KillScore, +5);
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(DeadAcresEntityType.ZOMBIE, DeadAcresEntityType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity zombie, Entity bullet) {
                bullet.removeFromWorld();
                HandleZombieHit(zombie);
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(DeadAcresEntityType.WALL, DeadAcresEntityType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity wall, Entity bullet) {
                bullet.removeFromWorld();
                play("brick.wav");
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(DeadAcresEntityType.ZOMBIE, DeadAcresEntityType.CONTROLLABLE_PLAYER) {
            @Override
            protected void onCollisionBegin(Entity zombie, Entity player) {
                int hp = getWorldProperties().intProperty(DeadAcresVariable.HealthPoints).get();

                if (hp > 0) {
                    if (hp % 5 == 0) {
                        play("zombie_bite.wav");
                        SpawnBloodSplatter(player.getX(), player.getY());
                    }
                    inc(DeadAcresVariable.HealthPoints, -1);
                }
                else{
                    play("death.wav");
                    getNotificationService().pushNotification("Game Over!");
                    getGameController().startNewGame();
                }
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(DeadAcresEntityType.ZOMBIE, DeadAcresEntityType.BRICK) {
            @Override
            protected void onCollisionBegin(Entity zombie, Entity brick) {
                play("brick.wav");
                play("zombie_groan.wav");
                play("zombie_bite.wav");
                brick.removeFromWorld();
                HandleZombieHit(zombie);
            }
        });
    }
    //endregion
    //region #NPC algorithms
    //TODO: Make a helper method that can be used for any entity to move towards another entity
    private void ZombiesFollowAndAttackAlgorithm() {
        getGameTimer().runAtInterval(() -> {
            for (DeadAcresCharacterEntity zombie : zombies) {
                double distanceX = playerOne.getCharacterEntity().getX() - zombie.getCharacterEntity().getX();
                double distanceY = playerOne.getCharacterEntity().getY() - zombie.getCharacterEntity().getY();
                double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                if (distance <= zombie.getResponseRadius()) {
                    double moveX = (distanceX / distance);
                    double moveY = (distanceY / distance);

                    zombie.getCharacterEntity().translateX(moveX);
                    zombie.getCharacterEntity().translateY(moveY);
                    HandlePlayerInRestrictedArea(zombie);
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            HandleZombieMovementAnimations(zombie, "character-right.png");
                        } else {
                            HandleZombieMovementAnimations(zombie, "character-left.png");
                        }
                    } else {
                        if (distanceY > 0) {
                            HandleZombieMovementAnimations(zombie, "character-down.png");
                        } else {
                            HandleZombieMovementAnimations(zombie, "character-up.png");
                        }
                    }
                }
                else{
                    //Outside the response radius the zombie wanders, holding one direction for ~2s before rerolling
                    zombie.tickWander();
                    if (zombie.shouldRerollWander()) {
                        DeadAcresDirection direction = DeadAcresDirection.values()[random.nextInt(4)];
                        zombie.setWanderDirection(direction, random.nextInt(20, 30));
                    }
                    WanderIdleZombieAlgorithm(zombie);
                }
            }
        }, Duration.seconds(0.1));
    }

    private void WanderIdleZombieAlgorithm(DeadAcresCharacterEntity zombie) {
        double speed = zombie.playerSpeed;
        switch (zombie.getWanderDirection()) {
            case NORTH:
                zombie.getCharacterEntity().translateY(-speed);
                HandleZombieMovementAnimations(zombie, "character-up.png");
                break;
            case SOUTH:
                zombie.getCharacterEntity().translateY(speed);
                HandleZombieMovementAnimations(zombie, "character-down.png");
                break;
            case WEST:
                zombie.getCharacterEntity().translateX(-speed);
                HandleZombieMovementAnimations(zombie, "character-left.png");
                break;
            case EAST:
                zombie.getCharacterEntity().translateX(speed);
                HandleZombieMovementAnimations(zombie, "character-right.png");
                break;
        }
        HandlePlayerInRestrictedArea(zombie);
    }

    //endregion
    //region #character animations
    private int nextFrameIndex = 0;
    private void PlayCharacterAnimation(DeadAcresCharacterEntity character, AnimationChannel channel){
        AnimatedTexture texture = new AnimatedTexture(channel);
        texture.playFrom(nextFrameIndex);

        character.getCharacterEntity().getViewComponent().clearChildren();
        character.getCharacterEntity().getViewComponent().addChild(texture);

        if(nextFrameIndex <= 4){
            nextFrameIndex++;
        }
        else{
            nextFrameIndex = 0;
        }
    }

    private void HandleCharacterMovementAnimations(String fileName){
        Image image = image(fileName);
        AnimationChannel channel = new AnimationChannel(image, 4, 16, 32, Duration.seconds(playerOne.playerSpeed), 0, 3);
        PlayCharacterAnimation(playerOne, channel);
    }

    private void HandleZombieMovementAnimations(DeadAcresCharacterEntity zombie, String fileName){
        Image image = image(fileName);
        AnimationChannel channel = new AnimationChannel(image, 4, 16, 32, Duration.seconds(zombie.playerSpeed), 0, 3);
        PlayCharacterAnimation(zombie, channel);
    }
    //endregion
    //region #character-specific actions (e.g. shooting, placing bricks)
    private void PlaceBrick(){
        int space = 16;
        switch (playerOne.getOrientation()) {
            case EAST:
                SpawnBrick((Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX() + space), Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY());
                break;
            case WEST:
                SpawnBrick((Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX() - space), Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY());
                break;
            case NORTH:
                SpawnBrick(Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX(), (Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY() - space));
                break;
            case SOUTH:
                SpawnBrick(Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getX(), (Helper.fixEntityToGrid(playerOne.getCharacterEntity()).getY() + space));
                break;
        }
    }

    private void ShootBullet(){
        play("gunshot.wav");
        switch (playerOne.getOrientation()) {
            case EAST:
                gameEntity.createBullet(playerOne.getCharacterEntity().getX() - 5, playerOne.getCharacterEntity().getY(), 300, +5, 0);
                break;
            case WEST:
                gameEntity.createBullet(playerOne.getCharacterEntity().getX(), playerOne.getCharacterEntity().getY(), 300, -5, 0);
                break;
            case NORTH:
                gameEntity.createBullet(playerOne.getCharacterEntity().getX(), playerOne.getCharacterEntity().getY(), 300, 0, -5);
                break;
            case SOUTH:
                gameEntity.createBullet(playerOne.getCharacterEntity().getX(), playerOne.getCharacterEntity().getY(), 300, 0, +5);
                break;
        }
    }
    //endregion
    //region #spawn factory
    private void SpawnMainCharacter() {
        playerOne = DeadAcresCharacterEntity.PlayerOne();
        FollowMainCharacter();
    }

    private void SpawnZombies(int round) {
        Random random = new Random();
        int zombieSpawnCount = random.nextInt(round * 4, round * 10);
        while (zombieSpawnCount > 0) {
            if(zombieSpawnCount % 2 == 0) {
                zombies.add(DeadAcresCharacterEntity.ZoneTwoZombie());
            }
            else{
                zombies.add(DeadAcresCharacterEntity.ZoneOneZombie());
            }
            zombieSpawnCount--;
        }
    }

    private void SpawnLootBox() {
        getGameTimer().runAtInterval(() -> {
            Random random = new Random();
            double x = random.nextDouble(560, 720);
            double y = random.nextDouble(55, 195);
            gameEntity.createLoot(x, y);
        }, Duration.seconds(random(5, 60)));
    }

    private void SpawnLootBox(double x, double y) {
        gameEntity.createLoot(x, y);
    }

    private void spawnStationaryObject(double x, double y, String fileName) {
        gameEntity.createLand(x, y, fileName);
    }

    private void SpawnPlants(double x, double y, String fileName) {
        gameEntity.createPlants(x, y, fileName);
    }

    private void SpawnBloodSplatter(double x, double y) {
        gameEntity.createBlood(x, y).setOpacity(0.5);
    }

    private void SpawnBombSmoke(double x, double y) {
        gameEntity.createBombSmoke(x, y).setOpacity(0.75);
    }

    private void SpawnBrick(double x, double y) {
        if ((x > 64 && x < 728) && (y > 54 && y < 547)) {
            play("brick.wav");
            gameEntity.createBrick(x, y, "brick.png");
        }
    }

    private void SpawnCannon(double x, double y) {
        if(cannon==null){
            cannon = gameEntity.createCannon(x, y);
            getGameTimer().runAtInterval(this::spawnBullet, Duration.seconds(2));
        }
    }

    private void spawnBullet(){
        play("gunshot.wav");
        gameEntity.createBullet(cannon.getRightX()-5, cannon.getY(), 300,+5,0);
    }

    private void SpawnAnimatedObject(double x, double y, String fileName, int frames, int frameWidth, int frameHeight) {
        gameEntity.createStationaryAnimatedObject(x, y, fileName, frames, frameWidth, frameHeight);
    }
    //endregion
    //region #updating in-game variables
    private void UpdatePlayerOneCoordinatesDisplay(DeadAcresDirection currentOrientation) {
        if ((playerOne.getCharacterEntity().getX() % 8 == 0) || (playerOne.getCharacterEntity().getY() % 8 == 0)) {
            inc(DeadAcresVariable.StepCounter, +1);
        }
        txtXPos.setText("X: " + (int) playerOne.getCharacterEntity().getX());
        txtYPos.setText("Y: " + (int) playerOne.getCharacterEntity().getY());
        playerOne.setOrientation(currentOrientation);
    }
    //endregion
    //region #control screen display
    private void ShowKeyboardKeys(double x, double y) {
        double step = KEY_SIZE + 4;
        ShowKeyboardKey(x + step, y, KeyboardKeys.UP);          //top row, centred
        ShowKeyboardKey(x, y + step, KeyboardKeys.LEFT);        //bottom row
        ShowKeyboardKey(x + step, y + step, KeyboardKeys.DOWN);
        ShowKeyboardKey(x + step * 2, y + step, KeyboardKeys.RIGHT);
    }

    //Reusable: glows + brightens a key while it is pressed, restores the dim idle look when released
    private void HighlightKey(KeyboardKeys key, boolean pressed) {
        ImageView keyView = keyViews.get(key);
        if (keyView == null) {
            return;
        }
        keyView.setOpacity(pressed ? 1.0 : IDLE_KEY_OPACITY);
        keyView.setEffect(pressed ? new Glow(0.8) : null);
    }

    private Image keyImage(KeyboardKeys key) {
        String fileName = switch (key) {
            case UP -> "ARROWUP.png";
            case DOWN -> "ARROWDOWN.png";
            case LEFT -> "ARROWLEFT.png";
            case RIGHT -> "ARROWRIGHT.png";
        };
        return image(fileName);
    }

    private void ShowKeyboardKey(double x, double y, KeyboardKeys key) {
        ImageView keyView = new ImageView(keyImage(key));
        keyView.setFitWidth(KEY_SIZE);
        keyView.setFitHeight(KEY_SIZE);
        keyView.setTranslateX(x);
        keyView.setTranslateY(y);
        keyView.setOpacity(IDLE_KEY_OPACITY);
        keyViews.put(key, keyView);
        getGameScene().addUINode(keyView);
    }
    //endregion
}
