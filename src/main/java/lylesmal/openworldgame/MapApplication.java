package lylesmal.openworldgame;

import com.almasb.fxgl.app.*;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lylesmal.openworldgame.domain.*;
import lylesmal.openworldgame.util.Helper;

import java.util.*;
import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.image;

public class MapApplication extends GameApplication {
    private MazeGameCharacterEntity controllablePlayer;
    private final List<MazeGameCharacterEntity> zombies = new ArrayList<>();
    private Entity wall;
    private Entity cannon;
    private Entity brick;
    private Entity animatedObject;
    private GameEntityFactory gameEntity;
    private final int yValue;
    private final Text txtPlayerHP;
    private final Text lblPlayerHP;
    private final Text txtScore;
    private final Text lblScore;
    private final Text txtSteps;
    private final Text lblSteps;
    private final Text txtXPos;
    private final Text txtYPos;
    Font font = new Font(Helper.fontName, 14);

    public MapApplication() {
        gameEntity = new GameEntityFactory();
        yValue = 630;
        txtPlayerHP = new Text();
        lblPlayerHP = new Text();
        txtScore = new Text();
        lblScore = new Text();
        txtSteps = new Text();
        lblSteps = new Text();
        txtXPos = new Text();
        txtYPos = new Text();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(800);
        gameSettings.setHeight(640);
        gameSettings.setTitle("a-Mazed");
        gameSettings.setVersion("1.0");
        gameSettings.setAppIcon("island-wall.png");
        gameSettings.setGameMenuEnabled(false);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("hp", 100);
        vars.put("score", 0);
        vars.put("steps", 0);
    }

