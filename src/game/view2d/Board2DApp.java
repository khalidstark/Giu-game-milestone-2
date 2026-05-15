package game.view2d;

import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Dasher;
import game.engine.monsters.MultiTasker;
import game.engine.monsters.Monster;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Board2DApp extends Application {

    private static final double DEFAULT_W = 1500;
    private static final double DEFAULT_H = 900;
    private static final double SIDE_MARGIN = 16;
    private static final double LEFT_PANEL_W = 270;
    private static final double RIGHT_PANEL_W = 286;
    private static final double PANEL_GAP = 18;
    private static final double HUD_TOP = 34;
    private static final double HUD_BOTTOM = 34;
    private static final double TOKEN_SIZE = 76;
    private static final double TOKEN_MOVE_SECONDS = 0.72;
    private static final double BOARD_MAX_TILE = 75;
    private static final double BOARD_MIN_TILE = 48;
    private static final double EFFECT_SPEED_SCALE = 1.5;
    private static final double EFFECT_DIM_ALPHA = 0.42;
    private static final double EFFECT_FADE_SECONDS = 0.25;

    private final Map<String, Image> images = new HashMap<>();
    private final Map<String, SpriteSheetEffect> effects = new HashMap<>();
    private Canvas canvas;
    private Game game;
    private Role selectedRole = Role.SCARER;
    private TokenView playerToken;
    private TokenView opponentToken;
    private TokenView followToken;
    private long lastFrameNs;
    private String status = "Space to roll";
    private String eventLine = "Preparing board";
    private final List<String> eventLog = new ArrayList<>();
    private int lastVisualRoll = 1;
    private boolean gameOver;
    private ActiveCellEffect activeEffect;
    private TurnResolution pendingResolution;
    private IntroSequence introSequence;
    private SoundManager2D sounds;
    private String latestCardTitle = "No card drawn";
    private String latestCardDetail = "Land on a card cell";

    private static final double INTRO_FALLBACK_SECONDS = 2.8;
    private static final double INTRO_POST_AUDIO_SECONDS = 0.45;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        selectedRole = parseRole(getParameters().getRaw().toArray(new String[0]));
        loadImages();
        sounds = new SoundManager2D(getClass().getClassLoader(), 0.10);
        sounds.startTheme();

        StackPane root = new StackPane();
        canvas = new Canvas(DEFAULT_W, DEFAULT_H);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        root.getChildren().add(canvas);

        try {
            game = new Game(selectedRole);
            playerToken = createToken(game.getPlayer(), Color.web("#67E8F9"), -18);
            opponentToken = createToken(game.getOpponent(), Color.web("#FFB56E"), 18);
            followToken = playerToken;
            eventLine = String.format(Locale.ROOT, "You are %s vs %s",
                    game.getPlayer().getName(), game.getOpponent().getName());
            addEvent("Game start: " + eventLine);
        } catch (IOException e) {
            status = "Could not load game data";
            eventLine = e.getMessage() == null ? "CSV load failed" : e.getMessage();
            addEvent(eventLine);
        }

        Scene scene = new Scene(root, DEFAULT_W, DEFAULT_H, Color.web("#07101A"));
        scene.setOnKeyPressed(e -> {
            if (isIntroPlaying()) {
                return;
            }
            if (e.getCode() == KeyCode.SPACE) {
                playTurn();
            } else if (e.getCode() == KeyCode.P) {
                usePowerup();
            } else if (e.getCode() == KeyCode.R) {
                followToken = game != null && game.getCurrent() == game.getOpponent() ? opponentToken : playerToken;
            }
        });
        scene.setOnMouseClicked(e -> handleCanvasClick(e.getX(), e.getY(), stage));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = lastFrameNs == 0 ? 0 : (now - lastFrameNs) / 1_000_000_000.0;
                lastFrameNs = now;
                update(dt);
                draw();
            }
        };
        timer.start();

        stage.setTitle("DooR DasH - 2D Board");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(840);
        stage.setResizable(true);
        stage.show();
        stage.toFront();
        root.requestFocus();
        Platform.runLater(() -> {
            root.requestFocus();
            startIntroSequence();
        });
    }

    @Override
    public void stop() {
        disposeSounds();
    }

    private Role parseRole(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--role=")) {
                return "laugher".equalsIgnoreCase(arg.substring(7)) ? Role.LAUGHER : Role.SCARER;
            }
        }
        return Role.SCARER;
    }

    private void loadImages() {
        images.put("background", loadImage("2d/board/background.png"));
        images.put("normal", loadImage("2d/board/normal-cell.png"));
        images.put("door", loadImage("2d/board/door-cell.png"));
        images.put("card", loadImage("2d/board/card-cell.png"));
        images.put("monster", loadImage("2d/board/monster-cell.png"));
        images.put("conveyor", loadImage("2d/board/conveyor-cell.png"));
        images.put("sock", loadImage("2d/board/sock-cell.png"));
        loadEffects();
    }

    private void loadEffects() {
        loadEffect("card", "Card cell", "2d/board/animations/card-effect.png",
                "2d/board/animations/card-effect.json");
        loadEffect("conveyor", "Conveyor belt", "2d/board/animations/conveyor-effect.png",
                "2d/board/animations/conveyor-effect.json");
        loadEffect("sock", "Contamination sock", "2d/board/animations/sock-effect.png",
                "2d/board/animations/sock-effect.json");
        loadEffect("door-scarer", "Scarer door", "2d/board/animations/door-scarer-effect.png",
                "2d/board/animations/door-scarer-effect.json");
        loadEffect("door-laugher", "Laugher door", "2d/board/animations/door-laugher-effect.png",
                "2d/board/animations/door-laugher-effect.json");
        loadEffect("monster-scarer", "Scarer monster cell", "2d/board/animations/monster-scarer-effect.png",
                "2d/board/animations/monster-scarer-effect.json");
        loadEffect("monster-laugher", "Laugher monster cell", "2d/board/animations/monster-laugher-effect.png",
                "2d/board/animations/monster-laugher-effect.json");
    }

    private void loadEffect(String key, String label, String imagePath, String jsonPath) {
        Image image = loadImage(imagePath);
        if (image == null) {
            return;
        }
        List<EffectFrame> frames = loadEffectFrames(jsonPath, image);
        effects.put(key, new SpriteSheetEffect(label, image, frames));
    }

    private Image loadImage(String path) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(path);
            return is == null ? null : new Image(is);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<EffectFrame> loadEffectFrames(String path, Image sheet) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                return fallbackEffectFrames(sheet);
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<EffectFrame> frames = new ArrayList<>();
            Pattern pattern = Pattern.compile(
                    "\"frame_\\d+\"\\s*:\\s*\\{\\s*\"frame\"\\s*:\\s*\\{\\s*\"x\"\\s*:\\s*(\\d+)\\s*,\\s*\"y\"\\s*:\\s*(\\d+)\\s*,\\s*\"w\"\\s*:\\s*(\\d+)\\s*,\\s*\"h\"\\s*:\\s*(\\d+)\\s*\\}.*?\"duration\"\\s*:\\s*(\\d+)",
                    Pattern.DOTALL);
            Matcher matcher = pattern.matcher(json);
            while (matcher.find()) {
                double x = Double.parseDouble(matcher.group(1));
                double y = Double.parseDouble(matcher.group(2));
                double w = Double.parseDouble(matcher.group(3));
                double h = Double.parseDouble(matcher.group(4));
                double duration = Math.max(0.03, Double.parseDouble(matcher.group(5)) / 1000.0) * EFFECT_SPEED_SCALE;
                frames.add(new EffectFrame(x, y, w, h, duration));
            }
            return frames.isEmpty() ? fallbackEffectFrames(sheet) : frames;
        } catch (Exception ignored) {
            return fallbackEffectFrames(sheet);
        }
    }

    private List<EffectFrame> fallbackEffectFrames(Image sheet) {
        List<EffectFrame> frames = new ArrayList<>();
        double frameW = sheet.getWidth() / 6.0;
        double frameH = sheet.getHeight() / 6.0;
        double duration = 0.08 * EFFECT_SPEED_SCALE;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                frames.add(new EffectFrame(col * frameW, row * frameH, frameW, frameH, duration));
            }
        }
        return frames;
    }

    private TokenView createToken(Monster monster, Color color, double laneOffset) {
        TokenView token = new TokenView();
        token.monster = monster;
        token.color = color;
        token.laneOffset = laneOffset;
        token.previousCell = normalize(monster.getPosition());
        token.cellIndex = token.previousCell;
        token.image = loadImage(monsterImagePath(monster));
        return token;
    }

    private String monsterImagePath(Monster monster) {
        return String.format(Locale.ROOT, "2d/monsters/CHR-P%02d.png", monsterAssetNumber(monster));
    }

    private int monsterAssetNumber(Monster monster) {
        String name = monster.getName().toLowerCase(Locale.ROOT);
        if (name.contains("mike")) return 1;
        if (name.contains("sullivan") || name.contains("sulley")) return 2;
        if (name.contains("randall")) return 3;
        if (name.contains("celia")) return 4;
        if (name.contains("fungus")) return 5;
        if (name.contains("yeti")) return 6;
        return Math.abs(monster.getName().hashCode()) % 8 + 1;
    }

    private void playTurn() {
        if (game == null || gameOver || isIntroPlaying() || isTurnAnimating()) {
            return;
        }

        Monster moving = game.getCurrent();
        boolean movingPlayer = moving == game.getPlayer();
        int playerBefore = normalize(game.getPlayer().getPosition());
        int opponentBefore = normalize(game.getOpponent().getPosition());
        int actorBefore = normalize(moving.getPosition());
        int actorEnergyBefore = moving.getEnergy();
        Role actorRoleBefore = moving.getRole();
        boolean wasFrozen = moving.isFrozen();

        try {
            game.playTurn();
            int playerAfter = normalize(game.getPlayer().getPosition());
            int opponentAfter = normalize(game.getOpponent().getPosition());
            TokenView actorToken = movingPlayer ? playerToken : opponentToken;
            int actorAfter = normalize(moving.getPosition());
            int triggeredCell = game.getLastTriggeredCellIndex();
            lastVisualRoll = game.getLastRoll() > 0
                    ? game.getLastRoll()
                    : inferVisualDiceRoll(actorBefore, actorAfter);

            if (wasFrozen && actorBefore == normalize(moving.getPosition())) {
                eventLine = moving.getName() + " thawed out and skipped the move";
            } else {
                eventLine = movementEventText(moving, actorBefore, actorAfter, lastVisualRoll);
            }

            SpriteSheetEffect effect = wasFrozen ? null : effectForCell(triggeredCell);
            updateLatestCardNotice(game.getLastDrawnCard());
            addCellImpactNotice(moving, actorRoleBefore, actorEnergyBefore, triggeredCell);
            if (effect != null) {
                prepareEffectTurn(actorToken, movingPlayer, actorBefore, playerBefore, opponentBefore,
                        playerAfter, opponentAfter, triggeredCell, effect, eventLine);
            } else {
                syncToken(playerToken, playerBefore, playerAfter);
                syncToken(opponentToken, opponentBefore, opponentAfter);
                TokenView changedToken = firstChangedToken(playerBefore, playerAfter, opponentBefore, opponentAfter);
                followToken = changedToken == null ? actorToken : changedToken;
                completeTurn(eventLine, "Turn: " + game.getCurrent().getName());
            }
        } catch (InvalidMoveException e) {
            eventLine = moving.getName() + " was blocked by the other token";
            status = "Move blocked";
            followToken = movingPlayer ? playerToken : opponentToken;
            addEvent(eventLine);
        }
    }

    private void usePowerup() {
        if (game == null || gameOver || isIntroPlaying() || isTurnAnimating()) {
            return;
        }

        Monster current = game.getCurrent();
        try {
            game.usePowerup();
            playBoardSfx(SoundManager2D.POWERUP);
            eventLine = current.getName() + " used a powerup";
            status = "Turn: " + game.getCurrent().getName();
            addEvent(eventLine);
        } catch (OutOfEnergyException e) {
            eventLine = current.getName() + " needs more energy";
            status = "Powerup blocked";
            addEvent(eventLine);
        }
    }

    private void prepareEffectTurn(TokenView actorToken, boolean movingPlayer, int actorBefore,
                                   int playerBefore, int opponentBefore, int playerAfter, int opponentAfter,
                                   int triggeredCell, SpriteSheetEffect effect, String finalEventLine) {
        TokenView waitingToken = movingPlayer ? opponentToken : playerToken;
        int waitingCell = movingPlayer ? opponentBefore : playerBefore;
        setTokenStatic(waitingToken, waitingCell);
        syncToken(actorToken, actorBefore, triggeredCell);
        followToken = actorToken;
        status = "Cell effect";
        eventLine = effect.label + " triggered on cell " + triggeredCell;
        playCellSfx(triggeredCell);
        pendingResolution = new TurnResolution(playerAfter, opponentAfter, finalEventLine,
                "Turn: " + game.getCurrent().getName(), actorToken, triggeredCell, effect);
    }

    private void completeTurn(String finalEventLine, String finalStatus) {
        eventLine = finalEventLine;
        status = finalStatus;
        addEvent(eventLine);
        updateWinner();
    }

    private void updateLatestCardNotice(Card card) {
        if (card == null) {
            return;
        }
        latestCardTitle = "Card: " + card.getName();
        latestCardDetail = card.getDescription();
        addEvent(latestCardTitle + " - " + latestCardDetail);
    }

    private void addCellImpactNotice(Monster actor, Role actorRoleBefore, int actorEnergyBefore, int triggeredCell) {
        if (triggeredCell < 0) {
            return;
        }
        Cell cell = engineCellAt(triggeredCell);
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            int energyDelta = actor.getEnergy() - actorEnergyBefore;
            if (energyDelta == 0) {
                return;
            }
            addEvent(String.format(Locale.ROOT, "%s DOOR (%d): %s %s %d energy on %s",
                    door.getRole().name(), door.getEnergy(), shortMonsterName(actor.getName()),
                    energyDelta > 0 ? "gained" : "lost", Math.abs(energyDelta), door.getName()));
        } else if (cell instanceof MonsterCell) {
            Monster cellMonster = ((MonsterCell) cell).getCellMonster();
            String stationed = cellMonster == null ? cell.getName() : shortMonsterName(cellMonster.getName());
            int energyDelta = actor.getEnergy() - actorEnergyBefore;
            if (cellMonster != null && cellMonster.getRole() == actorRoleBefore) {
                addEvent(String.format(Locale.ROOT, "MONSTER CELL: %s matched %s, power activated",
                        shortMonsterName(actor.getName()), stationed));
            } else if (energyDelta != 0) {
                addEvent(String.format(Locale.ROOT, "MONSTER CELL: %s %s %d energy vs %s",
                        shortMonsterName(actor.getName()), energyDelta > 0 ? "gained" : "lost",
                        Math.abs(energyDelta), stationed));
            } else {
                addEvent(String.format(Locale.ROOT, "MONSTER CELL: %s faced %s with no energy change",
                        shortMonsterName(actor.getName()), stationed));
            }
        }
    }

    private void setTokenStatic(TokenView token, int cell) {
        token.previousCell = normalize(cell);
        token.cellIndex = normalize(cell);
        token.progress = 1.0;
        token.moving = false;
    }

    private void updateWinner() {
        Monster winner = game.getWinner();
        if (winner != null) {
            gameOver = true;
            status = "Winner: " + winner.getName();
            eventLine = winner.getName() + " reached cell 99 with enough energy";
            addEvent(eventLine);
        }
    }

    private void handleCanvasClick(double x, double y, Stage stage) {
        if (isIntroPlaying()) {
            return;
        }
        double buttonX = SIDE_MARGIN;
        double buttonY = canvas.getHeight() - SIDE_MARGIN - 58;
        if (x >= buttonX && x <= buttonX + LEFT_PANEL_W && y >= buttonY && y <= buttonY + 48) {
            returnToLobby(stage);
        }
    }

    private void returnToLobby(Stage stage) {
        try {
            launchLobbyProcess();
            stage.close();
            Platform.exit();
        } catch (IOException e) {
            eventLine = "Could not open lobby";
            addEvent(e.getMessage() == null ? eventLine : e.getMessage());
        }
    }

    private void launchLobbyProcess() throws IOException {
        String javaExe = ProcessHandle.current().info().command()
                .orElseGet(() -> System.getProperty("java.home") + "/bin/java");
        String cp = System.getProperty("java.class.path");
        List<String> cmd = new ArrayList<>();
        cmd.add(javaExe);
        addJavaFxRuntimeArgs(cmd, cp);
        cmd.add("-cp");
        cmd.add(cp);
        cmd.add("--add-opens"); cmd.add("java.base/java.lang=ALL-UNNAMED");
        cmd.add("--add-opens"); cmd.add("java.desktop/sun.awt=ALL-UNNAMED");
        cmd.add("--add-opens"); cmd.add("java.desktop/sun.java2d=ALL-UNNAMED");
        cmd.add("game.view2d.LandingLauncher");
        cmd.add("--skip-intro-flow");
        new ProcessBuilder(cmd).inheritIO().start();
    }

    private void addEvent(String event) {
        eventLog.add(0, event);
        while (eventLog.size() > 8) {
            eventLog.remove(eventLog.size() - 1);
        }
    }

    private void syncToken(TokenView token, int before, int after) {
        token.previousCell = normalize(before);
        token.cellIndex = normalize(after);
        token.progress = before == after ? 1.0 : 0.0;
        token.moving = before != after;
    }

    private TokenView firstChangedToken(int playerBefore, int playerAfter, int opponentBefore, int opponentAfter) {
        if (playerBefore != playerAfter) {
            return playerToken;
        }
        if (opponentBefore != opponentAfter) {
            return opponentToken;
        }
        return null;
    }

    private boolean hasMovingToken() {
        return playerToken != null && playerToken.moving
                || opponentToken != null && opponentToken.moving;
    }

    private boolean isTurnAnimating() {
        return hasMovingToken() || activeEffect != null || pendingResolution != null;
    }

    private boolean isIntroPlaying() {
        return introSequence != null && introSequence.active;
    }

    private void startIntroSequence() {
        if (game == null || playerToken == null || opponentToken == null || isIntroPlaying()) {
            return;
        }
        introSequence = new IntroSequence(new IntroSlide[] {
                createIntroSlide("PLAYER 1", game.getPlayer(), playerToken.image),
                createIntroSlide("PLAYER 2", game.getOpponent(), opponentToken.image)
        });
        status = "Introducing monsters";
        eventLine = "Meet the competitors";
        beginIntroSlide();
    }

    private IntroSlide createIntroSlide(String playerLabel, Monster monster, Image image) {
        int number = monsterAssetNumber(monster);
        String audioPath = introAudioPath(number);
        double duration = audioPath == null
                ? INTRO_FALLBACK_SECONDS
                : introAudioDuration(number) + INTRO_POST_AUDIO_SECONDS;
        return new IntroSlide(playerLabel, monster.getName(), monster.getRole().name(),
                introQuote(monster), image, audioPath, duration);
    }

    private String introAudioPath(int monsterNumber) {
        String path = String.format(Locale.ROOT, "2d/audio/monster-intros/monster-%02d.m4a", monsterNumber);
        URL url = getClass().getClassLoader().getResource(path);
        return url == null ? null : path;
    }

    private double introAudioDuration(int monsterNumber) {
        switch (monsterNumber) {
            case 1:
                return 6.75;
            case 2:
                return 4.70;
            case 3:
                return 4.60;
            case 4:
                return 5.37;
            case 5:
                return 3.84;
            case 7:
                return 4.34;
            case 8:
                return 3.45;
            default:
                return INTRO_FALLBACK_SECONDS;
        }
    }

    private String introQuote(Monster monster) {
        String name = monster.getName().toLowerCase(Locale.ROOT);
        if (name.contains("mike")) {
            return "I'm on a roll!";
        }
        if (name.contains("sullivan") || name.contains("sulley")) {
            return "Top scarer coming through.";
        }
        if (name.contains("randall")) {
            return "You won't see me coming.";
        }
        if (name.contains("celia")) {
            return "Googly Bear, stay sharp!";
        }
        if (name.contains("fungus")) {
            return "Please don't panic.";
        }
        if (name.contains("yeti")) {
            return "Welcome to the Himalayas!";
        }
        return "Ready for the scare floor.";
    }

    private void beginIntroSlide() {
        if (!isIntroPlaying()) {
            return;
        }
        introSequence.elapsed = 0;
        IntroSlide slide = introSequence.currentSlide();
        playBoardSfx(slide.audioPath);
    }

    private void updateIntro(double dt) {
        if (!isIntroPlaying()) {
            return;
        }
        introSequence.elapsed += dt;
        if (introSequence.elapsed >= introSequence.currentSlide().duration) {
            advanceIntroSlide();
        }
    }

    private void advanceIntroSlide() {
        if (!isIntroPlaying()) {
            return;
        }
        if (introSequence.index < introSequence.slides.length - 1) {
            introSequence.index++;
            beginIntroSlide();
            return;
        }
        introSequence.active = false;
        introSequence = null;
        status = "Space to roll";
        eventLine = String.format(Locale.ROOT, "Game start: You are %s vs %s",
                game.getPlayer().getName(), game.getOpponent().getName());
    }

    private void playBoardSfx(String resourcePath) {
        if (resourcePath == null || sounds == null) {
            return;
        }
        sounds.playSfxPausingTheme(resourcePath);
    }

    private void playCellSfx(int cellIndex) {
        String resourcePath = soundForCell(cellIndex);
        playBoardSfx(resourcePath);
        if (SoundManager2D.MONSTER_CELL_SCARER.equals(resourcePath)) {
            playBoardSfx(resourcePath);
        }
    }

    private void disposeSounds() {
        if (sounds == null) {
            return;
        }
        sounds.dispose();
        sounds = null;
    }

    private int inferVisualDiceRoll(int before, int after) {
        int delta = forwardDistance(before, after);
        if (delta >= 1 && delta <= 6) {
            return delta;
        }
        return 1 + Math.abs(after - before) % 6;
    }

    private String movementEventText(Monster monster, int before, int after, int roll) {
        String base = String.format(Locale.ROOT, "%s rolled %d and moved %d -> %d",
                monster.getName(), roll, before, after);
        String reason = movementReason(monster, before, after, roll);
        return reason.isEmpty() ? base : base + " (" + reason + ")";
    }

    private String movementReason(Monster monster, int before, int after, int roll) {
        int distance = forwardDistance(before, after);
        String type = monster.getClass().getSimpleName();
        if ("Dasher".equals(type)) {
            if (distance == roll * 3) {
                return "Dasher momentum x3";
            }
            if (distance == roll * 2) {
                return "Dasher speed x2";
            }
        }
        if ("MultiTasker".equals(type) && distance == roll / 2) {
            return "Multitasker half-step";
        }
        if (distance != roll && distance <= 24) {
            return "final move " + distance + " spaces after ability/effect";
        }
        if (distance != roll) {
            return "cell effect changed the destination";
        }
        return "";
    }

    private int forwardDistance(int from, int to) {
        return (normalize(to) - normalize(from) + Constants.BOARD_SIZE) % Constants.BOARD_SIZE;
    }

    private void update(double dt) {
        updateIntro(dt);
        updateToken(playerToken, dt);
        updateToken(opponentToken, dt);
        updateCellEffect(dt);
    }

    private void updateCellEffect(double dt) {
        if (pendingResolution != null && activeEffect == null && !hasMovingToken()) {
            activeEffect = new ActiveCellEffect(pendingResolution.effect, pendingResolution.triggeredCell);
        }

        if (activeEffect == null) {
            return;
        }

        activeEffect.elapsed += dt;
        if (activeEffect.isDone()) {
            TurnResolution resolution = pendingResolution;
            activeEffect = null;
            pendingResolution = null;
            if (resolution != null) {
                animateFinalResolution(resolution);
            }
        }
    }

    private void animateFinalResolution(TurnResolution resolution) {
        TokenView changedToken = null;
        if (playerToken.cellIndex != normalize(resolution.playerAfter)) {
            changedToken = playerToken;
        } else if (opponentToken.cellIndex != normalize(resolution.opponentAfter)) {
            changedToken = opponentToken;
        }
        animateTokenTo(playerToken, resolution.playerAfter);
        animateTokenTo(opponentToken, resolution.opponentAfter);
        followToken = changedToken == null ? resolution.actorToken : changedToken;
        completeTurn(resolution.finalEventLine, resolution.finalStatus);
    }

    private void animateTokenTo(TokenView token, int destination) {
        int normalized = normalize(destination);
        token.previousCell = normalize(token.cellIndex);
        token.cellIndex = normalized;
        token.progress = token.previousCell == token.cellIndex ? 1.0 : 0.0;
        token.moving = token.previousCell != token.cellIndex;
    }

    private void updateToken(TokenView token, double dt) {
        if (token == null || !token.moving) {
            return;
        }
        token.progress += dt / TOKEN_MOVE_SECONDS;
        if (token.progress >= 1.0) {
            token.progress = 1.0;
            token.moving = false;
        }
    }

    private void draw() {
        if (canvas == null) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);
        drawBackground(gc, w, h);
        if (game != null) {
            drawBoard(gc, w, h);
            drawToken(gc, opponentToken);
            drawToken(gc, playerToken);
        }
        drawInterface(gc, w, h);
        drawCellEffectOverlay(gc, w, h);
        drawIntroOverlay(gc, w, h);
    }

    private void drawBackground(GraphicsContext gc, double w, double h) {
        Image bg = images.get("background");
        if (bg == null) {
            gc.setFill(Color.web("#07101A"));
            gc.fillRect(0, 0, w, h);
            return;
        }

        double scale = Math.max(w / bg.getWidth(), h / bg.getHeight());
        double dw = bg.getWidth() * scale;
        double dh = bg.getHeight() * scale;
        gc.drawImage(bg, (w - dw) * 0.5, (h - dh) * 0.5, dw, dh);
        gc.setFill(Color.rgb(4, 8, 20, 0.20));
        gc.fillRect(0, 0, w, h);
    }

    private void drawBoard(GraphicsContext gc, double w, double h) {
        double tile = tileSize(w, h);
        double boardW = tile * Constants.BOARD_COLS;
        double boardH = tile * Constants.BOARD_ROWS;
        double x0 = (w - boardW) * 0.5;
        double y0 = HUD_TOP + Math.max(6, (h - HUD_TOP - HUD_BOTTOM - boardH) * 0.48);

        gc.setFill(Color.rgb(2, 4, 12, 0.36));
        gc.fillRoundRect(x0 - 22, y0 - 18, boardW + 44, boardH + 44, 28, 28);
        drawRowRails(gc, x0, y0, tile);

        for (int index = 0; index < Constants.BOARD_SIZE; index++) {
            Point2 topLeft = cellTopLeft(index, x0, y0, tile);
            Image cellImage = imageForCell(index);
            if (cellImage != null) {
                gc.drawImage(cellImage, topLeft.x, topLeft.y, tile, tile);
            } else {
                gc.setFill(fallbackCellColor(index));
                gc.fillRoundRect(topLeft.x + 3, topLeft.y + 3, tile - 6, tile - 6, 10, 10);
            }
            drawCellNumber(gc, index, topLeft.x, topLeft.y, tile);
            drawStartFinishBadge(gc, index, topLeft.x, topLeft.y, tile);
        }
    }

    private void drawRowRails(GraphicsContext gc, double x0, double y0, double tile) {
        double rowH = tile * 0.18;
        for (int row = 0; row < Constants.BOARD_ROWS; row++) {
            double y = y0 + (Constants.BOARD_ROWS - 1 - row) * tile + tile * 0.82;
            gc.setFill(Color.rgb(0, 0, 0, 0.34));
            gc.fillRoundRect(x0 + tile * 0.08, y, tile * 9.84, rowH, rowH, rowH);
            gc.setFill(Color.rgb(93, 217, 233, 0.11));
            gc.fillRoundRect(x0 + tile * 0.28, y + rowH * 0.22, tile * 9.44, rowH * 0.22, rowH, rowH);
        }

        gc.setStroke(Color.rgb(94, 123, 143, 0.45));
        gc.setLineWidth(Math.max(4, tile * 0.05));
        for (int row = 0; row < Constants.BOARD_ROWS - 1; row++) {
            boolean rightSide = row % 2 == 0;
            double x = x0 + (rightSide ? tile * 9.5 : tile * 0.5);
            double yA = y0 + (Constants.BOARD_ROWS - 1 - row) * tile + tile * 0.50;
            double yB = y0 + (Constants.BOARD_ROWS - 2 - row) * tile + tile * 0.50;
            gc.strokeLine(x, yA, x, yB);
        }
    }

    private Image imageForCell(int index) {
        if (index == Constants.STARTING_POSITION || index == Constants.WINNING_POSITION) {
            return images.get("normal");
        }
        Cell cell = engineCellAt(index);
        if (cell instanceof MonsterCell) {
            return images.get("monster");
        }
        if (cell instanceof ConveyorBelt) {
            return images.get("conveyor");
        }
        if (cell instanceof ContaminationSock) {
            return images.get("sock");
        }
        if (cell instanceof CardCell) {
            return images.get("card");
        }
        if (index % 2 == 1) {
            return images.get("door");
        }
        return images.get("normal");
    }

    private SpriteSheetEffect effectForCell(int index) {
        if (index < 0 || game == null) {
            return null;
        }
        Cell cell = engineCellAt(index);
        if (cell instanceof CardCell) {
            return effects.get("card");
        }
        if (cell instanceof ConveyorBelt) {
            return effects.get("conveyor");
        }
        if (cell instanceof ContaminationSock) {
            return effects.get("sock");
        }
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            return door.getRole() == Role.SCARER ? effects.get("door-scarer") : effects.get("door-laugher");
        }
        if (cell instanceof MonsterCell) {
            Monster cellMonster = ((MonsterCell) cell).getCellMonster();
            if (cellMonster != null && cellMonster.getRole() == Role.SCARER) {
                return effects.get("monster-scarer");
            }
            return effects.get("monster-laugher");
        }
        return null;
    }

    private String soundForCell(int index) {
        if (index < 0 || game == null) {
            return null;
        }
        Cell cell = engineCellAt(index);
        if (cell instanceof ContaminationSock) {
            return SoundManager2D.CELL_CONTAMINATION_SOCK;
        }
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            return door.getRole() == Role.SCARER ? SoundManager2D.DOOR_SCARER : SoundManager2D.DOOR_LAUGHER;
        }
        if (cell instanceof MonsterCell) {
            Monster cellMonster = ((MonsterCell) cell).getCellMonster();
            if (cellMonster != null && cellMonster.getRole() == Role.SCARER) {
                return SoundManager2D.MONSTER_CELL_SCARER;
            }
            return SoundManager2D.MONSTER_CELL_LAUGHER;
        }
        return null;
    }

    private Color fallbackCellColor(int index) {
        Cell cell = engineCellAt(index);
        if (cell instanceof MonsterCell) return Color.web("#5F4E91");
        if (cell instanceof ConveyorBelt) return Color.web("#245C78");
        if (cell instanceof ContaminationSock) return Color.web("#A05045");
        if (cell instanceof CardCell) return Color.web("#764E91");
        if (index % 2 == 1) return Color.web("#2F6FA2");
        return Color.web("#263746");
    }

    private void drawCellNumber(GraphicsContext gc, int index, double x, double y, double tile) {
        String label = String.valueOf(index);
        double size = Math.max(18, tile * 0.31);
        gc.setFont(Font.font("System", FontWeight.BLACK, size));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setLineWidth(Math.max(3, tile * 0.045));
        gc.setStroke(Color.rgb(10, 8, 18, 0.90));
        gc.strokeText(label, x + tile * 0.12, y + tile * 0.08);
        gc.setFill(Color.web("#FFE08A"));
        gc.fillText(label, x + tile * 0.12, y + tile * 0.08);
    }

    private void drawStartFinishBadge(GraphicsContext gc, int index, double x, double y, double tile) {
        if (index != Constants.STARTING_POSITION && index != Constants.WINNING_POSITION) {
            return;
        }
        String label = index == Constants.STARTING_POSITION ? "START" : "FINISH";
        Color color = index == Constants.STARTING_POSITION ? Color.web("#79F1D0") : Color.web("#FFD66E");
        double badgeW = tile * 0.82;
        double badgeH = tile * 0.26;
        gc.setFill(Color.rgb(8, 10, 24, 0.86));
        gc.fillRoundRect(x + (tile - badgeW) * 0.5, y + tile * 0.68, badgeW, badgeH, badgeH * 0.6, badgeH * 0.6);
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x + (tile - badgeW) * 0.5, y + tile * 0.68, badgeW, badgeH, badgeH * 0.6, badgeH * 0.6);
        gc.setFont(Font.font("System", FontWeight.BLACK, Math.max(10, tile * 0.13)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(color);
        gc.fillText(label, x + tile * 0.5, y + tile * 0.81);
    }

    private void drawToken(GraphicsContext gc, TokenView token) {
        if (token == null) {
            return;
        }
        double tile = tileSize(canvas.getWidth(), canvas.getHeight());
        Point2 center = tokenWorldCenter(token, tile);
        double size = Math.min(TOKEN_SIZE, tile * 0.86);
        double ring = size * 0.86;
        double x = center.x + token.laneOffset * tile / BOARD_MAX_TILE;
        double y = center.y - tile * 0.05;

        gc.setFill(Color.rgb(0, 0, 0, 0.42));
        gc.fillOval(x - ring / 2 + 4, y - ring / 2 + 6, ring, ring);
        gc.setFill(Color.rgb(7, 13, 25, 0.90));
        gc.fillOval(x - ring / 2, y - ring / 2, ring, ring);
        gc.setStroke(token.color);
        gc.setLineWidth(Math.max(3, tile * 0.055));
        gc.strokeOval(x - ring / 2, y - ring / 2, ring, ring);

        if (token.image != null) {
            gc.drawImage(token.image, x - size / 2, y - size / 2 - tile * 0.07, size, size);
        } else {
            gc.setFill(token.color);
            gc.fillOval(x - size * 0.24, y - size * 0.32, size * 0.48, size * 0.48);
        }

        gc.setFont(Font.font("System", FontWeight.BOLD, Math.max(10, tile * 0.13)));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        String label = shortMonsterName(token.monster.getName());
        double labelW = Math.max(tile * 0.62, label.length() * tile * 0.085);
        gc.setFill(Color.rgb(5, 8, 17, 0.80));
        gc.fillRoundRect(x - labelW / 2, y + ring * 0.44, labelW, tile * 0.24, 10, 10);
        gc.setFill(token.color);
        gc.fillText(label, x, y + ring * 0.44 + tile * 0.12);
    }

    private void drawCellEffectOverlay(GraphicsContext gc, double w, double h) {
        if (activeEffect == null || activeEffect.effect.frames.isEmpty()) {
            return;
        }

        double dimAlpha = activeEffect.dimAlpha();
        gc.setFill(Color.rgb(0, 0, 0, dimAlpha));
        gc.fillRect(0, 0, w, h);

        EffectFrame frame = activeEffect.currentFrame();
        Point2 center = new Point2(w * 0.5, h * 0.5);
        double target = clamp(Math.min(w, h) * 0.66, 430, 700);
        double scale = target / Math.max(frame.w, frame.h);
        double drawW = frame.w * scale;
        double drawH = frame.h * scale;
        double x = center.x - drawW * 0.5;
        double y = center.y - drawH * 0.5;

        gc.setGlobalAlpha(activeEffect.visualAlpha());
        gc.drawImage(activeEffect.effect.sheet, frame.x, frame.y, frame.w, frame.h, x, y, drawW, drawH);
        gc.setGlobalAlpha(1.0);
    }

    private void drawIntroOverlay(GraphicsContext gc, double w, double h) {
        if (!isIntroPlaying()) {
            return;
        }

        IntroSlide slide = introSequence.currentSlide();
        double fadeIn = clamp(introSequence.elapsed / 0.35, 0, 1);
        double fadeOut = clamp((slide.duration - introSequence.elapsed) / 0.35, 0, 1);
        double alpha = Math.min(fadeIn, fadeOut);

        gc.setGlobalAlpha(alpha);
        gc.setFill(Color.rgb(0, 0, 0, 0.70));
        gc.fillRect(0, 0, w, h);

        double panelW = clamp(w * 0.66, 760, 940);
        double panelH = clamp(h * 0.60, 500, 570);
        double panelX = (w - panelW) * 0.5;
        double panelY = (h - panelH) * 0.5;

        gc.setFill(Color.rgb(8, 13, 29, 0.94));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 30, 30);
        gc.setStroke(Color.web("#FF9F10"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 30, 30);
        gc.setStroke(Color.rgb(103, 232, 249, 0.25));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(panelX + 10, panelY + 10, panelW - 20, panelH - 20, 22, 22);

        double imageBoxX = panelX + 48;
        double imageBoxY = panelY + 100;
        double imageBoxW = panelW * 0.38;
        double imageBoxH = panelH - 150;
        gc.setFill(Color.rgb(0, 0, 0, 0.32));
        gc.fillOval(imageBoxX + imageBoxW * 0.08, imageBoxY + imageBoxH * 0.72,
                imageBoxW * 0.84, imageBoxH * 0.18);
        drawImageFit(gc, slide.image, imageBoxX, imageBoxY, imageBoxW, imageBoxH);

        double textX = panelX + panelW * 0.48;
        double textW = panelW * 0.44;
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);

        gc.setFont(Font.font("System", FontWeight.BLACK, 24));
        gc.setFill(Color.web("#FFB33A"));
        gc.fillText(slide.playerLabel, textX, panelY + 76);

        gc.setFont(Font.font("System", FontWeight.BLACK, 42));
        gc.setFill(Color.web("#FFFFFF"));
        gc.fillText(ellipsize(Font.font("System", FontWeight.BLACK, 42), slide.name, textW), textX, panelY + 118);

        gc.setFont(Font.font("System", FontWeight.BOLD, 18));
        gc.setFill(Color.rgb(103, 232, 249, 0.18));
        gc.fillRoundRect(textX, panelY + 184, 128, 34, 17, 17);
        gc.setStroke(Color.rgb(103, 232, 249, 0.70));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(textX, panelY + 184, 128, 34, 17, 17);
        gc.setFill(Color.web("#BFFBFF"));
        gc.fillText(slide.role, textX + 18, panelY + 190);

        double quoteY = panelY + 260;
        gc.setFill(Color.rgb(0, 0, 0, 0.38));
        gc.fillRoundRect(textX - 20, quoteY - 24, textW + 40, 142, 18, 18);
        gc.setStroke(Color.rgb(255, 159, 16, 0.58));
        gc.setLineWidth(2);
        gc.strokeRoundRect(textX - 20, quoteY - 24, textW + 40, 142, 18, 18);

        Font quoteFont = Font.font("System", FontWeight.BLACK, 28);
        gc.setFont(quoteFont);
        gc.setFill(Color.web("#FFF1D6"));
        List<String> quoteLines = wrapIntroText("\"" + slide.quote + "\"", quoteFont, textW, 3);
        for (int i = 0; i < quoteLines.size(); i++) {
            gc.fillText(quoteLines.get(i), textX, quoteY + i * 34);
        }

        gc.setFont(Font.font("System", FontWeight.BOLD, 16));
        gc.setFill(Color.web("#BFD7FF"));
        gc.fillText("Game starts after both monsters are introduced.", textX, panelY + panelH - 74);
        gc.setGlobalAlpha(1.0);
    }

    private void drawImageFit(GraphicsContext gc, Image image, double x, double y, double w, double h) {
        if (image == null) {
            gc.setFill(Color.web("#67E8F9"));
            gc.fillOval(x + w * 0.25, y + h * 0.20, w * 0.50, w * 0.50);
            return;
        }
        double scale = Math.min(w / image.getWidth(), h / image.getHeight());
        double drawW = image.getWidth() * scale;
        double drawH = image.getHeight() * scale;
        gc.drawImage(image, x + (w - drawW) * 0.5, y + (h - drawH) * 0.5, drawW, drawH);
    }

    private List<String> wrapIntroText(String text, Font font, double maxWidth, int maxLines) {
        List<String> lines = new ArrayList<>();
        String clean = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (clean.isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] words = clean.split(" ");
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String candidate = current.length() == 0 ? words[i] : current + " " + words[i];
            if (textWidth(font, candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            if (current.length() > 0) {
                lines.add(current.toString());
            }
            current.setLength(0);
            current.append(words[i]);
            if (lines.size() == maxLines - 1) {
                lines.add(ellipsize(font, appendRemaining(current.toString(), words, i + 1), maxWidth));
                return lines;
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private void drawInterface(GraphicsContext gc, double w, double h) {
        double leftX = SIDE_MARGIN;
        double rightX = w - RIGHT_PANEL_W - SIDE_MARGIN;
        double top = 16;
        double bottom = h - SIDE_MARGIN;

        drawLogoPanel(gc, leftX, top, LEFT_PANEL_W, 148);
        if (game != null) {
            drawTurnPill(gc, leftX, top + 158, LEFT_PANEL_W, 48);
            drawPlayerPanel(gc, game.getPlayer(), playerToken, leftX, top + 218, LEFT_PANEL_W, 188,
                    "PLAYER 1", Color.web("#1AA9F8"));
            drawPlayerPanel(gc, game.getOpponent(), opponentToken, leftX, top + 418, LEFT_PANEL_W, 188,
                    "PLAYER 2", Color.web("#61D943"));
            drawPowerPanel(gc, leftX, top + 620, LEFT_PANEL_W, 104);
            drawEndTurnButton(gc, leftX, bottom - 58, LEFT_PANEL_W, 48);

            drawEventLog(gc, rightX, top, RIGHT_PANEL_W, 292);
            drawCardsPanel(gc, rightX, top + 312, RIGHT_PANEL_W, 304);
            drawDicePanel(gc, rightX, bottom - 170, RIGHT_PANEL_W, 154);
        }

        drawBottomStatus(gc, w, h);
    }

    private void drawLogoPanel(GraphicsContext gc, double x, double y, double width, double height) {
        gc.setFill(Color.rgb(5, 12, 35, 0.58));
        gc.fillRoundRect(x, y, width, height, 14, 14);
        gc.setStroke(Color.rgb(84, 208, 255, 0.45));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 14, 14);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("System", FontWeight.BLACK, 37));
        gc.setStroke(Color.rgb(4, 9, 24, 0.9));
        gc.setLineWidth(5);
        gc.strokeText("DOOR", x + width * 0.43, y + 56);
        gc.setFill(Color.web("#40C8FF"));
        gc.fillText("DOOR", x + width * 0.43, y + 56);
        gc.setStroke(Color.rgb(4, 9, 24, 0.9));
        gc.strokeText("DASH", x + width * 0.59, y + 94);
        gc.setFill(Color.web("#9DE84B"));
        gc.fillText("DASH", x + width * 0.59, y + 94);
        gc.setFont(Font.font("System", FontWeight.BOLD, 13));
        gc.setFill(Color.web("#DFF7FF"));
        gc.fillText("SCARE VS LAUGH TOUCHDOWN", x + width * 0.5, y + 128);
    }

    private void drawTurnPill(GraphicsContext gc, double x, double y, double width, double height) {
        drawPanel(gc, x, y, width, height, Color.web("#6935A8"), Color.web("#B76DFF"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("System", FontWeight.BLACK, 20));
        gc.setFill(Color.web("#F4E8FF"));
        gc.fillText(status.toUpperCase(Locale.ROOT), x + width * 0.5, y + height * 0.5);
    }

    private void drawPlayerPanel(GraphicsContext gc, Monster monster, TokenView token, double x, double y,
                                 double width, double height, String label, Color accent) {
        drawPanel(gc, x, y, width, height, accent.darker().darker(), accent);
        gc.setFill(Color.rgb(255, 255, 255, 0.08));
        gc.fillRoundRect(x + 10, y + 12, 72, 126, 10, 10);

        if (token != null && token.image != null) {
            gc.drawImage(token.image, x + 5, y + 42, 88, 88);
        }

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("System", FontWeight.BLACK, 18));
        gc.setFill(Color.web("#EAFBFF"));
        gc.fillText(label, x + 92, y + 18);
        gc.setFont(Font.font("System", FontWeight.BOLD, 13));
        gc.setFill(accent.brighter());
        gc.fillText(monster.getRole().name(), x + 92, y + 42);

        gc.setFont(Font.font("System", FontWeight.BLACK, 38));
        gc.setFill(Color.web("#F7FBFF"));
        gc.fillText(String.valueOf(monster.getEnergy()), x + 118, y + 68);
        gc.setFont(Font.font("System", FontWeight.BLACK, 14));
        gc.setFill(Color.web("#FFE08A"));
        gc.fillText("ENERGY", x + 121, y + 108);

        Font infoFont = Font.font("System", FontWeight.BOLD, 11);
        gc.setFont(infoFont);
        double infoX = x + 92;
        double infoW = width - 108;
        gc.setFill(Color.rgb(5, 9, 22, 0.42));
        gc.fillRoundRect(infoX - 6, y + 130, infoW + 12, 38, 8, 8);
        gc.setFill(Color.web("#D9F7FF"));
        gc.fillText(ellipsize(infoFont, "Power: " + powerupDescription(monster), infoW), infoX, y + 133);
        String statusText = activeMonsterStatus(monster);
        gc.setFill(statusText.isEmpty() ? Color.web("#FFE08A") : Color.web("#7DF7C6"));
        gc.fillText(ellipsize(infoFont, statusText.isEmpty() ? latestCardTitle : statusText, infoW), infoX, y + 151);

        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#BFD7FF"));
        gc.fillText(shortMonsterName(monster.getName()) + "  |  Cell " + normalize(monster.getPosition()),
                x + 18, y + height - 28);

        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        gc.setFill(Color.web("#C9DDFF"));
        gc.fillText(ellipsize(Font.font("System", FontWeight.BOLD, 10), latestCardDetail, width - 36),
                x + 18, y + height - 13);
    }

    private void drawPowerPanel(GraphicsContext gc, double x, double y, double width, double height) {
        drawPanel(gc, x, y, width, height, Color.web("#083550"), Color.web("#35D6FF"));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("System", FontWeight.BLACK, 17));
        gc.setFill(Color.web("#BFEFFF"));
        gc.fillText("POWER UPS", x + 16, y + 12);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#FFFFFF"));
        gc.fillText("P  Activate power", x + 18, y + 46);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setFill(Color.web("#BFD7FF"));
        gc.fillText("Cost: " + Constants.POWERUP_COST + " energy", x + 18, y + 68);
    }

    private String powerupDescription(Monster monster) {
        String type = monster.getClass().getSimpleName();
        if ("Dynamo".equals(type)) {
            return "Freeze opponent";
        }
        if ("Dasher".equals(type)) {
            return "Momentum x3 for 3 turns";
        }
        if ("MultiTasker".equals(type)) {
            return "Normal speed for 2 turns";
        }
        if ("Schemer".equals(type)) {
            return "Steal energy from all";
        }
        return "Special monster ability";
    }

    private String activeMonsterStatus(Monster monster) {
        if (monster.isFrozen()) {
            return "Status: Frozen";
        }
        if (monster.isConfused()) {
            return "Status: Confused " + monster.getConfusionTurns() + " turns";
        }
        if (monster.isShielded()) {
            return "Status: Shielded";
        }
        if (monster instanceof Dasher && ((Dasher) monster).getMomentumTurns() > 0) {
            return "Status: Momentum " + ((Dasher) monster).getMomentumTurns();
        }
        if (monster instanceof MultiTasker && ((MultiTasker) monster).getNormalSpeedTurns() > 0) {
            return "Status: Normal speed " + ((MultiTasker) monster).getNormalSpeedTurns();
        }
        return "";
    }

    private void drawEndTurnButton(GraphicsContext gc, double x, double y, double width, double height) {
        drawPanel(gc, x, y, width, height, Color.web("#6F2BA8"), Color.web("#D079FF"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("System", FontWeight.BLACK, 22));
        gc.setFill(Color.web("#F7E8FF"));
        gc.fillText("LOBBY", x + width * 0.5, y + height * 0.52);
    }

    private void drawEventLog(GraphicsContext gc, double x, double y, double width, double height) {
        drawPanel(gc, x, y, width, height, Color.web("#141B3F"), Color.web("#5660AA"));
        drawPanelTitle(gc, "EVENT LOG", x, y, width);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        Font eventFont = Font.font("System", FontWeight.BOLD, 13);
        gc.setFont(eventFont);
        double itemY = y + 52;
        double rowH = 50;
        double rowGap = 9;
        int count = Math.min(4, eventLog.size());
        for (int i = 0; i < count; i++) {
            gc.setFill(Color.rgb(255, 255, 255, i == 0 ? 0.10 : 0.06));
            gc.fillRoundRect(x + 14, itemY - 6, width - 28, rowH, 8, 8);
            gc.setFill(i == 0 ? Color.web("#FFFFFF") : Color.web("#C8D8EF"));
            List<String> lines = wrapEventText(eventLog.get(i), eventFont, width - 48);
            gc.fillText(lines.get(0), x + 24, itemY + 1);
            if (lines.size() > 1) {
                gc.fillText(lines.get(1), x + 24, itemY + 22);
            }
            itemY += rowH + rowGap;
        }
    }

    private void drawCardsPanel(GraphicsContext gc, double x, double y, double width, double height) {
        drawPanel(gc, x, y, width, height, Color.web("#191346"), Color.web("#8E56E8"));
        drawPanelTitle(gc, "YOUR CARDS", x, y, width);
        drawCardRow(gc, x + 14, y + 46, width - 28, "ENERGY STEAL", "Steal 50 or 100 energy", Color.web("#D49A1B"));
        drawCardRow(gc, x + 14, y + 96, width - 28, "SHIELD", "Block next negative effect", Color.web("#1C8CD7"));
        drawCardRow(gc, x + 14, y + 146, width - 28, "SWAPPER", "Swap if behind", Color.web("#8D42D7"));
        drawCardRow(gc, x + 14, y + 196, width - 28, "START OVER", "Send player to start", Color.web("#C64538"));
        drawCardRow(gc, x + 14, y + 246, width - 28, "CONFUSION", "Swap roles for 2 or 3 turns", Color.web("#26C6B8"));
    }

    private void drawCardRow(GraphicsContext gc, double x, double y, double width, String title,
                             String detail, Color accent) {
        gc.setFill(Color.rgb(8, 10, 24, 0.76));
        gc.fillRoundRect(x, y, width, 42, 8, 8);
        gc.setStroke(accent);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, 42, 8, 8);
        gc.setFill(accent);
        gc.fillRoundRect(x + 9, y + 8, 30, 26, 7, 7);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("System", FontWeight.BLACK, 12));
        gc.setFill(Color.web("#FFFFFF"));
        gc.fillText(title, x + 50, y + 6);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));
        gc.setFill(Color.web("#D5E2F8"));
        gc.fillText(detail, x + 50, y + 24);
    }

    private void drawDicePanel(GraphicsContext gc, double x, double y, double width, double height) {
        drawPanel(gc, x, y, width, height, Color.web("#3A1D74"), Color.web("#A566FF"));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("System", FontWeight.BLACK, 23));
        gc.setFill(Color.web("#F5E8FF"));
        gc.fillText("ROLL DICE", x + width * 0.5, y + 16);
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#D8C5FF"));
        gc.fillText("SPACE to roll", x + width * 0.5, y + 48);
        drawDice(gc, x + width * 0.5, y + 108, 62, lastVisualRoll);
    }

    private void drawBottomStatus(GraphicsContext gc, double w, double h) {
        double x = LEFT_PANEL_W + SIDE_MARGIN + PANEL_GAP;
        double width = w - LEFT_PANEL_W - RIGHT_PANEL_W - SIDE_MARGIN * 2 - PANEL_GAP * 2;
        gc.setFill(Color.rgb(3, 6, 18, 0.70));
        gc.fillRoundRect(x, h - 58, width, 42, 12, 12);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(Font.font("System", FontWeight.BOLD, 18));
        gc.setFill(Color.web("#FFE08A"));
        gc.fillText(eventLine, x + width * 0.5, h - 37);
    }

    private void drawPanelTitle(GraphicsContext gc, String title, double x, double y, double width) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("System", FontWeight.BLACK, 20));
        gc.setFill(Color.web("#F2E8FF"));
        gc.fillText(title, x + width * 0.5, y + 16);
    }

    private void drawPanel(GraphicsContext gc, double x, double y, double width, double height,
                           Color base, Color border) {
        gc.setFill(Color.rgb(0, 0, 0, 0.28));
        gc.fillRoundRect(x + 4, y + 5, width, height, 13, 13);
        gc.setFill(Color.rgb(
                (int) (base.getRed() * 255),
                (int) (base.getGreen() * 255),
                (int) (base.getBlue() * 255),
                0.84));
        gc.fillRoundRect(x, y, width, height, 13, 13);
        gc.setStroke(border);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 13, 13);
    }

    private void drawDice(GraphicsContext gc, double cx, double cy, double size, int value) {
        gc.setFill(Color.rgb(0, 0, 0, 0.30));
        gc.fillRoundRect(cx - size / 2 + 5, cy - size / 2 + 6, size, size, 12, 12);
        gc.setFill(Color.web("#F4F0E8"));
        gc.fillRoundRect(cx - size / 2, cy - size / 2, size, size, 12, 12);
        gc.setStroke(Color.web("#C9C2B8"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(cx - size / 2, cy - size / 2, size, size, 12, 12);
        gc.setFill(Color.web("#4E3A88"));
        double p = size * 0.11;
        double[][] spots = diceSpots(value);
        for (double[] spot : spots) {
            gc.fillOval(cx + spot[0] * size - p, cy + spot[1] * size - p, p * 2, p * 2);
        }
    }

    private double[][] diceSpots(int value) {
        switch (Math.max(1, Math.min(6, value))) {
            case 2:
                return new double[][]{{-0.20, -0.20}, {0.20, 0.20}};
            case 3:
                return new double[][]{{-0.22, -0.22}, {0, 0}, {0.22, 0.22}};
            case 4:
                return new double[][]{{-0.22, -0.22}, {0.22, -0.22}, {-0.22, 0.22}, {0.22, 0.22}};
            case 5:
                return new double[][]{{-0.23, -0.23}, {0.23, -0.23}, {0, 0}, {-0.23, 0.23}, {0.23, 0.23}};
            case 6:
                return new double[][]{{-0.24, -0.25}, {0.24, -0.25}, {-0.24, 0}, {0.24, 0}, {-0.24, 0.25}, {0.24, 0.25}};
            case 1:
            default:
                return new double[][]{{0, 0}};
        }
    }

    private String shortMonsterName(String name) {
        if (name.contains("Sullivan")) {
            return "Sulley";
        }
        int space = name.indexOf(' ');
        return space > 0 ? name.substring(0, space) : name;
    }

    private Point2 tokenWorldCenter(TokenView token, double tile) {
        Point2 from = cellCenter(token.previousCell, tile);
        Point2 to = cellCenter(token.cellIndex, tile);
        if (!token.moving && token.progress >= 1.0) {
            return to;
        }
        double t = easeInOut(clamp(token.progress, 0, 1));
        double x = lerp(from.x, to.x, t);
        double y = lerp(from.y, to.y, t);
        double distance = Math.hypot(to.x - from.x, to.y - from.y);
        double arc = Math.sin(t * Math.PI) * Math.min(tile * 1.25, tile * 0.18 + distance * 0.14);
        return new Point2(x, y - arc);
    }

    private Point2 cellCenter(int index, double tile) {
        Point2 topLeft = cellTopLeft(index, boardX(tile), boardY(tile), tile);
        return new Point2(topLeft.x + tile * 0.5, topLeft.y + tile * 0.5);
    }

    private Point2 cellTopLeft(int index, double x0, double y0, double tile) {
        int normalized = normalize(index);
        int row = normalized / Constants.BOARD_COLS;
        int logicalCol = normalized % Constants.BOARD_COLS;
        int col = row % 2 == 0 ? logicalCol : Constants.BOARD_COLS - 1 - logicalCol;
        return new Point2(x0 + col * tile, y0 + (Constants.BOARD_ROWS - 1 - row) * tile);
    }

    private double tileSize(double w, double h) {
        double boardLane = w - LEFT_PANEL_W - RIGHT_PANEL_W - SIDE_MARGIN * 2 - PANEL_GAP * 2 - 56;
        double byWidth = Math.max(BOARD_MIN_TILE, boardLane / Constants.BOARD_COLS);
        double byHeight = Math.max(BOARD_MIN_TILE, (h - HUD_TOP - HUD_BOTTOM - 46) / Constants.BOARD_ROWS);
        return clamp(Math.min(byWidth, byHeight), BOARD_MIN_TILE, BOARD_MAX_TILE);
    }

    private double boardX(double tile) {
        double laneX = LEFT_PANEL_W + SIDE_MARGIN + PANEL_GAP + 28;
        double laneW = canvas.getWidth() - LEFT_PANEL_W - RIGHT_PANEL_W - SIDE_MARGIN * 2 - PANEL_GAP * 2 - 56;
        return laneX + (laneW - tile * Constants.BOARD_COLS) * 0.5;
    }

    private double boardY(double tile) {
        double available = canvas.getHeight() - HUD_TOP - HUD_BOTTOM - tile * Constants.BOARD_ROWS;
        return HUD_TOP + Math.max(8, available * 0.48);
    }

    private Cell engineCellAt(int index) {
        Cell[][] cells = game.getBoard().getBoardCells();
        int normalized = normalize(index);
        int row = normalized / Constants.BOARD_COLS;
        int col = normalized % Constants.BOARD_COLS;
        if (row % 2 == 1) {
            col = Constants.BOARD_COLS - 1 - col;
        }
        return cells[row][col];
    }

    private int normalize(int index) {
        int normalized = index % Constants.BOARD_SIZE;
        return normalized < 0 ? normalized + Constants.BOARD_SIZE : normalized;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double easeInOut(double t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) * 0.5;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private List<String> wrapEventText(String text, Font font, double maxWidth) {
        List<String> lines = new ArrayList<>();
        String clean = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (clean.isEmpty()) {
            lines.add("");
            return lines;
        }
        String[] words = clean.split(" ");
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (textWidth(font, candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
            } else {
                lines.add(current.length() == 0 ? ellipsize(font, word, maxWidth) : current.toString());
                current.setLength(0);
                current.append(word);
                if (lines.size() == 2) {
                    lines.set(1, ellipsize(font, appendRemaining(lines.get(1), words, i), maxWidth));
                    return lines;
                }
            }
        }

        if (current.length() > 0) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        if (lines.size() > 2) {
            return List.of(lines.get(0), ellipsize(font, lines.get(1), maxWidth));
        }
        return lines;
    }

    private String appendRemaining(String start, String[] words, int nextIndex) {
        StringBuilder text = new StringBuilder(start);
        for (int i = nextIndex; i < words.length; i++) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(words[i]);
        }
        return text.toString();
    }

    private String ellipsize(Font font, String text, double maxWidth) {
        String clean = text == null ? "" : text.trim();
        if (textWidth(font, clean) <= maxWidth) {
            return clean;
        }
        String suffix = "...";
        int end = clean.length();
        while (end > 0 && textWidth(font, clean.substring(0, end).trim() + suffix) > maxWidth) {
            end--;
        }
        return clean.substring(0, Math.max(0, end)).trim() + suffix;
    }

    private double textWidth(Font font, String text) {
        Text probe = new Text(text == null ? "" : text);
        probe.setFont(font);
        return probe.getLayoutBounds().getWidth();
    }

    private static void addJavaFxRuntimeArgs(List<String> cmd, String classPath) {
        String modulePath = javaFxJarsFrom(System.getProperty("jdk.module.path"));
        if (isBlank(modulePath)) {
            modulePath = javaFxJarsFrom(classPath);
        }
        if (isBlank(modulePath)) {
            return;
        }
        cmd.add("--module-path");
        cmd.add(modulePath);
        cmd.add("--add-modules");
        cmd.add("javafx.controls,javafx.media");
    }

    private static String javaFxJarsFrom(String classPath) {
        if (isBlank(classPath)) {
            return "";
        }
        return Arrays.stream(classPath.split(File.pathSeparator))
                .filter(path -> new File(path).getName().startsWith("javafx-"))
                .collect(Collectors.joining(File.pathSeparator));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class TurnResolution {
        final int playerAfter;
        final int opponentAfter;
        final String finalEventLine;
        final String finalStatus;
        final TokenView actorToken;
        final int triggeredCell;
        final SpriteSheetEffect effect;

        TurnResolution(int playerAfter, int opponentAfter, String finalEventLine, String finalStatus,
                       TokenView actorToken, int triggeredCell, SpriteSheetEffect effect) {
            this.playerAfter = playerAfter;
            this.opponentAfter = opponentAfter;
            this.finalEventLine = finalEventLine;
            this.finalStatus = finalStatus;
            this.actorToken = actorToken;
            this.triggeredCell = triggeredCell;
            this.effect = effect;
        }
    }

    private static final class ActiveCellEffect {
        final SpriteSheetEffect effect;
        final int cellIndex;
        double elapsed;

        ActiveCellEffect(SpriteSheetEffect effect, int cellIndex) {
            this.effect = effect;
            this.cellIndex = cellIndex;
        }

        boolean isDone() {
            return elapsed >= effect.duration + EFFECT_FADE_SECONDS;
        }

        double dimAlpha() {
            return EFFECT_DIM_ALPHA * visualAlpha();
        }

        double visualAlpha() {
            if (elapsed <= effect.duration) {
                return 1.0;
            }
            double fade = (elapsed - effect.duration) / EFFECT_FADE_SECONDS;
            return clamp(1.0 - fade, 0, 1);
        }

        EffectFrame currentFrame() {
            double frameTime = Math.min(elapsed, Math.max(0, effect.duration - 0.001));
            double cursor = 0;
            for (EffectFrame frame : effect.frames) {
                cursor += frame.duration;
                if (frameTime <= cursor) {
                    return frame;
                }
            }
            return effect.frames.get(effect.frames.size() - 1);
        }
    }

    private static final class SpriteSheetEffect {
        final String label;
        final Image sheet;
        final List<EffectFrame> frames;
        final double duration;

        SpriteSheetEffect(String label, Image sheet, List<EffectFrame> frames) {
            this.label = label;
            this.sheet = sheet;
            this.frames = frames;
            double total = 0;
            for (EffectFrame frame : frames) {
                total += frame.duration;
            }
            this.duration = total;
        }
    }

    private static final class EffectFrame {
        final double x;
        final double y;
        final double w;
        final double h;
        final double duration;

        EffectFrame(double x, double y, double w, double h, double duration) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.duration = duration;
        }
    }

    private static final class IntroSequence {
        final IntroSlide[] slides;
        int index;
        double elapsed;
        boolean active = true;

        IntroSequence(IntroSlide[] slides) {
            this.slides = slides;
        }

        IntroSlide currentSlide() {
            return slides[Math.max(0, Math.min(index, slides.length - 1))];
        }
    }

    private static final class IntroSlide {
        final String playerLabel;
        final String name;
        final String role;
        final String quote;
        final Image image;
        final String audioPath;
        final double duration;

        IntroSlide(String playerLabel, String name, String role, String quote,
                   Image image, String audioPath, double duration) {
            this.playerLabel = playerLabel;
            this.name = name;
            this.role = role;
            this.quote = quote;
            this.image = image;
            this.audioPath = audioPath;
            this.duration = Math.max(1.2, duration);
        }
    }

    private static final class TokenView {
        Monster monster;
        Image image;
        Color color;
        int previousCell;
        int cellIndex;
        double laneOffset;
        double progress = 1.0;
        boolean moving;
    }

    private static final class Point2 {
        final double x;
        final double y;

        Point2(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
