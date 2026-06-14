package lylesmal.openworldgame;

import com.almasb.fxgl.app.*;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.util.Duration;
import lylesmal.openworldgame.domain.GameEntityFactory;
import lylesmal.openworldgame.domain.GameEntityOrientation;
import lylesmal.openworldgame.domain.GameEntityTypes;
import lylesmal.openworldgame.util.Helper;
import java.util.Map;
import java.util.Random;
import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.image;

public class MapApplication extends GameApplication {
    private Entity player;
    private Entity enemyPlayer;
    private Entity wall;
    private Entity cannon;
    private Entity brick;
    private Entity animatedObject;
    private GameEntityFactory gameEntity;
    private final int yValue;
    private Text txtPlayerHP;
    private Text lblPlayerHP;
    private Text txtScore;
    private Text lblScore;
    private Text txtSteps;
    private Text lblSteps;
    private Text txtXPos;
    private Text txtYPos;
    private String previousImage;
    Font font = new Font(Helper.fontName, 14);

    Point2D playerPreviousLocation;
    Point2D enemyPlayerPreviousLocation;

    boolean isGrootSpawned = false;

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

    private GameEntityOrientation playerOrientation = GameEntityOrientation.SOUTH;
    @Override
    protected void initInput() {
        double moveYSpeed = 1.5;
        double moveXSpeed = 1.5;

        //Player navigation
        getInput().addAction(new UserAction("player-move-up") {
            @Override
            protected void onAction() {
                HandlePlayerInRestrictedArea(player);
                updatePlayerPosition(player, GameEntityOrientation.NORTH);

                player.translateY(-moveYSpeed);
                handleCharacterMovementAnimations("character-up.png");
            }
        }, KeyCode.UP);

        getInput().addAction(new UserAction("player-move-down") {
            @Override
            protected void onAction() {
                HandlePlayerInRestrictedArea(player);
                updatePlayerPosition(player, GameEntityOrientation.SOUTH);

                player.translateY(moveYSpeed);
                handleCharacterMovementAnimations("character-down.png");
            }
        }, KeyCode.DOWN);

        getInput().addAction(new UserAction("player-move-left") {
            @Override
            protected void onAction() {
                HandlePlayerInRestrictedArea(player);
                updatePlayerPosition(player, GameEntityOrientation.WEST);

                player.translateX(-moveXSpeed);
                handleCharacterMovementAnimations("character-left.png");
            }
        }, KeyCode.LEFT);

        getInput().addAction(new UserAction("player-move-right") {
            @Override
            protected void onAction() {
                HandlePlayerInRestrictedArea(player);
                updatePlayerPosition(player, GameEntityOrientation.EAST);

                player.translateX(moveXSpeed);
                handleCharacterMovementAnimations("character-right.png");
            }
        }, KeyCode.RIGHT);

        getInput().addAction(new UserAction("player-jump") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(player);
                player.translateY(-10);
                getGameTimer().runOnceAfter(() -> player.translateY(10), Duration.seconds(0.2));
            }
        }, KeyCode.SPACE);

        //Player actions
        getInput().addAction(new UserAction("player-spawns-brick") {
            @Override
            protected void onActionBegin() {
                spawnBrick(Helper.fixEntityToGrid(player).getX(), Helper.fixEntityToGrid(player).getY(), "brick.png");
                HandlePlayerInRestrictedArea(player);
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("player-spawns-flag") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(player);
                spawnAnimatedObject(Helper.fixEntityToGrid(player).getX(), Helper.fixEntityToGrid(player).getY(), "flag.png", 4,32, 32);
            }
        }, KeyCode.Z);

        getInput().addAction(new UserAction("player-spawns-bullet") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(player);
                spawnCharacterBullet();
            }
        }, KeyCode.F);

        getInput().addAction(new UserAction("player-spawns-fence-vertical") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(player);
                spawnStationaryObject(Helper.fixEntityToGrid(player).getX(), Helper.fixEntityToGrid(player).getY(), "fence-vertical.png");
            }
        }, KeyCode.V);

        getInput().addAction(new UserAction("player-spawns-fence-horizontal") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(player);
                spawnStationaryObject(Helper.fixEntityToGrid(player).getX(), Helper.fixEntityToGrid(player).getY(), "fence-horizontal.png");
            }
        }, KeyCode.H);

        getInput().addAction(new UserAction("player-plants") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(player);
                String plants[] = {"shrubs.png", "strawberries.png", "granadilla.png"};

                spawnPlants(Helper.fixEntityToGrid(player).getX(), Helper.fixEntityToGrid(player).getY(), plants[random(0, 2)]);
            }
        }, KeyCode.P);

        getInput().addAction(new UserAction("player-spawns-cannon") {
            @Override
            protected void onActionBegin() {
                HandlePlayerInRestrictedArea(player);

                spawnCannon(Helper.fixEntityToGrid(player).getX(), Helper.fixEntityToGrid(player).getY());
            }
        }, KeyCode.C);

