package game.view3d;

import game.engine.Constants;
import game.engine.Board;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
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
    private static final float HUD_TOP_HEIGHT = 172f;
    private static final float HUD_BOTTOM_HEIGHT = 68f;
    private static final int HUD_BOTTOM_EVENT_COUNT = 3;
    private static final String[] MONSTER_CELL_MODELS = {
            "Models/boardcells/monstercells/Mike Wazowski Cell.glb",
            "Models/boardcells/monstercells/Randall Boggs Cell.glb",
            "Models/boardcells/monstercells/Sulley (James P. Sullivan) Cell.glb",
            "Models/boardcells/monstercells/celia.glb",
            "Models/boardcells/monstercells/fungus.glb",
            "Models/boardcells/monstercells/yeti.glb"
    };

    private static final float CAMERA_WIDE_FOV = 57f;
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
    private String roleSelectionError;
    private boolean sceneReady;

    public static void main(String[] args) {
        FactoryShellApp app = new FactoryShellApp();
        app.setSettings(buildSettings());
        app.setShowSettings(false);
        for (String arg : args) {
            if (arg.startsWith("--role=")) {
                String r = arg.substring(7);
                app.setPreselectedRole(r.equals("scarer") ? Role.SCARER : Role.LAUGHER);
            }
        }
        app.start();
    }

    private Role preselectedRole = null;

    public void setPreselectedRole(Role role) {
        this.preselectedRole = role;
    }

    public static AppSettings buildPublicSettings() {
        return buildSettings();
    }

    private static AppSettings buildSettings() {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("DooR DasH - Factory Shell");
        settings.setWidth(1600);
        settings.setHeight(900);
        settings.setMinResolution(960, 540);
        settings.setResizable(true);
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
        setImmediateCamera(CameraMode.ROLE_SELECTION_VIEW,
                new Vector3f(0f, 8.2f, 15.0f),
                new Vector3f(0f, 1.05f, -1.6f),
                46f);
        sceneReady = true;
        if (preselectedRole != null) {
            startBoardGame(preselectedRole);
        } else {
            showRoleSelectionMenu(null);
        }
    }

    private void showRoleSelectionMenu(String error) {
        roleSelectionError = error;
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

    @Override
    public void reshape(int width, int height) {
        super.reshape(width, height);
        if (!sceneReady || cam == null || width <= 0 || height <= 0) {
            return;
        }

        applyCamera(cam.getLocation(), cameraTarget, cameraCurrentFov);
        if (appMode == AppMode.ROLE_SELECTION) {
            showRoleSelectionMenu(roleSelectionError);
        } else {
            refreshGameplayHud();
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
        attachWorldDice();
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
            ensureGameplayHud();
        }
    }

    private void refreshGameplayHud() {
        gameplayHudGui.detachAllChildren();
        if (appMode != AppMode.BOARD || game == null) {
            return;
        }

        float screenW = cam.getWidth();
        float screenH = cam.getHeight();
        drawTopGameplayHud(screenW, screenH);
        drawBottomGameplayHud(screenW);
    }

    private void drawBottomGameplayHud(float screenW) {
        float x = HUD_MARGIN;
        float y = 8f;
        float width = screenW - HUD_MARGIN * 2f;
        float height = HUD_BOTTOM_HEIGHT;
        ColorRGBA accent = game.getCurrent() == game.getPlayer()
                ? new ColorRGBA(0.08f, 0.88f, 0.78f, 1f)
                : new ColorRGBA(1f, 0.63f, 0.42f, 1f);

        drawHudFrame("bottom_hud", x, y, width, height, accent);

        float gap = 10f;
        float turnW = clamp(width * 0.15f, 180f, 250f);
        float actionW = clamp(width * 0.17f, 235f, 315f);
        float sideButtonW = 28f;
        float actionX = x + width - actionW - sideButtonW - gap - 12f;
        float turnX = x + 12f;
        float innerY = y + 8f;
        float innerH = height - 16f;
        float eventX = turnX + turnW + gap;
        float eventW = actionX - eventX - gap;

        drawHudFrame("turn_chip", turnX, innerY, turnW, innerH, accent);
        gameplayHudGui.attachChild(guiText("CURRENT TURN", turnX + 12f, innerY + innerH - 13f, 10f, accent));
        gameplayHudGui.attachChild(guiText(shortText(game.getCurrent().getName(), maxTextChars(turnW - 92f, 11f)),
                turnX + 12f, innerY + 19f, 18f, new ColorRGBA(1f, 0.88f, 0.72f, 1f)));
        float roleSize = 34f;
        float roleIconX = turnX + turnW - roleSize - 12f;
        float roleIconY = innerY + 9f;
        float roleCut = (roleSize + 8f) * 0.22f;
        ColorRGBA chipBg = new ColorRGBA(0.006f, 0.012f, 0.022f, 1f);
        gameplayHudGui.attachChild(guiQuad("turn_role_diamond", roleIconX - 4f, roleIconY - 4f, roleSize + 8f, roleSize + 8f,
                colorMaterial(new ColorRGBA(accent.r * 0.60f, accent.g * 0.60f, accent.b * 0.65f, 1f)), 0.82f));
        gameplayHudGui.attachChild(guiQuad("turn_role_cut_tl", roleIconX - 4f, roleIconY + roleSize + 4f - roleCut, roleCut, roleCut,
                colorMaterial(chipBg), 0.83f));
        gameplayHudGui.attachChild(guiQuad("turn_role_cut_tr", roleIconX + roleSize + 4f - roleCut, roleIconY + roleSize + 4f - roleCut, roleCut, roleCut,
                colorMaterial(chipBg), 0.83f));
        gameplayHudGui.attachChild(guiQuad("turn_role_cut_bl", roleIconX - 4f, roleIconY - 4f, roleCut, roleCut,
                colorMaterial(chipBg), 0.83f));
        gameplayHudGui.attachChild(guiQuad("turn_role_cut_br", roleIconX + roleSize + 4f - roleCut, roleIconY - 4f, roleCut, roleCut,
                colorMaterial(chipBg), 0.83f));
        drawIconSlot("turn_role_slot", roleIconX, roleIconY, roleSize,
                accent, roleInitial(game.getCurrent()), roleIconPath(game.getCurrent().getRole()));

        gameplayHudGui.attachChild(guiQuad("last_roll_marker", eventX, y + height - 22f, 3f, 14f,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiText(shortText(lastRollText.toUpperCase(), maxTextChars(eventW, 9f)),
                eventX + 12f, y + height - 10f, 12f, new ColorRGBA(0.94f, 0.96f, 1f, 1f)));

        float rowY = innerY + 5f;
        float rowGap = 8f;
        float rowH = 28f;
        float rowW = (eventW - rowGap * (HUD_BOTTOM_EVENT_COUNT - 1)) / HUD_BOTTOM_EVENT_COUNT;
        for (int i = 0; i < HUD_BOTTOM_EVENT_COUNT; i++) {
            HudEvent event = i < eventLog.size() ? eventLog.get(i) : null;
            float rowX = eventX + i * (rowW + rowGap);
            ColorRGBA rowAccent = event == null
                    ? new ColorRGBA(0.10f, 0.14f, 0.20f, 1f)
                    : event.accent;
            drawHudFrame("bottom_event_" + i, rowX, rowY, rowW, rowH, rowAccent);
            if (event != null) {
                drawIconSlot("bottom_event_icon_" + i, rowX + 8f, rowY + 5f, 18f,
                        rowAccent, event.title.substring(0, 1), eventIconPath(event.title));
                String summary = event.title + ": " + event.detail;
                gameplayHudGui.attachChild(guiText(shortText(summary, maxTextChars(rowW - 40f, 7f)),
                        rowX + 34f, rowY + 18f, 11f,
                        new ColorRGBA(0.96f, 0.96f, 1f, 1f)));
            }
        }

        drawHudFrame("roll_action", actionX, innerY, actionW, innerH, new ColorRGBA(0.82f, 0.54f, 1f, 1f));
        drawIconSlot("roll_dice_slot", actionX + 14f, innerY + 10f, 30f,
                new ColorRGBA(0.82f, 0.54f, 1f, 1f), "D", "Textures/ui/icon-dice.png");
        gameplayHudGui.attachChild(guiText("SPACE ROLL", actionX + 54f, innerY + 33f, 20f,
                new ColorRGBA(0.98f, 0.95f, 1f, 1f)));
        gameplayHudGui.attachChild(guiText("P POWERUP  |  R CAMERA", actionX + 56f, innerY + 13f, 11f,
                new ColorRGBA(0.76f, 0.70f, 0.92f, 1f)));

        float buttonX = actionX + actionW + gap;
        drawIconSlot("help_slot", buttonX, innerY + innerH - 22f, 20f, accent, "?",
                "Textures/ui/icon-help.png");
        drawIconSlot("settings_slot", buttonX, innerY + 4f, 20f, accent, "*",
                "Textures/ui/icon-settings.png");
    }

    private void drawTopGameplayHud(float screenW, float screenH) {
        float x = HUD_MARGIN;
        float width = screenW - HUD_MARGIN * 2f;
        float height = HUD_TOP_HEIGHT;
        float y = screenH - HUD_MARGIN - height;
        float contentW = width;
        float panelH = height;
        float gap = 12f;
        float statW = clamp(contentW * 0.32f, 300f, 430f);
        float cardsW = contentW - statW * 2f - gap * 2f;

        ColorRGBA playerAccent = new ColorRGBA(0.08f, 0.88f, 0.78f, 1f);
        ColorRGBA opponentAccent = new ColorRGBA(1f, 0.63f, 0.42f, 1f);

        drawTopMonsterPanel("top_player", "PLAYER", game.getPlayer(), x, y,
                statW, panelH, playerAccent, game.getCurrent() == game.getPlayer());
        drawTopCardReference(x + statW + gap, y, cardsW, panelH);
        drawTopMonsterPanel("top_opponent", "OPPONENT", game.getOpponent(),
                x + statW + gap + cardsW + gap, y,
                statW, panelH, opponentAccent, game.getCurrent() == game.getOpponent());
    }

    private void drawTopMonsterPanel(String prefix, String label, Monster monster,
                                     float x, float y, float width, float height,
                                     ColorRGBA accent, boolean current) {
        drawHudFrame(prefix, x, y, width, height, accent);

        if (current) {
            gameplayHudGui.attachChild(guiQuad(prefix + "_turn_glow", x + 14f, y + height - 31f,
                    width - 28f, 19f,
                    colorMaterial(new ColorRGBA(accent.r * 0.28f, accent.g * 0.28f, accent.b * 0.34f, 0.94f)),
                    0.76f));
            gameplayHudGui.attachChild(guiText("CURRENT TURN", x + width - 116f, y + height - 17f, 10f,
                    new ColorRGBA(0.98f, 0.96f, 1f, 1f)));
        }

        float portrait = clamp(Math.min(height * 0.40f, width * 0.22f), 54f, 72f);
        float portraitX = x + 22f;
        float portraitY = y + height - portrait - 48f;
        float textX = portraitX + portrait + 18f;
        float valueX = x + width - 112f;
        int nameChars = maxTextChars(valueX - textX - 8f, 11f);
        String energyText = monster.getEnergy() + " / " + Constants.WINNING_ENERGY;
        float energyRatio = clamp(monster.getEnergy() / (float) Constants.WINNING_ENERGY, 0f, 1f);
        float energyX = textX;
        float energyY = y + height - 114f;
        float energyW = Math.max(70f, valueX - textX - 16f);

        gameplayHudGui.attachChild(guiText(label, textX, y + height - 25f, 12f, accent));
        drawPortraitSlot(prefix + "_portrait", monster, portraitX, portraitY, portrait, accent);
        gameplayHudGui.attachChild(guiText(shortText(monster.getName(), nameChars), textX, y + height - 53f,
                24f, new ColorRGBA(0.98f, 0.98f, 1f, 1f)));
        gameplayHudGui.attachChild(guiText(monsterTitle(monster), textX, y + height - 76f, 13f,
                new ColorRGBA(0.72f, 0.78f, 0.95f, 1f)));

        gameplayHudGui.attachChild(guiText(energyText, valueX, y + height - 65f, 13f,
                new ColorRGBA(0.92f, 0.93f, 1f, 1f)));
        gameplayHudGui.attachChild(guiText("CELL " + monster.getPosition() + " / " + Constants.WINNING_POSITION,
                valueX, y + height - 91f, 13f, new ColorRGBA(0.92f, 0.93f, 1f, 1f)));
        drawEnergyBar(prefix + "_energy", energyX, energyY, energyW, 13f, energyRatio, accent);

        String status = shortText(statusSummary(monster).toUpperCase(), maxTextChars(width - 86f, 8f));
        float statusY = y + 18f;
        gameplayHudGui.attachChild(guiQuad(prefix + "_status_border", x + 20f, statusY - 2f, width - 40f, 34f,
                colorMaterial(new ColorRGBA(0.90f, 0.70f, 0.20f, 0.52f)), 0.74f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_status_bg", x + 22f, statusY, width - 44f, 30f,
                colorMaterial(new ColorRGBA(accent.r * 0.12f, accent.g * 0.14f, accent.b * 0.18f, 0.90f)),
                0.76f));
        drawIconSlot(prefix + "_status_icon", x + 36f, statusY + 6f, 18f, accent, "S",
                "Textures/ui/icon-status.png");
        gameplayHudGui.attachChild(guiText(status, x + 66f, statusY + 20f, 12f,
                new ColorRGBA(0.94f, 0.96f, 1f, 1f)));
    }

    private void drawTopCardReference(float x, float y, float width, float height) {
        ColorRGBA accent = new ColorRGBA(0.82f, 0.54f, 1f, 1f);
        drawHudFrame("top_cards", x, y, width, height, accent);
        gameplayHudGui.attachChild(guiText("CARD EFFECTS", x + 28f, y + height - 24f, 18f,
                new ColorRGBA(0.98f, 0.96f, 1f, 1f)));
        gameplayHudGui.attachChild(guiText("REFERENCE BOARD", x + width - 130f, y + height - 24f, 10f,
                new ColorRGBA(0.66f, 0.72f, 0.88f, 1f)));

        String[][] cards = cardRows();
        ColorRGBA[] colors = cardColors();
        float boardSize = width > 520f ? 94f : 0f;
        float rowX = x + 26f;
        float rowW = width - 52f - (boardSize > 0f ? boardSize + 26f : 0f);
        float rowH = 18f;
        float rowGap = 6f;
        float rowY = y + height - 62f;
        float nameW = clamp(rowW * 0.34f, 94f, 150f);
        int detailChars = maxTextChars(rowW - nameW - 24f, 6.5f);

        for (int i = 0; i < cards.length; i++) {
            boolean highlighted = cards[i][0].equals(lastCardHighlight);
            ColorRGBA rowAccent = colors[i];
            ColorRGBA bg = highlighted
                    ? new ColorRGBA(rowAccent.r * 0.24f, rowAccent.g * 0.24f, rowAccent.b * 0.24f, 0.98f)
                    : new ColorRGBA(0.055f, 0.066f, 0.12f, 0.86f);
            float currentY = rowY - i * (rowH + rowGap);

            gameplayHudGui.attachChild(guiQuad("top_card_row_" + i, rowX, currentY, rowW, rowH,
                    colorMaterial(bg), 0.76f));
            drawIconSlot("top_card_icon_" + i, rowX + 6f, currentY + 3f, 12f,
                    rowAccent, cardInitial(cards[i][0]), cardIconPath(cards[i][0]));
            gameplayHudGui.attachChild(guiText(shortText(cards[i][0], maxTextChars(nameW, 6.6f)),
                    rowX + 28f, currentY + 13f, 11f,
                    highlighted ? rowAccent : new ColorRGBA(0.94f, 0.94f, 1f, 1f)));
            gameplayHudGui.attachChild(guiText(shortText(cards[i][1], detailChars),
                    rowX + nameW + 28f, currentY + 13f, 11f,
                    new ColorRGBA(0.68f, 0.74f, 0.88f, 1f)));
        }

        if (boardSize > 0f) {
            drawMiniReferenceBoard("card_ref_board", x + width - boardSize - 32f,
                    y + 30f, boardSize, colors);
        }
    }

    private void drawHudFrame(String prefix, float x, float y, float width, float height, ColorRGBA accent) {
        gameplayHudGui.attachChild(guiQuad(prefix + "_shadow", x + 4f, y - 4f, width, height,
                colorMaterial(new ColorRGBA(0f, 0f, 0f, 0.32f)), 0.48f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_bg", x, y, width, height,
                colorMaterial(new ColorRGBA(0.006f, 0.012f, 0.022f, 0.88f)), 0.50f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_inner", x + 6f, y + 6f, width - 12f, height - 12f,
                colorMaterial(new ColorRGBA(0.026f, 0.040f, 0.070f, 0.78f)), 0.54f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_top", x + 8f, y + height - 4f, width - 16f, 3f,
                colorMaterial(accent), 0.92f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_top_glow", x + 10f, y + height - 9f, width - 20f, 6f,
                colorMaterial(new ColorRGBA(accent.r, accent.g, accent.b, 0.20f)), 0.82f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_bottom", x + 8f, y + 1f, width - 16f, 2f,
                colorMaterial(new ColorRGBA(accent.r, accent.g, accent.b, 0.72f)), 0.84f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_left", x + 1f, y + 8f, 3f, height - 16f,
                colorMaterial(new ColorRGBA(accent.r, accent.g, accent.b, 0.72f)), 0.84f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_right", x + width - 4f, y + 8f, 3f, height - 16f,
                colorMaterial(new ColorRGBA(accent.r, accent.g, accent.b, 0.72f)), 0.84f));

        float corner = Math.min(34f, Math.min(width, height) * 0.24f);
        gameplayHudGui.attachChild(guiQuad(prefix + "_tl_h", x, y + height - 10f, corner, 3f,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_tl_v", x + 7f, y + height - corner, 3f, corner,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_tr_h", x + width - corner, y + height - 10f, corner, 3f,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_tr_v", x + width - 10f, y + height - corner, 3f, corner,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_bl_h", x, y + 7f, corner, 3f,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_bl_v", x + 7f, y, 3f, corner,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_br_h", x + width - corner, y + 7f, corner, 3f,
                colorMaterial(accent), 0.88f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_br_v", x + width - 10f, y, 3f, corner,
                colorMaterial(accent), 0.88f));
    }

    private void drawPortraitSlot(String prefix, Monster monster, float x, float y, float size, ColorRGBA accent) {
        String initial = monster.getName() == null || monster.getName().isEmpty()
                ? "?" : monster.getName().substring(0, 1).toUpperCase();
        float frame = size + 8f;
        float fx = x - 4f;
        float fy = y - 4f;
        float cut = frame * 0.22f;
        ColorRGBA panelBg = new ColorRGBA(0.006f, 0.012f, 0.022f, 1f);
        gameplayHudGui.attachChild(guiQuad(prefix + "_hex_bg", fx, fy, frame, frame,
                colorMaterial(new ColorRGBA(accent.r * 0.60f, accent.g * 0.60f, accent.b * 0.65f, 1f)), 0.82f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_hex_tl", fx, fy + frame - cut, cut, cut,
                colorMaterial(panelBg), 0.83f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_hex_tr", fx + frame - cut, fy + frame - cut, cut, cut,
                colorMaterial(panelBg), 0.83f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_hex_bl", fx, fy, cut, cut,
                colorMaterial(panelBg), 0.83f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_hex_br", fx + frame - cut, fy, cut, cut,
                colorMaterial(panelBg), 0.83f));
        drawIconSlot(prefix, x, y, size, accent, initial, portraitPathForMonster(monster));
    }

    private void drawIconSlot(String prefix, float x, float y, float size,
                              ColorRGBA accent, String fallbackText, String texturePath) {
        gameplayHudGui.attachChild(guiQuad(prefix + "_outer", x, y, size, size,
                colorMaterial(new ColorRGBA(accent.r * 0.28f, accent.g * 0.28f, accent.b * 0.30f, 0.96f)),
                0.86f));
        gameplayHudGui.attachChild(guiQuad(prefix + "_inner", x + 3f, y + 3f, size - 6f, size - 6f,
                colorMaterial(new ColorRGBA(0.020f, 0.032f, 0.052f, 0.96f)), 0.88f));

        Material texture = textureMaterial(texturePath);
        if (texture != null) {
            gameplayHudGui.attachChild(guiQuad(prefix + "_image", x + 5f, y + 5f, size - 10f, size - 10f,
                    texture, 0.90f));
            return;
        }

        gameplayHudGui.attachChild(guiQuad(prefix + "_missing", x + 7f, y + 7f, size - 14f, size - 14f,
                colorMaterial(new ColorRGBA(accent.r * 0.16f, accent.g * 0.16f, accent.b * 0.20f, 0.94f)),
                0.90f));
        if (fallbackText != null && !fallbackText.isEmpty()) {
            float textSize = clamp(size * 0.42f, 8f, 28f);
            float textX = x + size * (fallbackText.length() > 1 ? 0.27f : 0.37f);
            gameplayHudGui.attachChild(guiText(fallbackText, textX, y + size * 0.62f,
                    textSize, new ColorRGBA(0.96f, 0.98f, 1f, 1f)));
        }
    }

    private Material textureMaterial(String texturePath) {
        if (texturePath == null) {
            return null;
        }
        try {
            Texture texture = assetManager.loadTexture(texturePath);
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
        if (name.contains("mike")) {
            return "Textures/ui/mike-portrait.png";
        }
        if (name.contains("sullivan") || name.contains("sulley")) {
            return "Textures/ui/sullivan-portrait.png";
        }
        if (name.contains("randall")) {
            return "Textures/ui/randall-portrait.png";
        }
        if (name.contains("celia")) {
            return "Textures/ui/celia-portrait.png";
        }
        if (name.contains("fungus")) {
            return "Textures/ui/fungus-portrait.png";
        }
        if (name.contains("yeti")) {
            return "Textures/ui/yeti-portrait.png";
        }
        return null;
    }

    private String roleIconPath(Role role) {
        return role == Role.SCARER ? "Textures/ui/role-scarer.png" : "Textures/ui/role-laugher.png";
    }

    private String cardIconPath(String cardName) {
        if ("ENERGY STEAL".equals(cardName)) {
            return "Textures/ui/card-energy-steal.png";
        }
        if ("SHIELD".equals(cardName)) {
            return "Textures/ui/card-shield.png";
        }
        if ("SWAPPER".equals(cardName)) {
            return "Textures/ui/card-swapper.png";
        }
        if ("START OVER".equals(cardName)) {
            return "Textures/ui/card-start-over.png";
        }
        if ("CONFUSION".equals(cardName)) {
            return "Textures/ui/card-confusion.png";
        }
        return null;
    }

    private String eventIconPath(String title) {
        if (title == null) {
            return null;
        }
        String key = title.toLowerCase().replace(' ', '-');
        return "Textures/ui/event-" + key + ".png";
    }

    private String monsterTitle(Monster monster) {
        return (monster.getClass().getSimpleName() + " / " + monster.getRole().name()).toUpperCase();
    }

    private String roleInitial(Monster monster) {
        String role = monster.getRole().name();
        return role.isEmpty() ? "?" : role.substring(0, 1);
    }

    private String cardInitial(String cardName) {
        if (cardName == null || cardName.isEmpty()) {
            return "?";
        }
        if ("START OVER".equals(cardName)) {
            return "O";
        }
        if ("SWAPPER".equals(cardName)) {
            return "W";
        }
        return cardName.substring(0, 1);
    }

    private void drawEnergyBar(String prefix, float x, float y, float width, float height,
                               float ratio, ColorRGBA accent) {
        gameplayHudGui.attachChild(guiQuad(prefix + "_bg", x, y, width, height,
                colorMaterial(new ColorRGBA(0.08f, 0.10f, 0.13f, 0.95f)), 0.78f));
        if (ratio > 0f) {
            gameplayHudGui.attachChild(guiQuad(prefix + "_fill", x, y, Math.max(3f, width * ratio), height,
                    colorMaterial(accent), 0.84f));
        }
        int segments = 6;
        for (int i = 1; i < segments; i++) {
            float sx = x + width * i / segments;
            gameplayHudGui.attachChild(guiQuad(prefix + "_tick_" + i, sx, y, 1f, height,
                    colorMaterial(new ColorRGBA(0.006f, 0.012f, 0.022f, 0.76f)), 0.88f));
        }
    }

    private void drawMiniReferenceBoard(String prefix, float x, float y, float size, ColorRGBA[] colors) {
        drawHudFrame(prefix, x, y, size, size, new ColorRGBA(0.14f, 0.84f, 0.92f, 1f));
        float gap = 3f;
        float cell = (size - 26f - gap * 3f) / 4f;
        float startX = x + 13f;
        float startY = y + 13f;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int index = row * 4 + col;
                ColorRGBA color = new ColorRGBA(0.035f, 0.052f, 0.074f, 0.96f);
                if (index == 1 || index == 7 || index == 12) {
                    color = new ColorRGBA(0.12f, 0.86f, 0.92f, 0.88f);
                } else if (index == 5) {
                    color = colors[2];
                } else if (index == 10) {
                    color = colors[4];
                }
                gameplayHudGui.attachChild(guiQuad(prefix + "_cell_" + index,
                        startX + col * (cell + gap),
                        startY + (3 - row) * (cell + gap),
                        cell, cell, colorMaterial(color), 0.92f));
            }
        }
    }

    private int maxTextChars(float width, float approximateCharWidth) {
        return Math.max(4, (int) (width / approximateCharWidth));
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
        ensureGameplayHud();
    }

    private void attachWorldDice() {
        if (screenRoot != null) {
            rootNode.detachChild(screenRoot);
        }
        screenRoot = new Node("dice_display");
        rootNode.attachChild(screenRoot);

        attachDice();
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
        try {
            game.playTurn();
            refreshInfoPanels();
            syncTokenToMonster(playerToken, playerBefore);
            syncTokenToMonster(opponentToken, opponentBefore);
            lastRollText = turnStatus(movingMonster, playerBefore, opponentBefore);
            lastVisualRoll = game.getLastRoll() > 0
                    ? game.getLastRoll()
                    : inferVisualDiceRoll(movingPlayer ? playerBefore : opponentBefore,
                    movingMonster.getPosition());
            startDiceRoll(false);
            addTurnHudEvents(movingMonster, movingPlayer, playerSnapshot, opponentSnapshot);
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
                                  MonsterSnapshot playerBefore, MonsterSnapshot opponentBefore) {
        MonsterSnapshot playerAfter = new MonsterSnapshot(game.getPlayer());
        MonsterSnapshot opponentAfter = new MonsterSnapshot(game.getOpponent());
        MonsterSnapshot movingBefore = movingPlayer ? playerBefore : opponentBefore;
        MonsterSnapshot movingAfter = movingPlayer ? playerAfter : opponentAfter;
        MonsterSnapshot otherBefore = movingPlayer ? opponentBefore : playerBefore;
        MonsterSnapshot otherAfter = movingPlayer ? opponentAfter : playerAfter;
        String actor = movingPlayer ? "You" : "Opponent";
        int triggeredCell = game.getLastTriggeredCellIndex();

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

        addTransportEvent(actor, movingBefore, movingAfter, triggeredCell);
        addCellEffectEvent(actor, movingBefore, movingAfter, triggeredCell);

        Card drawnCard = game.getLastDrawnCard();
        if (drawnCard != null) {
            lastCardHighlight = cardHighlightKey(drawnCard);
            addHudEvent("CARD DRAWN", actor + " drew " + drawnCard.getName(),
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

    private void addTransportEvent(String actor, MonsterSnapshot before, MonsterSnapshot after, int triggeredCell) {
        int triggerIndex = triggeredCell >= 0 ? triggeredCell : findTransportTriggerIndex(before.position, after.position);
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

    private void addCellEffectEvent(String actor, MonsterSnapshot before, MonsterSnapshot after, int triggeredCell) {
        if (triggeredCell < 0) {
            return;
        }
        Cell cell = engineCellAt(triggeredCell);
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            int energyDelta = after.energy - before.energy;
            if (energyDelta != 0) {
                addHudEvent(door.getRole() + " DOOR",
                        actor + " " + energyWord(energyDelta) + " " + Math.abs(energyDelta)
                                + " energy on " + door.getName() + " (" + door.getEnergy() + ")",
                        energyDelta > 0 ? new ColorRGBA(0.20f, 0.86f, 1f, 1f)
                                : new ColorRGBA(1f, 0.38f, 0.30f, 1f));
            }
        } else if (cell instanceof MonsterCell) {
            Monster cellMonster = ((MonsterCell) cell).getCellMonster();
            String stationed = cellMonster == null ? cell.getName() : shortMonsterName(cellMonster.getName());
            int energyDelta = after.energy - before.energy;
            String detail;
            if (cellMonster != null && cellMonster.getRole() == before.role) {
                detail = actor + " matched " + stationed + " and activated power";
            } else if (energyDelta != 0) {
                detail = actor + " " + energyWord(energyDelta) + " " + Math.abs(energyDelta) + " energy vs " + stationed;
            } else {
                detail = actor + " faced " + stationed + " with no energy change";
            }
            addHudEvent("MONSTER CELL", detail, new ColorRGBA(0.26f, 1f, 0.78f, 1f));
        }
    }

    private String energyWord(int delta) {
        return delta > 0 ? "gained" : "lost";
    }

    private String shortMonsterName(String name) {
        if (name.contains("Sullivan")) {
            return "Sulley";
        }
        int space = name.indexOf(' ');
        return space > 0 ? name.substring(0, space) : name;
    }

    private String cardHighlightKey(Card card) {
        String type = card.getClass().getSimpleName();
        if ("EnergyStealCard".equals(type)) {
            return "ENERGY STEAL";
        }
        if ("ShieldCard".equals(type)) {
            return "SHIELD";
        }
        if ("SwapperCard".equals(type)) {
            return "SWAPPER";
        }
        if ("StartOverCard".equals(type)) {
            return "START OVER";
        }
        if ("ConfusionCard".equals(type)) {
            return "CONFUSION";
        }
        return "CARD";
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
        return new Vector3f(0f, 14.25f, 9.75f);
    }

    private Vector3f wideCameraTarget() {
        return new Vector3f(0f, 0.55f, -0.35f);
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