    @Override
    protected void initInput() {
        //Player navigation
        getInput().addAction(new UserAction("player-move-up") {
            @Override
            protected void onAction() {
                MovePlayer(GameEntityOrientation.NORTH);
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("player-move-down") {
            @Override
            protected void onAction() {
                MovePlayer(GameEntityOrientation.SOUTH);
            }
        }, KeyCode.DOWN);

        getInput().addAction(new UserAction("player-move-left") {
            @Override
            protected void onAction() {
                MovePlayer(GameEntityOrientation.WEST);
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("player-move-right") {
            @Override
            protected void onAction() {
                MovePlayer(GameEntityOrientation.EAST);
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("player-jump") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(controllablePlayer);
                controllablePlayer.getCharacterEntity().translateY(-10);
                getGameTimer().runOnceAfter(() -> controllablePlayer.getCharacterEntity().translateY(10), Duration.seconds(0.2));
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
                HandlePlayerInRestrictedArea(controllablePlayer);
                SpawnAnimatedObject(Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX(), Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY(), "flag.png", 4,32, 32);
            }
        }, KeyCode.Z);

        getInput().addAction(new UserAction("player-spawns-bullet") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(controllablePlayer);
                shootBullet();
            }
        }, KeyCode.F);

        getInput().addAction(new UserAction("player-spawns-fence-vertical") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(controllablePlayer);
                spawnStationaryObject(Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX(), Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY(), "fence-vertical.png");
            }
        }, KeyCode.V);

        getInput().addAction(new UserAction("player-spawns-fence-horizontal") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(controllablePlayer);
                spawnStationaryObject(Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX(), Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY(), "fence-horizontal.png");
            }
        }, KeyCode.H);

        getInput().addAction(new UserAction("player-plants") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(controllablePlayer);
                String[] plants = {"shrubs.png", "strawberries.png", "granadilla.png"};

                spawnPlants(Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX(), Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY(), plants[random(0, 2)]);
            }
        }, KeyCode.P);

        getInput().addAction(new UserAction("player-spawns-cannon") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(controllablePlayer);

                spawnCannon(Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX(), Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY());
            }
        }, KeyCode.C);

        getInput().addAction(new UserAction("player-bombs") {
            @Override
            protected void onActionBegin() {
                play("bomb.wav");
                play("zombie_groan.wav");
                Iterator<MazeGameCharacterEntity> listOfCurrentZombies = zombies.iterator();
                while (listOfCurrentZombies.hasNext()) {
                    MazeGameCharacterEntity zombie = listOfCurrentZombies.next();
                    Entity zombieEntity = zombie.getCharacterEntity();
                    double deltaX = controllablePlayer.getCharacterEntity().getX() - zombieEntity.getX();
                    double deltaY = controllablePlayer.getCharacterEntity().getY() - zombieEntity.getY();
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    if (distance < 150){
                        inc("score", +10);
                        zombieEntity.removeFromWorld();
                        listOfCurrentZombies.remove();
                    }
                }
            }
        }, KeyCode.B);
    }

    @Override
    protected void initGame() {
        play("game-music.mp3");
        generateMap();

        spawnMainCharacter();
        SpawnZombies();
        ZombiesFollowAndAttackAlgorithm();
    }

    @Override
    protected void initUI() {
        lblPlayerHP.setText("HP: ");
        lblPlayerHP.setFont(font);
        lblPlayerHP.setX(controllablePlayer.getCharacterEntity().getX());
        lblPlayerHP.setY(controllablePlayer.getCharacterEntity().getY());

        txtPlayerHP.setFont(font);
        txtPlayerHP.setTranslateX(60);
        txtPlayerHP.setTranslateY(yValue);

        lblScore.setText("Score: ");
        lblScore.setTranslateX(110);
        lblScore.setTranslateY(yValue);
        lblScore.setFont(font);

        txtScore.setFont(font);
        txtScore.setTranslateX(210);
        txtScore.setTranslateY(yValue);

        lblSteps.setText("Steps: ");
        lblSteps.setTranslateX(260);
        lblSteps.setTranslateY(yValue);
        lblSteps.setFont(font);

        txtSteps.setFont(font);
        txtSteps.setTranslateX(360);
        txtSteps.setTranslateY(yValue);

        UpdatePlayerPosition(controllablePlayer, controllablePlayer.getOrientation());
        txtXPos.setFont(font);
        txtXPos.setTranslateX(10);
        txtXPos.setTranslateY(10);

        txtYPos.setFont(font);
        txtYPos.setTranslateX(100);
        txtYPos.setTranslateY(10);


        txtPlayerHP.textProperty().bind(getWorldProperties().intProperty("hp").asString());
        txtScore.textProperty().bind(getWorldProperties().intProperty("score").asString());
        txtSteps.textProperty().bind(getWorldProperties().intProperty("steps").asString());

        getGameScene().addUINodes(lblPlayerHP, txtPlayerHP);
        getGameScene().addUINodes(lblScore, txtScore);
        getGameScene().addUINodes(lblSteps, txtSteps);
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
        getGameScene().getViewport().setZoom(1.5);
    }

    @Override
    protected void initPhysics() {
        PhysicsWorld physicsWorld = getPhysicsWorld();
        physicsWorld.addCollisionHandler(new CollisionHandler(GameEntityTypes.CONTROLLABLE_PLAYER, GameEntityTypes.LOOT) {
            @Override
            protected void onCollisionBegin(Entity player, Entity loot) {
                play("loot-pick-up.wav");
                loot.removeFromWorld();
                inc("score", +5);
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(GameEntityTypes.ZOMBIE, GameEntityTypes.BULLET) {
            @Override
            protected void onCollisionBegin(Entity zombie, Entity bullet) {
                bullet.removeFromWorld();
                HandleZombieHit(zombie);
            }
        });
    }

    //This method damages the zombie that was hit by a bullet and removes it once its health is depleted
    private void HandleZombieHit(Entity zombieEntity) {
        String entityId = zombieEntity.getString(MazeGameCharacterEntity.ENTITY_ID_PROPERTY);
        zombies.stream()
                .filter(zombie -> zombie.getEntityId().equals(entityId))
                .findFirst()
                .ifPresent(zombie -> {
                    zombie.takeDamage(5);
                    play("damage.wav");
                    if (zombie.isDead()) {
                        zombie.getCharacterEntity().removeFromWorld();
                        zombies.remove(zombie);
                        play("death.wav");
                        inc("score", +10);
                    }
                });
    }

    //This method generates a map
    private void generateMap() {
        entityBuilder()
                .at(0, 0)
                .view(texture("map.png", 800, 640)) // adjust size to match your large map
                .buildAndAttach();

        spawnLootBox();
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

    //This method ensures the character can explore the entire map while remaining in the viewport
    private void followMainCharacter() {
        double distanceX = getAppWidth() / 2.0 - controllablePlayer.getCharacterEntity().getWidth() / 2.0;
        double distanceY = getAppHeight() / 2.0 - controllablePlayer.getCharacterEntity().getHeight() / 2.0;

        getGameScene().getViewport().bindToEntity(controllablePlayer.getCharacterEntity(), distanceX, distanceY);
        getGameScene().getViewport().setBounds(0, 0, 800, 640);
        getGameScene().getViewport().setLazy(true);
    }

    //This method spawns your character
    private void spawnMainCharacter() {
        controllablePlayer = new MazeGameCharacterEntity(gameEntity.createControllablePlayer(), GameEntityOrientation.SOUTH, 0);
        followMainCharacter();
    }

    //This method spawns enemy characters
    private void SpawnZombies() {
        int zombieSpawnCount = 13;
        Random random = new Random();
        while (zombieSpawnCount > 0) {
            zombies.add(new MazeGameCharacterEntity(gameEntity.createZombieCharacter(random.nextDouble(64, 200), random.nextDouble(350, 500)), GameEntityOrientation.SOUTH, 150));
            zombieSpawnCount--;
        }
    }

    //TODO: Make this a helper method that can be used for any entity to move towards another entity
    private void ZombiesFollowAndAttackAlgorithm() {
        getGameTimer().runAtInterval(() -> {
            for (MazeGameCharacterEntity zombie : zombies) {
                double distanceX = controllablePlayer.getCharacterEntity().getX() - zombie.getCharacterEntity().getX();
                double distanceY = controllablePlayer.getCharacterEntity().getY() - zombie.getCharacterEntity().getY();
                double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                if (distance <= zombie.getResponseRadius()) {
                    double moveX = (distanceX / distance) * 2.0;
                    double moveY = (distanceY / distance) * 2.0;

                    zombie.getCharacterEntity().translateX(moveX);
                    zombie.getCharacterEntity().translateY(moveY);
                    HandlePlayerInRestrictedArea(zombie);
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            HandleEnemyMovementAnimations(zombie, "character-right.png");
                        } else {
                            HandleEnemyMovementAnimations(zombie, "character-left.png");
                        }
                    } else {
                        if (distanceY > 0) {
                            HandleEnemyMovementAnimations(zombie, "character-down.png");
                        } else {
                            HandleEnemyMovementAnimations(zombie, "character-up.png");
                        }
                    }
                }

                int hp = getWorldProperties().intProperty("hp").get();
                if (distance <= 10 & hp >= 0 ) {
                    if(hp % 5 == 0) {
                        play("zombie_bite.wav");
                    }
                    inc("hp", -1);
                    if (hp == 0){
                        controllablePlayer.getCharacterEntity().removeFromWorld();
                        play("death.wav");
                    }
                }
            }
        }, Duration.seconds(0.1));
    }

    private int nextFrameIndex = 0;
    //This method handles character animations
    public void handleCharacterMovementAnimations(String fileName){
        Image image = image(fileName);

        AnimationChannel channel = new AnimationChannel(image, 4, 16, 32, Duration.seconds(1.5), 0, 3);

        //Create the animated texture
        AnimatedTexture texture = new AnimatedTexture(channel);
        texture.playFrom(nextFrameIndex);

        controllablePlayer.getCharacterEntity().getViewComponent().clearChildren();
        controllablePlayer.getCharacterEntity().getViewComponent().addChild(texture);

        if(nextFrameIndex <= 4){
            nextFrameIndex++;
        }
        else{
            nextFrameIndex = 0;
        }
    }

    //This method handles character animations
    public void HandleEnemyMovementAnimations(MazeGameCharacterEntity zombie, String fileName){
        Image image = image(fileName);

        AnimationChannel channel = new AnimationChannel(image, 4, 16, 32, Duration.seconds(1.5), 0, 3);

        //Create the animated texture
        AnimatedTexture texture = new AnimatedTexture(channel);
        texture.playFrom(nextFrameIndex);

        zombie.getCharacterEntity().getViewComponent().clearChildren();
        zombie.getCharacterEntity().getViewComponent().addChild(texture);

        if(nextFrameIndex <= 4){
            nextFrameIndex++;
        }
        else{
            nextFrameIndex = 0;
        }
    }

    //This method spawns loot boxes every 5-60 seconds
    private void spawnLootBox() {
        getGameTimer().runAtInterval(() -> {
            Random random = new Random();
            double x = random.nextDouble(560, 720);
            double y = random.nextDouble(55, 195);
            gameEntity.createLoot(x, y);
        }, Duration.seconds(random(5, 60)));
    }

    //This method spawns land
    private void spawnStationaryObject(double x, double y, String fileName) {
        wall = gameEntity.createLand(x, y, fileName);
    }

    private void spawnPlants(double x, double y, String fileName) {
        gameEntity.createPlants(x, y, fileName);
    }

    //This method spawns a brick
    private void SpawnBrick(double x, double y) {
        if ((x > 64 && x < 728) && (y > 54 && y < 547)) {
        play("brick.wav");
        gameEntity.createBrick(x, y, "brick.png");
        }
    }

    //This method spawns a cannon
    private void spawnCannon(double x, double y) {
        if(cannon==null){
        cannon = gameEntity.createCannon(x, y);

         getGameTimer().runAtInterval(() ->
         spawnBullet(),
         Duration.seconds(2));
        }
    }

    //This method spawns a bullet
    private void spawnBullet(){
        play("explosion.wav");
         gameEntity.createBullet(cannon.getRightX()-5, cannon.getY(), 300,+5,0);
    }

    private void shootBullet(){
        play("explosion.wav");
        switch (controllablePlayer.getOrientation()) {
            case EAST:
                gameEntity.createBullet(controllablePlayer.getCharacterEntity().getX() - 5, controllablePlayer.getCharacterEntity().getY(), 300, +5, 0);
                break;
            case WEST:
                gameEntity.createBullet(controllablePlayer.getCharacterEntity().getX(), controllablePlayer.getCharacterEntity().getY(), 300, -5, 0);
                break;
            case NORTH:
                gameEntity.createBullet(controllablePlayer.getCharacterEntity().getX(), controllablePlayer.getCharacterEntity().getY(), 300, 0, -5);
                break;
            case SOUTH:
                gameEntity.createBullet(controllablePlayer.getCharacterEntity().getX(), controllablePlayer.getCharacterEntity().getY(), 300, 0, +5);
                break;
        }
    }

    //The method spawns an animated object
    private void SpawnAnimatedObject(double x, double y, String fileName, int frames, int frameWidth, int frameHeight) {
        animatedObject = gameEntity.createStationaryAnimatedObject(x, y, fileName, frames, frameWidth, frameHeight);
    }

    private void HandlePlayerInRestrictedArea(MazeGameCharacterEntity character) {
        boolean inRestrictedArea = getGameWorld().getEntitiesByType(GameEntityTypes.WALL,GameEntityTypes.BRICK)
                .stream()
                .anyMatch(land ->
                        character.getCharacterEntity().getBoundingBoxComponent()
                                .isCollidingWith(land.getBoundingBoxComponent())
                );

        if (inRestrictedArea) {
            character.getCharacterEntity().setPosition(character.getLastRecordedPosition());
        } else {
            character.setLastRecordPosition();
        }
    }

    private void MovePlayer(GameEntityOrientation requestedDirection) {
        HandlePlayerInRestrictedArea(controllablePlayer);
        UpdatePlayerPosition(controllablePlayer, requestedDirection);
        switch (requestedDirection) {
            case NORTH:
                controllablePlayer.getCharacterEntity().translateY(-controllablePlayer.playerSpeed);
                handleCharacterMovementAnimations("character-up.png");
                break;
            case SOUTH:
                controllablePlayer.getCharacterEntity().translateY(controllablePlayer.playerSpeed);
                handleCharacterMovementAnimations("character-down.png");
                break;
            case WEST:
                controllablePlayer.getCharacterEntity().translateX(-controllablePlayer.playerSpeed);
                handleCharacterMovementAnimations("character-left.png");
                break;
            case EAST:
                controllablePlayer.getCharacterEntity().translateX(controllablePlayer.playerSpeed);
                handleCharacterMovementAnimations("character-right.png");
                break;
        }
    }

    private void PlaceBrick(){
        int space = 16;
        switch (controllablePlayer.getOrientation()) {
            case EAST:
                SpawnBrick((Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX() + space), Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY());
                break;
            case WEST:
                SpawnBrick((Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX() - space), Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY());
                break;
            case NORTH:
                SpawnBrick(Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX(), (Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY() - space));
                break;
            case SOUTH:
                SpawnBrick(Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getX(), (Helper.fixEntityToGrid(controllablePlayer.getCharacterEntity()).getY() + space));
                break;
        }
    }

    //This updates the player coordinates that appear onscreen
    private void UpdatePlayerPosition(MazeGameCharacterEntity player, GameEntityOrientation currentOrientation) {
        if ((player.getCharacterEntity().getX() % 8 == 0) || (player.getCharacterEntity().getY() % 8 == 0)) {
            inc("steps", +1);
        }
        txtXPos.setText("X: " + (int) player.getCharacterEntity().getX());
        txtYPos.setText("Y: " + (int) player.getCharacterEntity().getY());
        player.setOrientation(currentOrientation);
    }

}
