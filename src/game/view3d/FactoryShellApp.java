package game.view3d;

import game.engine.Constants;
import game.engine.Board;
import game.engine.Game;
import game.engine.Role;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.cells.TransportCell;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Dasher;
import game.engine.monsters.Monster;
import game.engine.monsters.MultiTasker;

import com.jme3.anim.AnimComposer;
import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.shader.VarType;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class FactoryShellApp extends SimpleApplication {

    private static final int ROOM_MODULES = 4;
    private static final float MODULE_SIZE = 8f;
    private static final float ROOM_SIZE = ROOM_MODULES * MODULE_SIZE;
    private static final float ROOM_HALF = ROOM_SIZE * 0.5f;
    private static final float WALL_HEIGHT = 7.2f;
    private static final float WALL_THICKNESS = 0.42f;

    private static final float FLOOR_MODEL_X = 1.902f;
    private static final float FLOOR_MODEL_Z = 1.906f;
    private static final float BACK_MODEL_X = 1.890f;
    private static final float BACK_MODEL_Y = 1.843f;
    private static final float BACK_MODEL_Z = 0.247f;

    private static final float BOARD_CELL_SIZE = 1.5f;
    private static final float BOARD_ORIGIN_X = -BOARD_CELL_SIZE * (Constants.BOARD_COLS - 1) * 0.5f;
    private static final float BOARD_ORIGIN_Z = BOARD_CELL_SIZE * (Constants.BOARD_ROWS - 1) * 0.5f;
    private static final float BOARD_CELL_FLOOR_CLEARANCE = 0.04f;
    private static final float CELL_MODEL_FOOTPRINT = 1.906f;
    private static final float BOARD_CELL_SCALE = BOARD_CELL_SIZE / CELL_MODEL_FOOTPRINT;

    private static final String CELL_START = "Models/boardcells/startingcell.glb";
    private static final String CELL_FINISH = "Models/boardcells/finishcell.glb";
    private static final String CELL_NORMAL = "Models/boardcells/basenormalcell.glb";
    private static final String CELL_DOOR = "Models/boardcells/doorcell.glb";
    private static final String CELL_CARD = "Models/boardcells/cardcell.glb";
    private static final String CELL_CONVEYOR = "Models/boardcells/conveyer belt cell.glb";
    private static final String CELL_SOCK = "Models/boardcells/contaminent sockcell.glb";
    private static final String DOOR_PROP = "Models/boardcells/door-prop.glb";
    private static final float DOOR_PROP_WIDTH = BOARD_CELL_SIZE * 0.56f;

    private static final String PLAYER_SCREEN_MODEL = "Models/boardcells/player-screen.glb";
    private static final float SCREEN_TARGET_WIDTH = 4.25f;
    private static final float SCREEN_PLAYER_X = -8.35f;
    private static final float SCREEN_OPPONENT_X = 8.35f;
    private static final float SCREEN_Y = 4.12f;
    private static final float SCREEN_Z = -8.70f;
    private static final float SCREEN_PLAYER_YAW = 11f * FastMath.DEG_TO_RAD;
    private static final float SCREEN_OPPONENT_YAW = -11f * FastMath.DEG_TO_RAD;
    private static final float SCREEN_DISPLAY_WIDTH = 2.72f;
    private static final float SCREEN_DISPLAY_HEIGHT = 4.05f;
    private static final float SCREEN_DISPLAY_CENTER_Y = 0.28f;
    private static final float SCREEN_DISPLAY_FRONT_GAP = 0.06f;
    private static final float CARD_KIOSK_WIDTH = 3.95f;
    private static final float CARD_KIOSK_HEIGHT = 3.18f;
    private static final float CARD_KIOSK_Y = 4.05f;
    private static final float CARD_KIOSK_Z = -9.20f;
    private static final String DICE_MODEL = "Models/props/dice.glb";
    private static final float DICE_TARGET_SIZE = 0.72f;
    private static final float DICE_HOME_X = 0f;
    private static final float DICE_HOME_Y = 3.28f;
    private static final float DICE_HOME_Z = -7.80f;
    private static final float DICE_FINAL_ROLL_TIME = 1.35f;

    private static final int VISUAL_MONSTER_CELL_COUNT = 4;
    private static final String CHARACTER_CELIA = "Models/characters/celia-walk.glb";
    private static final String CHARACTER_FUNGUS = "Models/characters/fungus-walk.glb";
    private static final String CHARACTER_RANDALL = "Models/characters/randall-walk.glb";
    private static final String CHARACTER_SULLIVAN = "Models/characters/sullivan-walk.glb";
    private static final String CHARACTER_YETI = "Models/characters/yeti-walk.glb";
    private static final float TOKEN_TARGET_HEIGHT = 0.65f;
    private static final float TOKEN_SOURCE_HEIGHT = 1.64f;
    private static final float TOKEN_MAX_VISUAL_SCALE = 0.5f;
    private static final float TOKEN_VISUAL_SCALE = Math.min(TOKEN_TARGET_HEIGHT / TOKEN_SOURCE_HEIGHT,
            TOKEN_MAX_VISUAL_SCALE);
    private static final float TOKEN_STEP_TIME = 0.42f;
    private static final float TOKEN_SPECIAL_MOVE_TIME = 0.50f;
    private static final float TOKEN_SPECIAL_MOVE_HEIGHT = 0.55f;
    private static final int TOKEN_MAX_WALK_STEPS = 18;
    private static final float TOKEN_DOOR_OFFSET = BOARD_CELL_SIZE * 0.30f;
    private static final float TOKEN_LANE_OFFSET = BOARD_CELL_SIZE * 0.17f;
    private static final float TOKEN_FORWARD_YAW_OFFSET = 0f;
    private static final float HUD_MARGIN = 18f;
    private static final float HUD_BOTTOM_HEIGHT = 86f;
    private static final int HUD_BOTTOM_EVENT_COUNT = 3;
    private static final String[] MONSTER_CELL_MODELS = {
            "Models/boardcells/monstercells/Mike Wazowski Cell.glb",
            "Models/boardcells/monstercells/Randall Boggs Cell.glb",
            "Models/boardcells/monstercells/Sulley (James P. Sullivan) Cell.glb",
            "Models/boardcells/monstercells/celia.glb",
            "Models/boardcells/monstercells/fungus.glb",
            "Models/boardcells/monstercells/yeti.glb"
    };

    private static final float CAMERA_WIDE_FOV = 48f;
    private static final float CAMERA_FOCUS_FOV = 37f;
    private static final float CAMERA_INTRO_ZOOM_TIME = 1.35f;
    private static final float CAMERA_INTRO_ORBIT_TIME = 4.00f;
    private static final float CAMERA_WIDE_TIME = 1.10f;
    private static final float CAMERA_LANDING_TIME = 0.58f;
    private static final float CAMERA_DICE_RESULT_TIME = 1.55f;
    private static final float CAMERA_RESULT_TIME = 0.95f;
    private static final float CAMERA_FOLLOW_SMOOTHING = 4.4f;

    private enum CameraMode {
        ROLE_SELECTION_VIEW,
        INTRO_PLAYERS,
        INTRO_360,
        WIDE_BOARD,
        FOLLOW_MOVE,
        LANDING_FOCUS,
        DICE_RESULT_CLOSE,
        RESULT_WIDE
    }
    private enum AppMode { ROLE_SELECTION, BOARD }

    private static final class MonsterToken {
        private Monster monster;
        private Node node;
        private Spatial visual;
        private AnimComposer animComposer;
        private String walkClip;
        private int cellIndex = Constants.STARTING_POSITION;
        private int finalCellIndex = Constants.STARTING_POSITION;
        private int pendingSteps;
        private int stepDirection = 1;
        private float stepTimer;
        private final Vector3f stepStart = new Vector3f();
        private final Vector3f stepEnd = new Vector3f();
        private final Vector3f lastDirection = Vector3f.UNIT_X.clone();
        private float laneOffset;
        private boolean moving;
        private boolean specialMove;
    }

    private static final class HudEvent {
        private final String title;
        private final String detail;
        private final ColorRGBA accent;

        private HudEvent(String title, String detail, ColorRGBA accent) {
            this.title = title;
            this.detail = detail;
            this.accent = accent;
        }
    }

    private static final class MonsterSnapshot {
        private final String name;
        private final Role role;
        private final int energy;
        private final int position;
        private final boolean frozen;
        private final boolean shielded;
        private final int confusionTurns;

        private MonsterSnapshot(Monster monster) {
            name = monster.getName();
            role = monster.getRole();
            energy = monster.getEnergy();
            position = monster.getPosition();
            frozen = monster.isFrozen();
            shielded = monster.isShielded();
            confusionTurns = monster.getConfusionTurns();
        }
    }

    private final Vector3f cameraTarget = new Vector3f(0f, 0.85f, 0f);
    private final Vector3f cameraStartEye = new Vector3f();
    private final Vector3f cameraEndEye = new Vector3f();
    private final Vector3f cameraStartTarget = new Vector3f();
    private final Vector3f cameraEndTarget = new Vector3f();
    private final Vector3f introOrbitTarget = new Vector3f();
    private final Vector3f followCameraEye = new Vector3f();
    private final List<String> visibleMonsterCellModels = new ArrayList<>();
    private final List<MonsterToken> activeTokens = new ArrayList<>();
    private AppMode appMode = AppMode.ROLE_SELECTION;
    private CameraMode cameraMode = CameraMode.ROLE_SELECTION_VIEW;
    private float cameraTimer;
    private float cameraDuration;
    private float cameraStartFov = 43f;
    private float cameraEndFov = 43f;
    private float cameraCurrentFov = 43f;
    private float boardTopY = BOARD_CELL_FLOOR_CLEARANCE;
    private Game game;
    private Node boardRoot;
    private Node screenRoot;
    private Node playerScreenContent;
    private Node opponentScreenContent;
    private Node cardKioskContent;
    private Node diceNode;
    private Node roleMenuGui = new Node("role_selection_menu");
    private Node gameplayHudGui = new Node("gameplay_hud");
    private final List<HudEvent> eventLog = new ArrayList<>();
    private BitmapText statusText;
    private float statusTimer;
    private MonsterToken playerToken;
    private MonsterToken opponentToken;
    private MonsterToken movingCameraToken;
    private boolean diceRolling;
    private boolean diceFinalThrow;
    private float diceTimer;
    private int lastVisualRoll = 1;
    private String lastRollText = "Space to roll";
    private String lastCardHighlight;

    public static void main(String[] args) {
        FactoryShellApp app = new FactoryShellApp();
        app.setSettings(buildSettings());
        app.setShowSettings(false);
        app.start();
    }

    private static AppSettings buildSettings() {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("DooR DasH - Factory Shell");
        settings.setWidth(1600);
        settings.setHeight(900);
        settings.setSamples(8);
        settings.setVSync(true);
        settings.setFrameRate(60);
        settings.setGammaCorrection(true);
        settings.setDepthBits(24);
        settings.setRenderer(AppSettings.LWJGL_OPENGL41);
        return settings;
    }

    @Override
    public void simpleInitApp() {
        setDisplayFps(false);
        setDisplayStatView(false);
        flyCam.setEnabled(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.045f, 0.047f, 0.055f, 1f));

        loadFactoryShell();
        setupLights();
        setupInput();
        showRoleSelectionMenu(null);
        setImmediateCamera(CameraMode.ROLE_SELECTION_VIEW,
                new Vector3f(0f, 8.2f, 15.0f),
                new Vector3f(0f, 1.05f, -1.6f),
                46f);
    }

    private void showRoleSelectionMenu(String error) {
        roleMenuGui.detachAllChildren();
        if (roleMenuGui.getParent() == null) {
            guiNode.attachChild(roleMenuGui);
        }

        float width = cam.getWidth();
        float height = cam.getHeight();
        float panelWidth = 560f;
        float panelHeight = error == null ? 330f : 380f;
        float panelX = (width - panelWidth) * 0.5f;
        float panelY = (height - panelHeight) * 0.5f;

        roleMenuGui.attachChild(guiQuad("role_menu_backdrop", 0f, 0f, width, height,
                colorMaterial(new ColorRGBA(0.01f, 0.012f, 0.025f, 0.56f)), 0f));
        roleMenuGui.attachChild(guiQuad("role_menu_panel", panelX, panelY, panelWidth, panelHeight,
                colorMaterial(new ColorRGBA(0.03f, 0.04f, 0.095f, 0.92f)), 1f));
        roleMenuGui.attachChild(guiQuad("role_menu_accent_left", panelX, panelY, 8f, panelHeight,
                colorMaterial(new ColorRGBA(0.04f, 0.85f, 0.78f, 0.9f)), 2f));
        roleMenuGui.attachChild(guiQuad("role_menu_accent_top", panelX + 8f, panelY + panelHeight - 8f,
                panelWidth - 8f, 8f, colorMaterial(new ColorRGBA(0.78f, 0.28f, 0.94f, 0.9f)), 2f));

        roleMenuGui.attachChild(guiText("Choose Your Side", panelX + 96f, panelY + panelHeight - 84f,
                34f, new ColorRGBA(0.96f, 0.97f, 1f, 1f)));
        roleMenuGui.attachChild(guiText("Press S for SCARER", panelX + 116f, panelY + panelHeight - 158f,
                28f, new ColorRGBA(1f, 0.63f, 0.42f, 1f)));
        roleMenuGui.attachChild(guiText("Press L for LAUGHER", panelX + 116f, panelY + panelHeight - 214f,
                28f, new ColorRGBA(0.56f, 0.9f, 1f, 1f)));
        roleMenuGui.attachChild(guiText("A random monster will be assigned, then the board starts.",
                panelX + 58f, panelY + 74f, 18f, new ColorRGBA(0.76f, 0.78f, 0.86f, 1f)));

        if (error != null) {
            roleMenuGui.attachChild(guiText(error, panelX + 58f, panelY + 38f,
                    17f, new ColorRGBA(1f, 0.42f, 0.42f, 1f)));
        }
    }

    private void startBoardGame(Role selectedRole) {
        try {
            game = new Game(selectedRole);
        } catch (IOException e) {
            showRoleSelectionMenu("Could not load CSV game data: " + e.getMessage());
            return;
        }

        appMode = AppMode.BOARD;
        guiNode.detachChild(roleMenuGui);
        buildVisibleMonsterCellModels();
        loadBoardFootprint();
        placeActiveMonsterTokens();
        eventLog.clear();
        lastRollText = "Space to roll";
        lastCardHighlight = null;
        attachWallScreens();
        refreshInfoPanels();
        addHudEvent("GAME START", String.format("You are %s vs %s",
                game.getPlayer().getName(), game.getOpponent().getName()),
                new ColorRGBA(0.08f, 0.88f, 0.78f, 1f));
        startIntroCameraSequence();
    }

    private void handleUsePowerup() {
        if (appMode != AppMode.BOARD || game == null || hasMovingToken()) {
            return;
        }
        Monster current = game.getCurrent();
        try {
            game.usePowerup();
            refreshInfoPanels();
            addHudEvent("POWERUP", current.getName() + " used powerup",
                    new ColorRGBA(0.76f, 0.28f, 0.95f, 1f));
            System.out.println("[Powerup] " + current.getName() + " used powerup");
        } catch (OutOfEnergyException e) {
            addHudEvent("NO ENERGY", e.getMessage(), new ColorRGBA(1f, 0.34f, 0.34f, 1f));
            System.out.println("[Powerup] " + e.getMessage());
        }
    }

    private void showStatusMessage(String message, float seconds) {
        if (statusText == null) {
            statusText = guiText("", 26f, HUD_MARGIN + HUD_BOTTOM_HEIGHT + 34f, 22f,
                    new ColorRGBA(0.95f, 0.96f, 1f, 1f));
        }
        statusText.setText(message);
        statusTimer = seconds;
        if (statusText.getParent() == null) {
            guiNode.attachChild(statusText);
        }
    }

    private void ensureGameplayHud() {
        if (gameplayHudGui.getParent() == null) {
            guiNode.attachChild(gameplayHudGui);
        }
        refreshGameplayHud();
    }

    private void addHudEvent(String title, String detail, ColorRGBA accent) {
        eventLog.add(0, new HudEvent(title, detail, accent));
        while (eventLog.size() > 12) {
            eventLog.remove(eventLog.size() - 1);
        }
        if (appMode == AppMode.BOARD) {
            refreshWallScreens();
            ensureGameplayHud();
        }
    }

    private void refreshGameplayHud() {
        gameplayHudGui.detachAllChildren();
        if (appMode != AppMode.BOARD || game == null) {
            return;
        }

        float screenW = cam.getWidth();
        float x = HUD_MARGIN;
        float y = HUD_MARGIN;
        float width = screenW - HUD_MARGIN * 2f;
        float height = HUD_BOTTOM_HEIGHT;
        ColorRGBA accent = game.getCurrent() == game.getPlayer()
                ? new ColorRGBA(0.08f, 0.88f, 0.78f, 1f)
                : new ColorRGBA(1f, 0.63f, 0.42f, 1f);

        gameplayHudGui.attachChild(guiQuad("bottom_hud_bg", x, y, width, height,
                colorMaterial(new ColorRGBA(0.018f, 0.023f, 0.052f, 0.88f)), 0.50f));
        gameplayHudGui.attachChild(guiQuad("bottom_hud_inner", x + 3f, y + 3f, width - 6f, height - 6f,
                colorMaterial(new ColorRGBA(0.042f, 0.052f, 0.095f, 0.90f)), 0.55f));
        gameplayHudGui.attachChild(guiQuad("bottom_hud_topline", x, y + height - 3f, width, 3f,
                colorMaterial(accent), 0.70f));

        float turnW = 188f;
        gameplayHudGui.attachChild(guiQuad("turn_chip_bg", x + 14f, y + 16f, turnW, height - 30f,
                colorMaterial(new ColorRGBA(accent.r * 0.16f, accent.g * 0.16f, accent.b * 0.18f, 0.96f)),
                0.72f));
        gameplayHudGui.attachChild(guiQuad("turn_chip_bar", x + 14f, y + 16f, 4f, height - 30f,
                colorMaterial(accent), 0.76f));
        gameplayHudGui.attachChild(guiText("CURRENT TURN", x + 30f, y + 61f, 12f,
                new ColorRGBA(0.68f, 0.74f, 0.88f, 1f)));
        gameplayHudGui.attachChild(guiText(shortText(game.getCurrent().getName(), 16), x + 30f, y + 36f, 20f,
                new ColorRGBA(0.98f, 0.98f, 1f, 1f)));

        float actionW = 260f;
        float actionX = x + width - actionW - 14f;
        gameplayHudGui.attachChild(guiQuad("roll_action_bg", actionX, y + 16f, actionW, height - 30f,
                colorMaterial(new ColorRGBA(0.16f, 0.09f, 0.28f, 0.96f)), 0.72f));
        gameplayHudGui.attachChild(guiQuad("roll_action_glow", actionX, y + height - 17f, actionW, 3f,
                colorMaterial(new ColorRGBA(0.82f, 0.54f, 1f, 1f)), 0.80f));
        gameplayHudGui.attachChild(guiText("[SPACE] ROLL", actionX + 26f, y + 57f, 20f,
                new ColorRGBA(0.98f, 0.96f, 1f, 1f)));
        gameplayHudGui.attachChild(guiText("P  POWERUP     R  CAMERA", actionX + 26f, y + 31f, 12f,
                new ColorRGBA(0.66f, 0.70f, 0.86f, 1f)));

        float eventX = x + turnW + 34f;
        float eventW = actionX - eventX - 18f;
        gameplayHudGui.attachChild(guiText(shortText(lastRollText, 96), eventX, y + 66f, 15f,
                new ColorRGBA(0.91f, 0.94f, 1f, 1f)));

        float rowY = y + 19f;
        float rowGap = 8f;
        float rowW = (eventW - rowGap * (HUD_BOTTOM_EVENT_COUNT - 1)) / HUD_BOTTOM_EVENT_COUNT;
        for (int i = 0; i < HUD_BOTTOM_EVENT_COUNT; i++) {
            HudEvent event = i < eventLog.size() ? eventLog.get(i) : null;
            float rowX = eventX + i * (rowW + rowGap);
            ColorRGBA rowAccent = event == null
                    ? new ColorRGBA(0.12f, 0.15f, 0.22f, 1f)
                    : event.accent;
            gameplayHudGui.attachChild(guiQuad("bottom_event_" + i, rowX, rowY, rowW, 32f,
                    colorMaterial(new ColorRGBA(0.055f, 0.066f, 0.12f, 0.86f)), 0.72f));
            gameplayHudGui.attachChild(guiQuad("bottom_event_bar_" + i, rowX, rowY, 3f, 32f,
                    colorMaterial(rowAccent), 0.78f));
            if (event != null) {
                gameplayHudGui.attachChild(guiText(shortText(event.title, 18), rowX + 12f, rowY + 23f, 11f,
                        new ColorRGBA(0.96f, 0.96f, 1f, 1f)));
                gameplayHudGui.attachChild(guiText(shortText(event.detail, 30), rowX + 12f, rowY + 9f, 10f,
                        new ColorRGBA(0.68f, 0.74f, 0.88f, 1f)));
            }
        }
    }


    private String shortText(String text, int max) {
        if (text == null || text.length() <= max) {
            return text;
        }
        return text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private void buildVisibleMonsterCellModels() {
        visibleMonsterCellModels.clear();
        String playerName = game.getPlayer().getName();
        String opponentName = game.getOpponent().getName();
        for (String model : MONSTER_CELL_MODELS) {
            String key = monsterModelKey(model);
            if (matchesMonsterModel(playerName, key) || matchesMonsterModel(opponentName, key)) {
                continue;
            }
            visibleMonsterCellModels.add(model);
            if (visibleMonsterCellModels.size() == VISUAL_MONSTER_CELL_COUNT) {
                return;
            }
        }
    }

    private boolean matchesMonsterModel(String monsterName, String modelKey) {
        String name = monsterName.toLowerCase();
        if ("sulley".equals(modelKey)) {
            return name.contains("sullivan") || name.contains("sulley");
        }
        return name.contains(modelKey);
    }

    private String monsterModelKey(String modelPath) {
        String file = modelPath.substring(modelPath.lastIndexOf('/') + 1).toLowerCase();
        int paren = file.indexOf(' ');
        if (paren > 0) {
            file = file.substring(0, paren);
        }
        int dot = file.indexOf('.');
        return dot > 0 ? file.substring(0, dot) : file;
    }

    private void loadFactoryShell() {
        attachFloor();
        attachBackWall();
        attachFrontWall();
        attachLeftWall();
        attachRightWall();
    }

    private void attachFloor() {
        Spatial template = loadFactoryModel("Models/factory/floor.glb");
        Quaternion floorRotation = new Quaternion().fromAngles(-FastMath.HALF_PI, 0f, 0f);
        float start = -ROOM_HALF + MODULE_SIZE * 0.5f;

        for (int row = 0; row < ROOM_MODULES; row++) {
            for (int col = 0; col < ROOM_MODULES; col++) {
                Spatial floor = template.clone(false);
                floor.setLocalRotation(floorRotation);
                floor.setLocalScale(MODULE_SIZE / FLOOR_MODEL_X, MODULE_SIZE / FLOOR_MODEL_Z, 1f);
                floor.setLocalTranslation(start + col * MODULE_SIZE, -0.015f, start + row * MODULE_SIZE);
                rootNode.attachChild(floor);
            }
        }
    }

    private void attachBackWall() {
        Spatial template = loadFactoryModel("Models/factory/backwall.glb");
        float start = -ROOM_HALF + MODULE_SIZE * 0.5f;
        for (int i = 0; i < ROOM_MODULES; i++) {
            Spatial wall = template.clone(false);
            wall.setLocalScale(MODULE_SIZE / BACK_MODEL_X, WALL_HEIGHT / BACK_MODEL_Y, WALL_THICKNESS / BACK_MODEL_Z);
            wall.setLocalTranslation(start + i * MODULE_SIZE, WALL_HEIGHT * 0.5f, -ROOM_HALF);
            rootNode.attachChild(wall);
        }
    }

    private void attachFrontWall() {
        Spatial template = loadFactoryModel("Models/factory/backwall.glb");
        Quaternion frontRotation = new Quaternion().fromAngles(0f, FastMath.PI, 0f);
        float start = -ROOM_HALF + MODULE_SIZE * 0.5f;
        for (int i = 0; i < ROOM_MODULES; i++) {
            Spatial wall = template.clone(false);
            wall.setLocalRotation(frontRotation);
            wall.setLocalScale(MODULE_SIZE / BACK_MODEL_X, WALL_HEIGHT / BACK_MODEL_Y, WALL_THICKNESS / BACK_MODEL_Z);
            wall.setLocalTranslation(start + i * MODULE_SIZE, WALL_HEIGHT * 0.5f, ROOM_HALF);
            rootNode.attachChild(wall);
        }
    }

    private void attachLeftWall() {
        Spatial template = loadFactoryModel("Models/factory/backwall.glb");
        Quaternion leftRotation = new Quaternion().fromAngles(0f, FastMath.HALF_PI, 0f);
        float start = -ROOM_HALF + MODULE_SIZE * 0.5f;
        for (int i = 0; i < ROOM_MODULES; i++) {
            Spatial wall = template.clone(false);
            wall.setLocalRotation(leftRotation);
            wall.setLocalScale(MODULE_SIZE / BACK_MODEL_X, WALL_HEIGHT / BACK_MODEL_Y, WALL_THICKNESS / BACK_MODEL_Z);
            wall.setLocalTranslation(-ROOM_HALF, WALL_HEIGHT * 0.5f, start + i * MODULE_SIZE);
            rootNode.attachChild(wall);
        }
    }

    private void attachRightWall() {
        Spatial template = loadFactoryModel("Models/factory/backwall.glb");
        Quaternion rightRotation = new Quaternion().fromAngles(0f, -FastMath.HALF_PI, 0f);
        float start = -ROOM_HALF + MODULE_SIZE * 0.5f;
        for (int i = 0; i < ROOM_MODULES; i++) {
            Spatial wall = template.clone(false);
            wall.setLocalRotation(rightRotation);
            wall.setLocalScale(MODULE_SIZE / BACK_MODEL_X, WALL_HEIGHT / BACK_MODEL_Y, WALL_THICKNESS / BACK_MODEL_Z);
            wall.setLocalTranslation(ROOM_HALF, WALL_HEIGHT * 0.5f, start + i * MODULE_SIZE);
            rootNode.attachChild(wall);
        }
    }

    private void loadBoardFootprint() {
        if (boardRoot != null) {
            rootNode.detachChild(boardRoot);
        }
        boardRoot = new Node("BoardFootprint");
        Map<String, CellTemplate> templates = loadBoardCellTemplates();
        CellTemplate normalCell = templates.get(CELL_NORMAL);
        if (normalCell != null) {
            boardTopY = BOARD_CELL_FLOOR_CLEARANCE + normalCell.halfHeight * 2f * BOARD_CELL_SCALE;
        }

        for (int index = 0; index < Constants.BOARD_SIZE; index++) {
            String modelPath = modelPathForCell(index);
            CellTemplate template = templates.get(modelPath);
            Spatial cell = template.spatial.clone(false);
            cell.setName(String.format("cell_%03d", index));
            cell.setLocalScale(BOARD_CELL_SCALE);

            Vector3f center = boardCellCenter(index);
            float targetCenterY = BOARD_CELL_FLOOR_CLEARANCE + template.halfHeight * BOARD_CELL_SCALE;
            cell.setLocalTranslation(
                    center.x - template.center.x * BOARD_CELL_SCALE,
                    targetCenterY - template.center.y * BOARD_CELL_SCALE,
                    center.z - template.center.z * BOARD_CELL_SCALE);
            boardRoot.attachChild(cell);
        }

        attachDoorProps(boardRoot, templates.get(CELL_DOOR));
        rootNode.attachChild(boardRoot);
    }

    private void placeActiveMonsterTokens() {
        clearActiveMonsterTokens();
        playerToken = createMonsterToken("player", game.getPlayer(), -TOKEN_LANE_OFFSET,
                new ColorRGBA(0.08f, 0.88f, 0.78f, 1f));
        opponentToken = createMonsterToken("opponent", game.getOpponent(), TOKEN_LANE_OFFSET,
                new ColorRGBA(1f, 0.63f, 0.42f, 1f));
        activeTokens.add(playerToken);
        activeTokens.add(opponentToken);
    }

    private void clearActiveMonsterTokens() {
        for (MonsterToken token : activeTokens) {
            if (token.node != null) {
                rootNode.detachChild(token.node);
            }
        }
        activeTokens.clear();
        playerToken = null;
        opponentToken = null;
    }

    private MonsterToken createMonsterToken(String label, Monster monster, float laneOffset, ColorRGBA fallbackColor) {
        MonsterToken token = new MonsterToken();
        token.monster = monster;
        token.laneOffset = laneOffset;
        token.cellIndex = monster.getPosition();
        token.finalCellIndex = token.cellIndex;
        token.node = new Node(label + "_monster_token");
        token.visual = loadMonsterTokenVisual(monster, fallbackColor);
        token.node.attachChild(token.visual);
        token.node.setLocalTranslation(tokenFootPosition(token, token.cellIndex));
        orientTokenForCell(token, token.cellIndex);
        configureTokenAnimation(token);
        rootNode.attachChild(token.node);
        System.out.printf("[Token] %s placed at cell %d, lane %.2f%n",
                monster.getName(), token.cellIndex, token.laneOffset);
        return token;
    }

    private Spatial loadMonsterTokenVisual(Monster monster, ColorRGBA fallbackColor) {
        String modelPath = characterModelPath(monster);
        if (modelPath != null) {
            try {
                Spatial visual = loadFactoryModel(modelPath);
                visual.setName(monster.getName() + "_visual");
                normalizeTokenVisual(visual, monster.getName());
                return visual;
            } catch (RuntimeException e) {
                System.out.println("[Token] Could not load " + modelPath + ": " + e.getMessage());
            }
        }
        return createPlaceholderTokenVisual(monster, fallbackColor);
    }

    private String characterModelPath(Monster monster) {
        String name = monster.getName().toLowerCase();
        if (name.contains("celia")) {
            return CHARACTER_CELIA;
        }
        if (name.contains("fungus")) {
            return CHARACTER_FUNGUS;
        }
        if (name.contains("randall")) {
            return CHARACTER_RANDALL;
        }
        if (name.contains("sullivan") || name.contains("sulley")) {
            return CHARACTER_SULLIVAN;
        }
        if (name.contains("yeti")) {
            return CHARACTER_YETI;
        }
        return null;
    }

    private void normalizeTokenVisual(Spatial visual, String monsterName) {
        visual.setLocalScale(TOKEN_VISUAL_SCALE);
        visual.setLocalTranslation(0f, 0f, 0f);
        System.out.printf("[TokenScale] %s fixedScale=%.5f targetHeight=%.2f sourceHeight=%.2f%n",
                monsterName, TOKEN_VISUAL_SCALE, TOKEN_TARGET_HEIGHT, TOKEN_SOURCE_HEIGHT);
    }

    private Spatial createPlaceholderTokenVisual(Monster monster, ColorRGBA color) {
        Node placeholder = new Node(monster.getName() + "_placeholder_visual");
        Geometry body = new Geometry(monster.getName() + "_placeholder_body",
                new Box(0.18f, TOKEN_TARGET_HEIGHT * 0.5f, 0.18f));
        body.setMaterial(colorMaterial(color));
        body.setLocalTranslation(0f, TOKEN_TARGET_HEIGHT * 0.5f, 0f);
        placeholder.attachChild(body);
        return placeholder;
    }

    private void configureTokenAnimation(MonsterToken token) {
        token.animComposer = findAnimComposer(token.visual);
        if (token.animComposer != null && !token.animComposer.getAnimClipsNames().isEmpty()) {
            token.walkClip = token.animComposer.getAnimClipsNames().iterator().next();
            token.animComposer.setCurrentAction(token.walkClip);
            token.animComposer.setGlobalSpeed(0f);
            System.out.println("[Token] " + token.monster.getName() + " animation: " + token.walkClip);
        }
    }

    private AnimComposer findAnimComposer(Spatial spatial) {
        AnimComposer composer = spatial.getControl(AnimComposer.class);
        if (composer != null) {
            return composer;
        }
        if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                composer = findAnimComposer(child);
                if (composer != null) {
                    return composer;
                }
            }
        }
        return null;
    }

    private void attachDoorProps(Node boardNode, CellTemplate doorCellTemplate) {
        Spatial template = loadFactoryModel(DOOR_PROP);
        template.updateModelBound();
        if (!(template.getWorldBound() instanceof BoundingBox)) {
            throw new IllegalStateException("Door prop model has no bounding box: " + DOOR_PROP);
        }

        BoundingBox bounds = (BoundingBox) template.getWorldBound();
        float modelWidth = bounds.getXExtent() * 2f;
        float scale = modelWidth > 0f ? DOOR_PROP_WIDTH / modelWidth : 1f;
        float cellTopY = BOARD_CELL_FLOOR_CLEARANCE + doorCellTemplate.halfHeight * 2f * BOARD_CELL_SCALE;

        for (int index = 0; index < Constants.BOARD_SIZE; index++) {
            if (!isVisualDoorCell(index)) {
                continue;
            }

            Spatial door = template.clone(false);
            door.setName(String.format("door_%03d", index));
            door.setLocalScale(scale);

            Vector3f center = boardCellCenter(index);
            Vector3f modelCenter = bounds.getCenter();
            door.setLocalTranslation(
                    center.x - modelCenter.x * scale,
                    cellTopY + bounds.getYExtent() * scale - modelCenter.y * scale + 0.03f,
                    center.z - modelCenter.z * scale);
            boardNode.attachChild(door);
        }
    }

    private void refreshInfoPanels() {
        refreshWallScreens();
        ensureGameplayHud();
    }

    private void attachWallScreens() {
        if (screenRoot != null) {
            rootNode.detachChild(screenRoot);
        }
        screenRoot = new Node("wall_screens");
        rootNode.attachChild(screenRoot);

        playerScreenContent = createWallScreen("player_screen",
                SCREEN_PLAYER_X, SCREEN_PLAYER_YAW);
        opponentScreenContent = createWallScreen("opponent_screen",
                SCREEN_OPPONENT_X, SCREEN_OPPONENT_YAW);
        cardKioskContent = createCardKiosk();
        attachDice();
    }

    private Node createWallScreen(String name, float x, float yaw) {
        Node container = new Node(name);
        container.setLocalRotation(new Quaternion().fromAngles(0f, yaw, 0f));
        container.setLocalTranslation(x, SCREEN_Y, SCREEN_Z);

        Spatial frame = loadFactoryModel(PLAYER_SCREEN_MODEL);
        float displayZ = SCREEN_DISPLAY_FRONT_GAP;
        frame.updateModelBound();
        if (frame.getWorldBound() instanceof BoundingBox) {
            BoundingBox bounds = (BoundingBox) frame.getWorldBound();
            float modelWidth = bounds.getXExtent() * 2f;
            float scale = modelWidth > 0f ? SCREEN_TARGET_WIDTH / modelWidth : 1f;
            frame.setLocalScale(scale);
            Vector3f c = bounds.getCenter();
            frame.setLocalTranslation(-c.x * scale, -c.y * scale, -c.z * scale);
            displayZ = bounds.getZExtent() * scale + SCREEN_DISPLAY_FRONT_GAP;
        }
        container.attachChild(frame);

        Node content = new Node(name + "_content");
        content.setLocalTranslation(
                -SCREEN_DISPLAY_WIDTH * 0.5f,
                SCREEN_DISPLAY_CENTER_Y - SCREEN_DISPLAY_HEIGHT * 0.5f,
                displayZ);
        container.attachChild(content);
        screenRoot.attachChild(container);
        return content;
    }

    private Node createCardKiosk() {
        Node container = new Node("card_effects_kiosk");
        container.setLocalTranslation(
                -CARD_KIOSK_WIDTH * 0.5f,
                CARD_KIOSK_Y - CARD_KIOSK_HEIGHT * 0.5f,
                CARD_KIOSK_Z);
        screenRoot.attachChild(container);
        return container;
    }

    private void attachDice() {
        diceNode = new Node("rolling_dice");
        diceNode.setLocalTranslation(DICE_HOME_X, DICE_HOME_Y, DICE_HOME_Z);

        Spatial dice = loadFactoryModel(DICE_MODEL);
        dice.updateModelBound();
        if (dice.getWorldBound() instanceof BoundingBox) {
            BoundingBox bounds = (BoundingBox) dice.getWorldBound();
            float modelSize = Math.max(bounds.getXExtent() * 2f,
                    Math.max(bounds.getYExtent() * 2f, bounds.getZExtent() * 2f));
            float scale = modelSize > 0f ? DICE_TARGET_SIZE / modelSize : 1f;
            dice.setLocalScale(scale);
            Vector3f c = bounds.getCenter();
            dice.setLocalTranslation(-c.x * scale, -c.y * scale, -c.z * scale);
        }
        diceNode.attachChild(dice);
        screenRoot.attachChild(diceNode);
    }

    private void refreshWallScreens() {
        if (game == null || playerScreenContent == null || opponentScreenContent == null
                || cardKioskContent == null) {
            return;
        }
        populateWallScreen(playerScreenContent, "PLAYER", game.getPlayer(),
                new ColorRGBA(0.08f, 0.88f, 0.78f, 1f));
        populateWallScreen(opponentScreenContent, "OPPONENT", game.getOpponent(),
                new ColorRGBA(1f, 0.63f, 0.42f, 1f));
        populateCardKiosk(cardKioskContent);
    }

    private void populateWallScreen(Node content, String label, Monster monster, ColorRGBA accent) {
        content.detachAllChildren();

        float W = SCREEN_DISPLAY_WIDTH;
        float H = SCREEN_DISPLAY_HEIGHT;
        drawWorldPanelFrame(content, "screen", W, H, accent);

        boolean current = game.getCurrent() == monster;
        if (current) {
            content.attachChild(panelQuad("scr_current_glow", 0.17f, H - 0.34f, W - 0.34f, 0.19f,
                    colorMaterial(new ColorRGBA(accent.r * 0.35f, accent.g * 0.35f, accent.b * 0.40f, 0.92f)),
                    0.024f));
            content.attachChild(panelText("CURRENT TURN", 0.31f, H - 0.20f, 0.105f,
                    new ColorRGBA(0.99f, 0.96f, 1f, 1f), 0.040f));
        }

        float avatarSize = 0.70f;
        float avatarX = 0.24f;
        float avatarY = H - 1.12f;
        content.attachChild(panelQuad("scr_avatar_ring", avatarX - 0.05f, avatarY - 0.05f,
                avatarSize + 0.10f, avatarSize + 0.10f, colorMaterial(accent), 0.026f));
        Material portrait = portraitMaterial(monster);
        content.attachChild(panelQuad("scr_avatar_bg", avatarX, avatarY, avatarSize, avatarSize,
                portrait != null
                        ? portrait
                        : colorMaterial(new ColorRGBA(accent.r * 0.20f, accent.g * 0.20f, accent.b * 0.22f, 0.99f)),
                0.032f));
        String initial = monster.getName() == null || monster.getName().isEmpty()
                ? "?" : monster.getName().substring(0, 1).toUpperCase();
        if (portrait == null) {
            content.attachChild(panelText(initial, avatarX + 0.23f, avatarY + 0.24f, 0.46f,
                    new ColorRGBA(0.97f, 0.98f, 1f, 1f), 0.046f));
        }

        float identityX = avatarX + avatarSize + 0.22f;
        content.attachChild(panelText(label, identityX, H - 0.58f, 0.135f,
                accent, 0.040f));
        content.attachChild(panelText(monster.getRole().name(), identityX, H - 0.86f, 0.165f,
                new ColorRGBA(0.72f, 0.78f, 0.95f, 1f), 0.040f));

        String name = shortText(monster.getName(), 15);
        content.attachChild(panelText(name, 0.24f, H - 1.47f, 0.22f,
                new ColorRGBA(0.98f, 0.98f, 1f, 1f), 0.040f));

        float energyY = 1.54f;
        float energyH = 0.15f;
        float energyW = W - 0.48f;
        float energyX = 0.24f;
        float energyRatio = clamp(monster.getEnergy() / (float) Constants.WINNING_ENERGY, 0f, 1f);
        String energyText = monster.getEnergy() + " / " + Constants.WINNING_ENERGY;
        content.attachChild(panelText("ENERGY", energyX, energyY + 0.34f, 0.115f,
                new ColorRGBA(0.66f, 0.74f, 0.90f, 1f), 0.040f));
        content.attachChild(panelText(energyText, energyX + energyW - 0.88f, energyY + 0.34f, 0.115f,
                new ColorRGBA(0.92f, 0.93f, 1f, 1f), 0.040f));
        content.attachChild(panelQuad("scr_energy_bg", energyX, energyY, energyW, energyH,
                colorMaterial(new ColorRGBA(0.10f, 0.13f, 0.20f, 0.98f)), 0.028f));
        if (monster.getEnergy() > 0) {
            content.attachChild(panelQuad("scr_energy_fill", energyX, energyY,
                    Math.max(0.04f, energyW * energyRatio), energyH,
                    colorMaterial(accent), 0.034f));
        }

        String position = "CELL " + monster.getPosition() + " / " + Constants.WINNING_POSITION;
        content.attachChild(panelText(position, 0.24f, 1.02f, 0.145f,
                new ColorRGBA(0.74f, 0.80f, 0.95f, 1f), 0.040f));

        String statusChip = shortText(statusSummary(monster), 22).toUpperCase();
        content.attachChild(panelQuad("scr_status_chip", 0.20f, 0.42f, W - 0.40f, 0.28f,
                colorMaterial(new ColorRGBA(accent.r * 0.24f, accent.g * 0.24f, accent.b * 0.30f, 0.96f)),
                0.028f));
        content.attachChild(panelText(statusChip, 0.34f, 0.61f, 0.105f,
                new ColorRGBA(0.98f, 0.98f, 1f, 1f), 0.040f));
    }

    private void populateCardKiosk(Node content) {
        content.detachAllChildren();

        float W = CARD_KIOSK_WIDTH;
        float H = CARD_KIOSK_HEIGHT;
        ColorRGBA accent = new ColorRGBA(0.82f, 0.54f, 1f, 1f);
        drawWorldPanelFrame(content, "cards", W, H, accent);
        content.attachChild(panelText("CARD EFFECTS", 0.34f, H - 0.34f, 0.25f,
                new ColorRGBA(0.98f, 0.96f, 1f, 1f), 0.040f));
        content.attachChild(panelText("reference board", W - 1.32f, H - 0.39f, 0.13f,
                new ColorRGBA(0.66f, 0.72f, 0.88f, 1f), 0.040f));

        String[][] cards = cardRows();
        ColorRGBA[] colors = cardColors();
        float rowH = 0.47f;
        float rowGap = 0.08f;
        float startY = H - 0.96f;
        for (int i = 0; i < cards.length; i++) {
            float rowY = startY - i * (rowH + rowGap) - rowH;
            boolean highlighted = cards[i][0].equals(lastCardHighlight);
            ColorRGBA rowAccent = colors[i];
            ColorRGBA bg = highlighted
                    ? new ColorRGBA(rowAccent.r * 0.24f, rowAccent.g * 0.24f, rowAccent.b * 0.24f, 0.98f)
                    : new ColorRGBA(0.055f, 0.065f, 0.12f, 0.94f);

            content.attachChild(panelQuad("card_row_" + i, 0.28f, rowY, W - 0.56f, rowH,
                    colorMaterial(bg), 0.028f));
            content.attachChild(panelQuad("card_row_bar_" + i, 0.28f, rowY, 0.06f, rowH,
                    colorMaterial(rowAccent), 0.034f));
            content.attachChild(panelQuad("card_row_icon_" + i, 0.46f, rowY + 0.10f, 0.28f, 0.28f,
                    colorMaterial(new ColorRGBA(rowAccent.r, rowAccent.g, rowAccent.b, highlighted ? 1f : 0.82f)),
                    0.036f));
            content.attachChild(panelText(cards[i][0], 0.86f, rowY + 0.33f, 0.16f,
                    highlighted ? rowAccent : new ColorRGBA(0.94f, 0.94f, 1f, 1f), 0.044f));
            content.attachChild(panelText(cards[i][1], 0.86f, rowY + 0.13f, 0.12f,
                    new ColorRGBA(0.68f, 0.74f, 0.88f, 1f), 0.044f));
        }
    }

    private void drawWorldPanelFrame(Node content, String prefix, float width, float height, ColorRGBA accent) {
        content.attachChild(panelQuad(prefix + "_shadow", -0.06f, -0.06f, width + 0.12f, height + 0.12f,
                colorMaterial(new ColorRGBA(0f, 0f, 0f, 0.36f)), 0.006f));
        content.attachChild(panelQuad(prefix + "_bg", 0f, 0f, width, height,
                colorMaterial(new ColorRGBA(0.025f, 0.032f, 0.075f, 0.97f)), 0.012f));
        content.attachChild(panelQuad(prefix + "_inner", 0.06f, 0.06f, width - 0.12f, height - 0.12f,
                colorMaterial(new ColorRGBA(0.046f, 0.056f, 0.112f, 0.96f)), 0.018f));
        content.attachChild(panelQuad(prefix + "_top_accent", 0f, height - 0.05f, width, 0.05f,
                colorMaterial(accent), 0.030f));
        content.attachChild(panelQuad(prefix + "_side_accent", 0f, 0f, 0.05f, height,
                colorMaterial(new ColorRGBA(0.04f, 0.88f, 0.82f, 1f)), 0.030f));
    }

    private String[][] cardRows() {
        return new String[][] {
                {"ENERGY STEAL", "Steal 150 energy"},
                {"SHIELD",       "Block next bad effect"},
                {"SWAPPER",      "Swap if behind"},
                {"START OVER",   "Send foe to start"},
                {"CONFUSION",    "Swap roles temporarily"}
        };
    }

    private ColorRGBA[] cardColors() {
        return new ColorRGBA[] {
                new ColorRGBA(0.95f, 0.76f, 0.20f, 1f),
                new ColorRGBA(0.16f, 0.74f, 1f,    1f),
                new ColorRGBA(0.72f, 0.34f, 1f,    1f),
                new ColorRGBA(1f,    0.36f, 0.24f, 1f),
                new ColorRGBA(0.26f, 1f,    0.78f, 1f)
        };
    }

    private Geometry panelQuad(String name, float x, float y, float width, float height,
                               Material material, float z) {
        Geometry quad = new Geometry(name, new Quad(width, height));
        quad.setLocalTranslation(x, y, z);
        quad.setQueueBucket(RenderQueue.Bucket.Transparent);
        quad.setMaterial(material);
        return quad;
    }

    private BitmapText panelText(String text, float x, float baselineY, float size,
                                 ColorRGBA color, float z) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText label = new BitmapText(font, false);
        label.setText(text);
        label.setSize(size);
        label.setColor(color);
        label.setLocalTranslation(x, baselineY, z);
        label.setQueueBucket(RenderQueue.Bucket.Transparent);
        return label;
    }

    private Material portraitMaterial(Monster monster) {
        String portraitPath = portraitPathForMonster(monster);
        if (portraitPath == null) {
            return null;
        }
        try {
            Texture texture = assetManager.loadTexture(portraitPath);
            texture.setMinFilter(Texture.MinFilter.Trilinear);
            texture.setMagFilter(Texture.MagFilter.Bilinear);
            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setTexture("ColorMap", texture);
            material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
            material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
            return material;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String portraitPathForMonster(Monster monster) {
        String name = monster.getName().toLowerCase();
        if (name.contains("yeti")) {
            return "Textures/ui/yeti-portrait.png";
        }
        return null;
    }

    private Material colorMaterial(ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", color);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        return material;
    }

    private String statusSummary(Monster monster) {
        StringBuilder status = new StringBuilder();
        if (monster.isFrozen()) {
            status.append("Frozen");
        }
        if (monster.isShielded()) {
            appendStatus(status, "Shield");
        }
        if (monster.getConfusionTurns() > 0) {
            appendStatus(status, "Conf " + monster.getConfusionTurns());
        }
        if (monster instanceof Dasher && ((Dasher) monster).getMomentumTurns() > 0) {
            appendStatus(status, "Momentum " + ((Dasher) monster).getMomentumTurns());
        }
        if (monster instanceof MultiTasker && ((MultiTasker) monster).getNormalSpeedTurns() > 0) {
            appendStatus(status, "Focus " + ((MultiTasker) monster).getNormalSpeedTurns());
        }
        return status.length() == 0 ? "No effects" : status.toString();
    }

    private void appendStatus(StringBuilder status, String value) {
        if (status.length() > 0) {
            status.append(" | ");
        }
        status.append(value);
    }

    private Geometry guiQuad(String name, float x, float y, float width, float height,
                             Material material, float z) {
        Geometry quad = new Geometry(name, new Quad(width, height));
        quad.setLocalTranslation(x, y, z);
        quad.setQueueBucket(RenderQueue.Bucket.Gui);
        quad.setMaterial(material);
        return quad;
    }

    private BitmapText guiText(String text, float x, float baselineY, float size, ColorRGBA color) {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText label = new BitmapText(font, false);
        label.setText(text);
        label.setSize(size);
        label.setColor(color);
        label.setLocalTranslation(x, baselineY, 3f);
        label.setQueueBucket(RenderQueue.Bucket.Gui);
        return label;
    }

    private Map<String, CellTemplate> loadBoardCellTemplates() {
        Map<String, CellTemplate> templates = new HashMap<>();
        for (int index = 0; index < Constants.BOARD_SIZE; index++) {
            String modelPath = modelPathForCell(index);
            if (!templates.containsKey(modelPath)) {
                templates.put(modelPath, loadBoardCellTemplate(modelPath));
            }
        }
        return templates;
    }

    private CellTemplate loadBoardCellTemplate(String path) {
        Spatial spatial = loadFactoryModel(path);
        spatial.updateModelBound();
        if (!(spatial.getWorldBound() instanceof BoundingBox)) {
            throw new IllegalStateException("Board cell model has no bounding box: " + path);
        }

        BoundingBox bounds = (BoundingBox) spatial.getWorldBound();
        return new CellTemplate(spatial, bounds.getCenter().clone(), bounds.getYExtent());
    }

    private String modelPathForCell(int index) {
        if (index == Constants.STARTING_POSITION) {
            return CELL_START;
        }
        if (index == Constants.WINNING_POSITION) {
            return CELL_FINISH;
        }

        int monsterSlot = monsterSlotForIndex(index);
        if (monsterSlot >= 0) {
            return monsterSlot < visibleMonsterCellModels.size()
                    ? visibleMonsterCellModels.get(monsterSlot)
                    : CELL_NORMAL;
        }
        if (containsIndex(Constants.CONVEYOR_CELL_INDICES, index)) {
            return CELL_CONVEYOR;
        }
        if (containsIndex(Constants.SOCK_CELL_INDICES, index)) {
            return CELL_SOCK;
        }
        if (containsIndex(Constants.CARD_CELL_INDICES, index)) {
            return CELL_CARD;
        }
        if (index % 2 == 1) {
            return CELL_DOOR;
        }
        return CELL_NORMAL;
    }

    private boolean isVisualDoorCell(int index) {
        return index % 2 == 1 && index != Constants.WINNING_POSITION;
    }

    private int monsterSlotForIndex(int index) {
        for (int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
            if (Constants.MONSTER_CELL_INDICES[i] == index) {
                return i;
            }
        }
        return -1;
    }

    private boolean containsIndex(int[] indices, int target) {
        for (int index : indices) {
            if (index == target) {
                return true;
            }
        }
        return false;
    }

    private Vector3f boardCellCenter(int index) {
        int row = index / Constants.BOARD_COLS;
        int logicalCol = index % Constants.BOARD_COLS;
        int col = row % 2 == 0 ? logicalCol : Constants.BOARD_COLS - 1 - logicalCol;

        float x = BOARD_ORIGIN_X + col * BOARD_CELL_SIZE;
        float z = BOARD_ORIGIN_Z - row * BOARD_CELL_SIZE;
        return new Vector3f(x, BOARD_CELL_FLOOR_CLEARANCE, z);
    }

    private Spatial loadFactoryModel(String path) {
        Spatial spatial = assetManager.loadModel(path);
        improveTextureFiltering(spatial);
        return spatial;
    }

    private void improveTextureFiltering(Spatial spatial) {
        if (spatial instanceof Geometry) {
            Material material = ((Geometry) spatial).getMaterial();
            if (material == null) {
                return;
            }
            for (MatParam param : material.getParams()) {
                if (param.getVarType() == VarType.Texture2D && param.getValue() instanceof Texture) {
                    Texture texture = (Texture) param.getValue();
                    texture.setMinFilter(Texture.MinFilter.Trilinear);
                    texture.setMagFilter(Texture.MagFilter.Bilinear);
                    texture.setAnisotropicFilter(16);
                }
            }
        } else if (spatial instanceof Node) {
            for (Spatial child : ((Node) spatial).getChildren()) {
                improveTextureFiltering(child);
            }
        }
    }

    private void setupLights() {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.28f, 0.31f, 0.36f, 1f));
        rootNode.addLight(ambient);

        DirectionalLight overhead = new DirectionalLight();
        overhead.setDirection(new Vector3f(-0.35f, -1f, -0.45f).normalizeLocal());
        overhead.setColor(new ColorRGBA(0.95f, 0.92f, 0.84f, 1f).mult(1.55f));
        rootNode.addLight(overhead);

        DirectionalLight coolFill = new DirectionalLight();
        coolFill.setDirection(new Vector3f(0.65f, -0.45f, 0.35f).normalizeLocal());
        coolFill.setColor(new ColorRGBA(0.28f, 0.42f, 0.62f, 1f).mult(0.75f));
        rootNode.addLight(coolFill);

        PointLight centerGlow = new PointLight();
        centerGlow.setPosition(new Vector3f(0f, 4.8f, 0f));
        centerGlow.setColor(new ColorRGBA(0.26f, 0.8f, 0.62f, 1f).mult(1.15f));
        centerGlow.setRadius(24f);
        rootNode.addLight(centerGlow);
    }

    private void setupInput() {
        inputManager.addMapping("ResetCamera", new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping("RollDice", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("ChooseScarer", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("ChooseLaugher", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("UsePowerup", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("DebugState", new KeyTrigger(KeyInput.KEY_T));

        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if ("ResetCamera".equals(name) && isPressed) {
                startWideBoardCamera(CAMERA_RESULT_TIME);
            } else if ("ChooseScarer".equals(name) && isPressed && appMode == AppMode.ROLE_SELECTION) {
                startBoardGame(Role.SCARER);
            } else if ("ChooseLaugher".equals(name) && isPressed && appMode == AppMode.ROLE_SELECTION) {
                startBoardGame(Role.LAUGHER);
            } else if ("RollDice".equals(name) && isPressed && appMode == AppMode.BOARD) {
                playEngineTurn();
            } else if ("UsePowerup".equals(name) && isPressed && appMode == AppMode.BOARD) {
                handleUsePowerup();
            } else if ("DebugState".equals(name) && isPressed && appMode == AppMode.BOARD) {
                debugTokenState();
            }
        }, "ResetCamera", "RollDice", "ChooseScarer", "ChooseLaugher", "UsePowerup", "DebugState");
    }

    private void resetCamera() {
        startWideBoardCamera(CAMERA_RESULT_TIME);
    }

    private void playEngineTurn() {
        if (game == null || hasMovingToken() || isIntroCameraActive()) {
            return;
        }

        Monster movingMonster = game.getCurrent();
        boolean movingPlayer = movingMonster == game.getPlayer();
        MonsterSnapshot playerSnapshot = new MonsterSnapshot(game.getPlayer());
        MonsterSnapshot opponentSnapshot = new MonsterSnapshot(game.getOpponent());
        int playerBefore = game.getPlayer().getPosition();
        int opponentBefore = game.getOpponent().getPosition();
        int deckBefore = Board.getCards().size();
        try {
            game.playTurn();
            refreshInfoPanels();
            syncTokenToMonster(playerToken, playerBefore);
            syncTokenToMonster(opponentToken, opponentBefore);
            lastRollText = turnStatus(movingMonster, playerBefore, opponentBefore);
            lastVisualRoll = inferVisualDiceRoll(movingPlayer ? playerBefore : opponentBefore,
                    movingMonster.getPosition());
            startDiceRoll(false);
            addTurnHudEvents(movingMonster, movingPlayer, playerSnapshot, opponentSnapshot, deckBefore);
            startTurnCamera(movingPlayer ? playerToken : opponentToken);
            System.out.printf("[Turn] %s: player %d->%d, opponent %d->%d%n",
                    movingMonster.getName(),
                    playerBefore, game.getPlayer().getPosition(),
                    opponentBefore, game.getOpponent().getPosition());
        } catch (InvalidMoveException e) {
            lastRollText = "Move blocked";
            addHudEvent("MOVE BLOCKED", "Destination occupied", new ColorRGBA(1f, 0.34f, 0.34f, 1f));
            startDiceResultCamera();
            System.out.println("[Turn] Move blocked: destination occupied");
        }
    }

    private int inferVisualDiceRoll(int before, int after) {
        int delta = forwardDistance(before, after);
        if (delta >= 1 && delta <= 6) {
            return delta;
        }
        return 1 + Math.abs(after - before) % 6;
    }

    private String turnStatus(Monster movingMonster, int playerBefore, int opponentBefore) {
        int playerAfter = game.getPlayer().getPosition();
        int opponentAfter = game.getOpponent().getPosition();
        String playerChange = describePositionChange(game.getPlayer(), playerBefore, playerAfter);
        String opponentChange = describePositionChange(game.getOpponent(), opponentBefore, opponentAfter);
        String nextTurn = "Turn: " + game.getCurrent().getName();

        if (playerBefore == playerAfter && opponentBefore == opponentAfter) {
            return movingMonster.getName() + " stayed at cell " + movingMonster.getPosition() + " | " + nextTurn;
        }
        if (playerBefore != playerAfter && opponentBefore != opponentAfter) {
            return playerChange + " | " + opponentChange + " | " + nextTurn;
        }
        return (playerBefore != playerAfter ? playerChange : opponentChange) + " | " + nextTurn;
    }

    private String describePositionChange(Monster monster, int before, int after) {
        if (before == after) {
            return monster.getName() + " stayed at " + after;
        }
        if (after == Constants.STARTING_POSITION) {
            return monster.getName() + " returned to start";
        }
        return monster.getName() + ": " + before + " -> " + after;
    }

    private void addTurnHudEvents(Monster movingMonster, boolean movingPlayer,
                                  MonsterSnapshot playerBefore, MonsterSnapshot opponentBefore,
                                  int deckBefore) {
        MonsterSnapshot playerAfter = new MonsterSnapshot(game.getPlayer());
        MonsterSnapshot opponentAfter = new MonsterSnapshot(game.getOpponent());
        MonsterSnapshot movingBefore = movingPlayer ? playerBefore : opponentBefore;
        MonsterSnapshot movingAfter = movingPlayer ? playerAfter : opponentAfter;
        MonsterSnapshot otherBefore = movingPlayer ? opponentBefore : playerBefore;
        MonsterSnapshot otherAfter = movingPlayer ? opponentAfter : playerAfter;
        String actor = movingPlayer ? "You" : "Opponent";
        int deckAfter = Board.getCards().size();

        addHudEvent("TURN RESULT", actor + " " + positionText(movingBefore.position, movingAfter.position),
                movingPlayer ? new ColorRGBA(0.08f, 0.88f, 0.78f, 1f) : new ColorRGBA(1f, 0.63f, 0.42f, 1f));

        if (movingBefore.frozen && !movingAfter.frozen && movingBefore.position == movingAfter.position) {
            addHudEvent("FROZEN", actor + " skipped a turn", new ColorRGBA(0.42f, 0.82f, 1f, 1f));
        }

        if (otherBefore.position != otherAfter.position) {
            addHudEvent("POSITION EFFECT", (movingPlayer ? "Opponent" : "You") + " "
                    + positionText(otherBefore.position, otherAfter.position),
                    new ColorRGBA(0.72f, 0.34f, 1f, 1f));
        }

        addTransportEvent(actor, movingBefore, movingAfter);
        addCellEffectEvent(actor, movingBefore, movingAfter);

        if (deckAfter != deckBefore) {
            lastCardHighlight = inferCardHighlight(playerBefore, opponentBefore, playerAfter, opponentAfter);
            addHudEvent("CARD DRAWN", actor + " drew " + friendlyCardName(lastCardHighlight),
                    new ColorRGBA(0.72f, 0.34f, 1f, 1f));
        }
    }

    private String positionText(int before, int after) {
        if (before == after) {
            return "stayed at cell " + after;
        }
        if (after == Constants.STARTING_POSITION) {
            return "returned to start";
        }
        return "moved " + before + " -> " + after;
    }

    private void addTransportEvent(String actor, MonsterSnapshot before, MonsterSnapshot after) {
        int triggerIndex = findTransportTriggerIndex(before.position, after.position);
        if (triggerIndex < 0) {
            return;
        }

        Cell cell = engineCellAt(triggerIndex);
        int energyDelta = after.energy - before.energy;
        if (cell instanceof ContaminationSock) {
            String detail = actor + " slipped " + triggerIndex + " -> " + after.position;
            if (energyDelta < 0) {
                detail += " | " + energyDelta + " energy";
            }
            addHudEvent("CONTAMINATION SOCK", detail, new ColorRGBA(1f, 0.40f, 0.28f, 1f));
        } else if (cell instanceof ConveyorBelt) {
            addHudEvent("CONVEYOR BELT", actor + " moved " + triggerIndex + " -> " + after.position,
                    new ColorRGBA(0.35f, 0.86f, 1f, 1f));
        }
    }

    private void addCellEffectEvent(String actor, MonsterSnapshot before, MonsterSnapshot after) {
        Cell cell = engineCellAt(after.position);
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            int energyDelta = after.energy - before.energy;
            if (energyDelta != 0) {
                String sign = energyDelta > 0 ? "+" : "";
                addHudEvent(door.getRole() + " DOOR", actor + " " + sign + energyDelta + " energy",
                        energyDelta > 0 ? new ColorRGBA(0.20f, 0.86f, 1f, 1f)
                                : new ColorRGBA(1f, 0.38f, 0.30f, 1f));
            }
        } else if (cell instanceof MonsterCell) {
            int energyDelta = after.energy - before.energy;
            String detail = actor + " landed on " + cell.getName();
            if (energyDelta != 0) {
                detail += " | " + (energyDelta > 0 ? "+" : "") + energyDelta + " energy";
            }
            addHudEvent("MONSTER CELL", detail, new ColorRGBA(0.26f, 1f, 0.78f, 1f));
        }
    }

    private String inferCardHighlight(MonsterSnapshot playerBefore, MonsterSnapshot opponentBefore,
                                      MonsterSnapshot playerAfter, MonsterSnapshot opponentAfter) {
        if ((playerAfter.position == Constants.STARTING_POSITION && playerBefore.position != Constants.STARTING_POSITION)
                || (opponentAfter.position == Constants.STARTING_POSITION
                && opponentBefore.position != Constants.STARTING_POSITION)) {
            return "START OVER";
        }
        if (playerAfter.position == opponentBefore.position
                && opponentAfter.position == playerBefore.position
                && playerBefore.position != opponentBefore.position) {
            return "SWAPPER";
        }
        if ((!playerBefore.shielded && playerAfter.shielded)
                || (!opponentBefore.shielded && opponentAfter.shielded)) {
            return "SHIELD";
        }
        if (playerBefore.role != playerAfter.role || opponentBefore.role != opponentAfter.role
                || playerAfter.confusionTurns > playerBefore.confusionTurns
                || opponentAfter.confusionTurns > opponentBefore.confusionTurns) {
            return "CONFUSION";
        }
        if ((playerAfter.energy > playerBefore.energy && opponentAfter.energy < opponentBefore.energy)
                || (opponentAfter.energy > opponentBefore.energy && playerAfter.energy < playerBefore.energy)) {
            return "ENERGY STEAL";
        }
        return "CARD";
    }

    private String friendlyCardName(String key) {
        return "CARD".equals(key) ? "a card" : key;
    }

    private int findTransportTriggerIndex(int before, int after) {
        for (int index : Constants.CONVEYOR_CELL_INDICES) {
            if (transportMatches(before, after, index)) {
                return index;
            }
        }
        for (int index : Constants.SOCK_CELL_INDICES) {
            if (transportMatches(before, after, index)) {
                return index;
            }
        }
        return -1;
    }

    private boolean transportMatches(int before, int after, int transportIndex) {
        Cell cell = engineCellAt(transportIndex);
        if (!(cell instanceof TransportCell)) {
            return false;
        }
        int distanceToTransport = forwardDistance(before, transportIndex);
        if (distanceToTransport <= 0 || distanceToTransport > TOKEN_MAX_WALK_STEPS) {
            return false;
        }
        int target = normalizeCellIndex(transportIndex + ((TransportCell) cell).getEffect());
        return target == normalizeCellIndex(after);
    }

    private int forwardDistance(int from, int to) {
        return (normalizeCellIndex(to) - normalizeCellIndex(from) + Constants.BOARD_SIZE) % Constants.BOARD_SIZE;
    }

    private Cell engineCellAt(int index) {
        Cell[][] cells = game.getBoard().getBoardCells();
        int normalized = normalizeCellIndex(index);
        int row = normalized / Constants.BOARD_ROWS;
        int col = normalized % Constants.BOARD_COLS;
        if (row % 2 == 1) {
            col = Constants.BOARD_COLS - 1 - col;
        }
        return cells[row][col];
    }

    private void debugTokenState() {
        if (game == null) {
            return;
        }
        logMonsterState("PLAYER", game.getPlayer(), playerToken);
        logMonsterState("OPPONENT", game.getOpponent(), opponentToken);
        addHudEvent("DEBUG", String.format("Player %d | Opponent %d | Turn: %s",
                game.getPlayer().getPosition(), game.getOpponent().getPosition(), game.getCurrent().getName()),
                new ColorRGBA(0.68f, 0.72f, 0.86f, 1f));
    }

    private void logMonsterState(String label, Monster monster, MonsterToken token) {
        int visualCell = token == null ? -1 : token.cellIndex;
        System.out.printf("[Debug] %s %s role=%s energy=%d engineCell=%d visualCell=%d moving=%s special=%s%n",
                label, monster.getName(), monster.getRole(), monster.getEnergy(),
                monster.getPosition(), visualCell,
                token != null && token.moving,
                token != null && token.specialMove);
    }

    private boolean hasMovingToken() {
        for (MonsterToken token : activeTokens) {
            if (token.moving || token.pendingSteps > 0) {
                return true;
            }
        }
        return false;
    }

    private void syncTokenToMonster(MonsterToken token, int previousCell) {
        if (token == null || token.node == null) {
            return;
        }

        int targetCell = token.monster.getPosition();
        if (previousCell == targetCell) {
            token.cellIndex = targetCell;
            token.finalCellIndex = targetCell;
            token.pendingSteps = 0;
            token.moving = false;
            token.specialMove = false;
            token.node.setLocalTranslation(tokenFootPosition(token, targetCell));
            orientTokenForCell(token, targetCell);
            setTokenWalking(token, false);
            return;
        }
        startTokenPath(token, previousCell, targetCell);
    }

    private void startTokenPath(MonsterToken token, int startCell, int targetCell) {
        token.cellIndex = normalizeCellIndex(startCell);
        token.finalCellIndex = normalizeCellIndex(targetCell);
        token.node.setLocalTranslation(tokenFootPosition(token, token.cellIndex));

        int forwardSteps = token.finalCellIndex - token.cellIndex;
        if (forwardSteps <= 0 || forwardSteps > TOKEN_MAX_WALK_STEPS) {
            startSpecialTokenMove(token);
            return;
        }

        token.specialMove = false;
        token.stepDirection = 1;
        token.pendingSteps = forwardSteps;
        startNextTokenStep(token);
    }

    private void startSpecialTokenMove(MonsterToken token) {
        token.pendingSteps = 0;
        token.stepDirection = 0;
        token.stepTimer = 0f;
        token.stepStart.set(token.node.getLocalTranslation());
        token.stepEnd.set(tokenFootPosition(token, token.finalCellIndex));
        token.moving = true;
        token.specialMove = true;
        setTokenWalking(token, false);

        Vector3f direction = token.stepEnd.subtract(token.stepStart);
        if (direction.lengthSquared() > 0.0001f) {
            token.lastDirection.set(direction).normalizeLocal();
            rotateTokenToward(token, token.lastDirection);
        }

        System.out.printf("[VisualMove] %s special %d -> %d%n",
                token.monster.getName(), token.cellIndex, token.finalCellIndex);
    }

    private void startNextTokenStep(MonsterToken token) {
        if (token.pendingSteps <= 0) {
            token.moving = false;
            token.specialMove = false;
            token.cellIndex = token.finalCellIndex;
            token.node.setLocalTranslation(tokenFootPosition(token, token.cellIndex));
            setTokenWalking(token, false);
            orientTokenForCell(token, token.cellIndex);
            return;
        }

        int nextCell = normalizeCellIndex(token.cellIndex + token.stepDirection);
        token.stepTimer = 0f;
        token.stepStart.set(token.node.getLocalTranslation());
        token.stepEnd.set(tokenFootPosition(token, nextCell));
        token.pendingSteps--;
        token.moving = true;
        token.specialMove = false;
        setTokenWalking(token, true);

        Vector3f direction = token.stepEnd.subtract(token.stepStart);
        if (direction.lengthSquared() > 0.0001f) {
            token.lastDirection.set(direction).normalizeLocal();
        }
        rotateTokenToward(token, token.lastDirection);
    }

    private void updateTokenMovements(float tpf) {
        for (MonsterToken token : activeTokens) {
            updateTokenMovement(token, tpf);
        }
    }

    private void updateTokenMovement(MonsterToken token, float tpf) {
        if (token == null || !token.moving || token.node == null) {
            return;
        }

        if (token.specialMove) {
            updateSpecialTokenMovement(token, tpf);
            return;
        }

        token.stepTimer += tpf;
        float t = easeInOut(clamp(token.stepTimer / TOKEN_STEP_TIME, 0f, 1f));
        token.node.setLocalTranslation(
                lerp(token.stepStart.x, token.stepEnd.x, t),
                lerp(token.stepStart.y, token.stepEnd.y, t),
                lerp(token.stepStart.z, token.stepEnd.z, t));

        if (token.stepTimer >= TOKEN_STEP_TIME) {
            token.node.setLocalTranslation(token.stepEnd);
            token.cellIndex = normalizeCellIndex(token.cellIndex + token.stepDirection);
            startNextTokenStep(token);
        }
    }

    private void updateSpecialTokenMovement(MonsterToken token, float tpf) {
        token.stepTimer += tpf;
        float t = easeInOut(clamp(token.stepTimer / TOKEN_SPECIAL_MOVE_TIME, 0f, 1f));
        float lift = FastMath.sin(t * FastMath.PI) * TOKEN_SPECIAL_MOVE_HEIGHT;
        token.node.setLocalTranslation(
                lerp(token.stepStart.x, token.stepEnd.x, t),
                lerp(token.stepStart.y, token.stepEnd.y, t) + lift,
                lerp(token.stepStart.z, token.stepEnd.z, t));

        if (token.stepTimer >= TOKEN_SPECIAL_MOVE_TIME) {
            token.node.setLocalTranslation(token.stepEnd);
            token.cellIndex = token.finalCellIndex;
            token.moving = false;
            token.specialMove = false;
            orientTokenForCell(token, token.cellIndex);
        }
    }

    private void setTokenWalking(MonsterToken token, boolean walking) {
        if (token.animComposer == null || token.walkClip == null) {
            return;
        }
        token.animComposer.setGlobalSpeed(walking ? 1f : 0f);
    }

    private Vector3f tokenFootPosition(MonsterToken token, int index) {
        int cellIndex = normalizeCellIndex(index);
        Vector3f foot = boardCellCenter(cellIndex);
        foot.y = boardTopY + 0.02f;
        foot.x += token.laneOffset;
        if (isVisualDoorCell(cellIndex)) {
            foot.z += TOKEN_DOOR_OFFSET;
        }
        return foot;
    }

    private void orientTokenForCell(MonsterToken token, int index) {
        int cellIndex = normalizeCellIndex(index);
        if (isVisualDoorCell(cellIndex)) {
            Vector3f doorCenter = boardCellCenter(cellIndex);
            Vector3f tokenFoot = tokenFootPosition(token, cellIndex);
            rotateTokenToward(token, doorCenter.subtract(tokenFoot));
        } else {
            rotateTokenToward(token, token.lastDirection);
        }
    }

    private void rotateTokenToward(MonsterToken token, Vector3f direction) {
        if (token == null || token.node == null || direction.lengthSquared() < 0.0001f) {
            return;
        }
        float yaw = FastMath.atan2(direction.x, direction.z) + TOKEN_FORWARD_YAW_OFFSET;
        token.node.setLocalRotation(new Quaternion().fromAngles(0f, yaw, 0f));
    }

    private void startDiceRoll(boolean finalThrow) {
        if (diceNode == null) {
            return;
        }
        diceRolling = true;
        diceFinalThrow = finalThrow;
        diceTimer = 0f;
        if (!finalThrow) {
            diceNode.setLocalTranslation(DICE_HOME_X, DICE_HOME_Y + 0.08f, DICE_HOME_Z);
        }
    }

    private void updateDiceAnimation(float tpf) {
        if (diceNode == null || !diceRolling) {
            return;
        }

        diceTimer += tpf;
        if (diceFinalThrow) {
            float t = clamp(diceTimer / DICE_FINAL_ROLL_TIME, 0f, 1f);
            float eased = easeInOut(t);
            float bounce = FastMath.sin(t * FastMath.PI) * 0.52f;
            float settleBounce = t > 0.55f
                    ? FastMath.abs(FastMath.sin((t - 0.55f) * FastMath.TWO_PI * 3.0f)) * (1f - t) * 0.24f
                    : 0f;
            diceNode.setLocalTranslation(
                    lerp(DICE_HOME_X - 0.82f, DICE_HOME_X, eased),
                    DICE_HOME_Y + bounce + settleBounce,
                    lerp(DICE_HOME_Z + 0.38f, DICE_HOME_Z, eased));
            diceNode.setLocalRotation(new Quaternion().fromAngles(
                    t * FastMath.TWO_PI * (2.25f + lastVisualRoll * 0.16f),
                    t * FastMath.TWO_PI * (2.90f + lastVisualRoll * 0.10f),
                    t * FastMath.TWO_PI * 1.35f));
            if (t >= 1f) {
                diceRolling = false;
                diceNode.setLocalTranslation(DICE_HOME_X, DICE_HOME_Y, DICE_HOME_Z);
                diceNode.setLocalRotation(settledDiceRotation(lastVisualRoll));
            }
            return;
        }

        float hover = FastMath.sin(diceTimer * 7.0f) * 0.08f;
        diceNode.setLocalTranslation(DICE_HOME_X, DICE_HOME_Y + 0.12f + hover, DICE_HOME_Z);
        diceNode.setLocalRotation(new Quaternion().fromAngles(
                diceTimer * 8.2f,
                diceTimer * 10.8f,
                diceTimer * 6.0f));
    }

    private Quaternion settledDiceRotation(int roll) {
        switch (roll) {
            case 2:
                return new Quaternion().fromAngles(FastMath.HALF_PI, 0f, 0f);
            case 3:
                return new Quaternion().fromAngles(0f, 0f, FastMath.HALF_PI);
            case 4:
                return new Quaternion().fromAngles(0f, 0f, -FastMath.HALF_PI);
            case 5:
                return new Quaternion().fromAngles(-FastMath.HALF_PI, 0f, 0f);
            case 6:
                return new Quaternion().fromAngles(FastMath.PI, 0f, 0f);
            case 1:
            default:
                return new Quaternion();
        }
    }

    private boolean isIntroCameraActive() {
        return cameraMode == CameraMode.INTRO_PLAYERS || cameraMode == CameraMode.INTRO_360;
    }

    private void startIntroCameraSequence() {
        Vector3f target = startTokensTarget();
        Vector3f eye = target.add(new Vector3f(0.20f, 2.25f, 4.15f));
        movingCameraToken = null;
        setCameraShot(CameraMode.INTRO_PLAYERS, eye, target, CAMERA_FOCUS_FOV, CAMERA_INTRO_ZOOM_TIME);
    }

    private void startIntroOrbitCamera() {
        introOrbitTarget.set(startTokensTarget());
        cameraMode = CameraMode.INTRO_360;
        cameraTimer = 0f;
        cameraDuration = CAMERA_INTRO_ORBIT_TIME;
        cameraStartFov = CAMERA_FOCUS_FOV;
        cameraEndFov = CAMERA_FOCUS_FOV;
    }

    private void startTurnCamera(MonsterToken rolledToken) {
        MonsterToken followToken = rolledToken;
        if (!isTokenAnimating(followToken)) {
            followToken = firstAnimatingToken();
        }
        if (followToken != null && isTokenAnimating(followToken)) {
            startFollowCamera(followToken);
        } else {
            startLandingFocusCamera(rolledToken);
        }
    }

    private void startFollowCamera(MonsterToken token) {
        movingCameraToken = token;
        cameraMode = CameraMode.FOLLOW_MOVE;
        cameraTimer = 0f;
        cameraDuration = 0f;
        followCameraEye.set(cam.getLocation());
    }

    private void startLandingFocusCamera(MonsterToken token) {
        if (token == null || token.node == null) {
            startDiceResultCamera();
            return;
        }
        movingCameraToken = token;
        Vector3f target = tokenCameraTarget(token);
        Vector3f eye = target.add(new Vector3f(1.95f, 2.35f, 3.35f));
        setCameraShot(CameraMode.LANDING_FOCUS, eye, target, CAMERA_FOCUS_FOV, CAMERA_LANDING_TIME);
    }

    private void startDiceResultCamera() {
        movingCameraToken = null;
        startDiceRoll(true);
        setCameraShot(CameraMode.DICE_RESULT_CLOSE,
                new Vector3f(0f, 4.08f, -3.40f),
                new Vector3f(0f, 3.46f, -8.10f),
                34f,
                CAMERA_DICE_RESULT_TIME);
    }

    private void startWideBoardCamera(float duration) {
        movingCameraToken = null;
        setCameraShot(CameraMode.RESULT_WIDE, wideCameraEye(), wideCameraTarget(),
                CAMERA_WIDE_FOV, duration);
    }

    private boolean isTokenAnimating(MonsterToken token) {
        return token != null && (token.moving || token.pendingSteps > 0);
    }

    private MonsterToken firstAnimatingToken() {
        for (MonsterToken token : activeTokens) {
            if (isTokenAnimating(token)) {
                return token;
            }
        }
        return null;
    }

    private Vector3f startTokensTarget() {
        if (playerToken == null || opponentToken == null) {
            return new Vector3f(-BOARD_CELL_SIZE * 4.5f, 0.92f, BOARD_CELL_SIZE * 4.5f);
        }
        Vector3f player = tokenCameraTarget(playerToken);
        Vector3f opponent = tokenCameraTarget(opponentToken);
        return new Vector3f(
                (player.x + opponent.x) * 0.5f,
                Math.max(player.y, opponent.y),
                (player.z + opponent.z) * 0.5f);
    }

    private Vector3f tokenCameraTarget(MonsterToken token) {
        Vector3f target = token.node.getWorldTranslation().clone();
        target.y += 0.68f;
        return target;
    }

    private Vector3f followCameraEyeFor(Vector3f target) {
        return new Vector3f(
                target.x + 1.8f,
                target.y + 2.55f,
                target.z + 4.35f);
    }

    private Vector3f wideCameraEye() {
        return new Vector3f(0f, 8.55f, 14.00f);
    }

    private Vector3f wideCameraTarget() {
        return new Vector3f(0f, 2.10f, -2.60f);
    }

    private void setCameraShot(CameraMode mode, Vector3f eye, Vector3f target,
                               float fov, float duration) {
        cameraMode = mode;
        cameraTimer = 0f;
        cameraDuration = Math.max(0.001f, duration);
        cameraStartEye.set(cam.getLocation());
        cameraEndEye.set(eye);
        cameraStartTarget.set(cameraTarget);
        cameraEndTarget.set(target);
        cameraStartFov = cameraCurrentFov;
        cameraEndFov = fov;
    }

    private void setImmediateCamera(CameraMode mode, Vector3f eye, Vector3f target, float fov) {
        cameraMode = mode;
        cameraTimer = 0f;
        cameraDuration = 0f;
        applyCamera(eye, target, fov);
    }

    private void updateScriptedCamera(float tpf) {
        if (cameraMode == CameraMode.INTRO_360) {
            updateIntroOrbitCamera(tpf);
            return;
        }
        if (cameraMode == CameraMode.FOLLOW_MOVE) {
            updateFollowCamera(tpf);
            return;
        }
        if (cameraDuration <= 0f) {
            return;
        }

        cameraTimer += tpf;
        float t = easeInOut(clamp(cameraTimer / cameraDuration, 0f, 1f));
        Vector3f eye = lerpVector(cameraStartEye, cameraEndEye, t);
        Vector3f target = lerpVector(cameraStartTarget, cameraEndTarget, t);
        applyCamera(eye, target, lerp(cameraStartFov, cameraEndFov, t));

        if (cameraTimer >= cameraDuration) {
            onCameraShotComplete();
        }
    }

    private void updateIntroOrbitCamera(float tpf) {
        cameraTimer += tpf;
        float t = clamp(cameraTimer / cameraDuration, 0f, 1f);
        float angle = -FastMath.HALF_PI + t * FastMath.TWO_PI;
        float radius = 4.10f;
        Vector3f eye = new Vector3f(
                introOrbitTarget.x + FastMath.sin(angle) * radius,
                introOrbitTarget.y + 2.15f,
                introOrbitTarget.z + FastMath.cos(angle) * radius);
        applyCamera(eye, introOrbitTarget, CAMERA_FOCUS_FOV);
        if (cameraTimer >= cameraDuration) {
            startWideBoardCamera(CAMERA_WIDE_TIME);
        }
    }

    private void updateFollowCamera(float tpf) {
        if (movingCameraToken == null || movingCameraToken.node == null) {
            startWideBoardCamera(CAMERA_RESULT_TIME);
            return;
        }
        if (!hasMovingToken()) {
            startLandingFocusCamera(movingCameraToken);
            return;
        }

        Vector3f target = tokenCameraTarget(movingCameraToken);
        Vector3f desiredEye = followCameraEyeFor(target);
        float alpha = clamp(tpf * CAMERA_FOLLOW_SMOOTHING, 0f, 1f);
        followCameraEye.interpolateLocal(desiredEye, alpha);
        cameraTarget.interpolateLocal(target, alpha);
        applyCamera(followCameraEye, cameraTarget, 40f);
    }

    private void onCameraShotComplete() {
        applyCamera(cameraEndEye, cameraEndTarget, cameraEndFov);
        if (cameraMode == CameraMode.INTRO_PLAYERS) {
            startIntroOrbitCamera();
        } else if (cameraMode == CameraMode.LANDING_FOCUS) {
            startDiceResultCamera();
        } else if (cameraMode == CameraMode.DICE_RESULT_CLOSE) {
            startWideBoardCamera(CAMERA_RESULT_TIME);
        } else if (cameraMode == CameraMode.RESULT_WIDE) {
            cameraMode = CameraMode.WIDE_BOARD;
            cameraDuration = 0f;
        }
    }

    private void applyCamera(Vector3f eye, Vector3f target, float fov) {
        cam.setLocation(eye);
        cameraTarget.set(target);
        cameraCurrentFov = fov;
        cam.lookAt(cameraTarget, Vector3f.UNIT_Y);
        cam.setFrustumPerspective(fov, cam.getWidth() / (float) cam.getHeight(), 0.1f, 80f);
    }

    private Vector3f lerpVector(Vector3f from, Vector3f to, float t) {
        return new Vector3f(
                lerp(from.x, to.x, t),
                lerp(from.y, to.y, t),
                lerp(from.z, to.z, t));
    }

    private int normalizeCellIndex(int index) {
        int normalized = index % Constants.BOARD_SIZE;
        return normalized < 0 ? normalized + Constants.BOARD_SIZE : normalized;
    }

    @Override
    public void simpleUpdate(float tpf) {
        updateTokenMovements(tpf);
        updateDiceAnimation(tpf);
        updateStatusMessage(tpf);
        updateScriptedCamera(tpf);
    }

    private void updateStatusMessage(float tpf) {
        if (statusText == null || statusText.getParent() == null || statusTimer <= 0f) {
            return;
        }
        statusTimer -= tpf;
        if (statusTimer <= 0f) {
            guiNode.detachChild(statusText);
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float easeInOut(float t) {
        return t < 0.5f ? 2f * t * t : 1f - FastMath.pow(-2f * t + 2f, 2f) * 0.5f;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    private static final class CellTemplate {
        private final Spatial spatial;
        private final Vector3f center;
        private final float halfHeight;

        private CellTemplate(Spatial spatial, Vector3f center, float halfHeight) {
            this.spatial = spatial;
            this.center = center;
            this.halfHeight = halfHeight;
        }
    }
}