//        getInput().addAction(new UserAction("player-picks-up-flag") {
//            @Override
//            protected void onActionBegin() {
//                isPlayerInRestrictedArea(player);
//                pickUpFlags();
//            }
//        }, KeyCode.R);
    }

    @Override
    protected void initGame() {
        play("game-music.mp3");
        generateMap("map.png");

        spawnMainCharacter();
        spawnEnemyCharacter();
        moveEnemyTowardsPlayer();
        playerPreviousLocation = player.getPosition();
    }

    @Override
    protected void initUI() {

        lblPlayerHP.setText("HP: ");
        lblPlayerHP.setFont(font);
        lblPlayerHP.setX(10);
        lblPlayerHP.setY(yValue);

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

        updatePlayerPosition(player, playerOrientation);
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
        physicsWorld.addCollisionHandler(new CollisionHandler(GameEntityTypes.PLAYER, GameEntityTypes.LOOT) {
            @Override
            protected void onCollisionBegin(Entity player, Entity loot) {
                play("loot-pick-up.wav");
                loot.removeFromWorld();
                inc("score", +5);
            }
        });
    }

    //This method generates a map
    private void generateMap(String fileName) {
        entityBuilder()
                .at(0, 0)
                .view(texture(fileName, 800, 640)) // adjust size to match your large map
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
        spawnAnimatedObject(224, 192, "water-fountain.png", 3,48, 48);
        spawnAnimatedObject(672, 64, "pond.png", 2,64, 64);

        spawnAnimatedObject(0, 0, "waves.png", 5,160, 32);
        spawnAnimatedObject(160, 0, "waves.png", 4,160, 32);
        spawnAnimatedObject(320, 0, "waves.png", 4,160, 32);
        spawnAnimatedObject(480, 0, "waves.png", 3,160, 32);
        spawnAnimatedObject(640, 0, "waves.png", 3,160, 32);

        spawnAnimatedObject(0, 608, "waves.png", 5,160, 32);
        spawnAnimatedObject(160, 608, "waves.png", 4,160, 32);
        spawnAnimatedObject(320, 608, "waves.png", 4,160, 32);
        spawnAnimatedObject(480, 608, "waves.png", 3,160, 32);
        spawnAnimatedObject(640, 608, "waves.png", 3,160, 32);

        spawnStationaryObject(0,32, "no-waves.png");
        spawnStationaryObject(768,32, "no-waves.png");
    }

    //This method ensures the character can explore the entire map while remaining in the viewport
    private void followCharacter(boolean isFollowing) {
        if (isFollowing) {
            double distanceX = getAppWidth() / 2.0 - player.getWidth() / 2.0;
            double distanceY = getAppHeight() / 2.0 - player.getHeight() / 2.0;

            getGameScene().getViewport().bindToEntity(player, distanceX, distanceY);
            getGameScene().getViewport().setBounds(0, 0, 800, 640);
            getGameScene().getViewport().setLazy(true);
        }
    }

    //This method spawns your character
    private void spawnMainCharacter() {
        player = gameEntity.createCharacter();
        followCharacter(true);
    }

    //This method spawns an enemy character
    private void spawnEnemyCharacter() {
        enemyPlayer = gameEntity.createEnemy();
    }

    //TODO: Make this a helper method that can be used for any entity to move towards another entity
    private void moveEnemyTowardsPlayer() {
        getGameTimer().runAtInterval(() -> {
            double deltaX = player.getX() - enemyPlayer.getX();
            double deltaY = player.getY() - enemyPlayer.getY();
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (distance > 10 && distance < 100) {
                double moveX = (deltaX / distance) * 4.0; // Adjust speed as needed
                double moveY = (deltaY / distance) * 4.0; // Adjust speed as needed

                enemyPlayer.translateX(moveX);
                enemyPlayer.translateY(moveY);
                HandleEnemyPlayerInRestrictedArea(enemyPlayer);
                //change the animatedChannel based on the direction the enemy is moving towards
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 0) {
                        handleEnemyMovementAnimations("character-right.png");
                    } else {
                        handleEnemyMovementAnimations("character-left.png");
                    }
                } else {
                    if (deltaY > 0) {
                        handleEnemyMovementAnimations("character-down.png");
                    } else {
                        handleEnemyMovementAnimations("character-up.png");
                    }
                }
            }

            int hp = getWorldProperties().intProperty("hp").get();
            if(distance <= 10 & hp >= 0 ){
                play("damage.mp3");
                inc("hp", -1);
                if (hp == 0){
                    player.removeFromWorld();
                    play("death.mp3");
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

        player.getViewComponent().clearChildren();
        player.getViewComponent().addChild(texture);

        if(nextFrameIndex <= 4){
            nextFrameIndex++;
        }
        else{
            nextFrameIndex = 0;
        }

        previousImage = fileName;
    }
    //This method handles character animations
    public void handleEnemyMovementAnimations(String fileName){
        Image image = image(fileName);

        AnimationChannel channel = new AnimationChannel(image, 4, 16, 32, Duration.seconds(1.5), 0, 3);

        //Create the animated texture
        AnimatedTexture texture = new AnimatedTexture(channel);
        texture.playFrom(nextFrameIndex);

        enemyPlayer.getViewComponent().clearChildren();
        enemyPlayer.getViewComponent().addChild(texture);

        if(nextFrameIndex <= 4){
            nextFrameIndex++;
        }
        else{
            nextFrameIndex = 0;
        }

        previousImage = fileName;
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
    private void spawnBrick(double x, double y, String fileName) {
        if ((x > 64 && x < 728) && (y > 54 && y < 547)) {
        play("brick.wav");
        gameEntity.createBrick(x, y, fileName);
        }
    }

    //This method spawns a cannon
    private void spawnCannon(double x, double y) {
        if(cannon==null){
        cannon = gameEntity.createCannon(x, y);

         getGameTimer().runAtInterval(() ->
         spawnBullet(),
         Duration.seconds(2));

         handleBulletImpact();
        }
    }

    //This method spawns a bullet
    private void spawnBullet(){
        play("explosion.wav");
        gameEntity.createBullet(cannon.getRightX()-5, cannon.getY(), 300,+5,0);
    }


    private void spawnCharacterBullet(){
        play("explosion.wav");
        switch (playerOrientation){
            case EAST:
                gameEntity.createBullet(player.getX() - 5, player.getY(), 300, +5, 0);
                break;
            case WEST:
                gameEntity.createBullet(player.getX(), player.getY(), 300, -5, 0);
                break;
            case NORTH:
                gameEntity.createBullet(player.getX(), player.getY(), 300, 0, -5);
                break;
            case SOUTH:
                gameEntity.createBullet(player.getX(), player.getY(), 300, 0, +5);
                break;
        }
    }

    //The method spawns an animated object
    private void spawnAnimatedObject(double x, double y, String fileName, int frames, int frameWidth, int frameHeight) {
        animatedObject = gameEntity.createStationaryAnimatedObject(x, y, fileName, frames, frameWidth, frameHeight);
    }


    private void HandlePlayerInRestrictedArea(Entity player) {
        boolean inRestrictedArea = getGameWorld().getEntitiesByType(GameEntityTypes.WALL,GameEntityTypes.BRICK)
                .stream()
                .anyMatch(land ->
                        player.getBoundingBoxComponent()
                                .isCollidingWith(land.getBoundingBoxComponent())
                );

        if (inRestrictedArea) {
            player.setPosition(playerPreviousLocation);
        } else {
            playerPreviousLocation = player.getPosition();
        }
    }

    private void HandleEnemyPlayerInRestrictedArea(Entity player) {
        boolean inRestrictedArea = getGameWorld().getEntitiesByType(GameEntityTypes.WALL,GameEntityTypes.BRICK)
                .stream()
                .anyMatch(land ->
                        enemyPlayer.getBoundingBoxComponent()
                                .isCollidingWith(land.getBoundingBoxComponent())
                );

        if (inRestrictedArea) {
            player.setPosition(enemyPlayerPreviousLocation);
        } else {
            enemyPlayerPreviousLocation = player.getPosition();
        }
    }

//    private void pickUpFlags(){
//        PhysicsWorld physicsWorld = getPhysicsWorld();
//        physicsWorld.addCollisionHandler(new CollisionHandler(GameEntityTypes.PLAYER, GameEntityTypes.ANIMATED_OBJECT) {
//            @Override
//            protected void onCollisionBegin(Entity player, Entity flag) {
//                flag.setVisible(false);
//            }
//        });
//    }

    private void handleBulletImpact(){
        PhysicsWorld physicsWorld = getPhysicsWorld();
        physicsWorld.addCollisionHandler(new CollisionHandler(GameEntityTypes.WALL, GameEntityTypes.BULLET) {
            @Override
            protected void onCollisionBegin(Entity wall, Entity bullet) {
                play("brick.wav");
                wall.removeFromWorld();
                bullet.removeFromWorld();
            }
        });

        physicsWorld.addCollisionHandler(new CollisionHandler(GameEntityTypes.PLAYER, GameEntityTypes.BULLET) {
            @Override
            protected void onCollisionBegin(Entity player, Entity bullet) {
                inc("hp", -1);
                int hp = getWorldProperties().intProperty("hp").get();
                if (hp == 0){
                    player.removeFromWorld();
                }
                bullet.removeFromWorld();
            }
        });
    }

    //This updates the player coordinates that appear onscreen
    private void updatePlayerPosition(Entity player, GameEntityOrientation currentOrientation) {
        if ((player.getX() % 8 == 0) || (player.getY() % 8 == 0)) {
            inc("steps", +1);
        }
        txtXPos.setText("X: " + (int) player.getX());
        txtYPos.setText("Y: " + (int) player.getY());
        playerOrientation = currentOrientation;
    }

    @Override
    protected void onUpdate(double tpf){
     //TODO: Check if the player is moving at a timed interval (e.g 0.5s)
     //TODO: Stop the moving player animation texture
    }

}
