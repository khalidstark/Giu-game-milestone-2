package game.view3d;

import game.engine.Constants;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shader.VarType;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import java.util.HashMap;
import java.util.Map;

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
    private static final String[] MONSTER_CELL_MODELS = {
            "Models/boardcells/monstercells/Mike Wazowski Cell.glb",
            "Models/boardcells/monstercells/Randall Boggs Cell.glb",
            "Models/boardcells/monstercells/Sulley (James P. Sullivan) Cell.glb",
            "Models/boardcells/monstercells/celia.glb",
            "Models/boardcells/monstercells/fungus.glb",
            "Models/boardcells/monstercells/yeti.glb"
    };

    private static final float MIN_DIST = 6.5f;
    private static final float MAX_DIST = 13.2f;
    private static final float MIN_PITCH = 24f * FastMath.DEG_TO_RAD;
    private static final float MAX_PITCH = 68f * FastMath.DEG_TO_RAD;

    private static final float INTRO_TIME = 3.0f;
    private static final float INTRO_START_YAW = -36f * FastMath.DEG_TO_RAD;
    private static final float INTRO_START_PITCH = 64f * FastMath.DEG_TO_RAD;
    private static final float INTRO_START_DIST = 14.2f;
    private static final float ORBIT_START_YAW = 42f * FastMath.DEG_TO_RAD;
    private static final float ORBIT_START_PITCH = 48f * FastMath.DEG_TO_RAD;
    private static final float ORBIT_START_DIST = 10.8f;

    private enum CameraMode { INTRO, ORBIT }

    private final Vector3f cameraTarget = new Vector3f(0f, 0.85f, 0f);
    private CameraMode cameraMode = CameraMode.INTRO;
    private boolean dragging;
    private float introTimer;
    private float yaw = INTRO_START_YAW;
    private float pitch = INTRO_START_PITCH;
    private float distance = INTRO_START_DIST;

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
        loadBoardFootprint();
        setupLights();
        setupInput();
        updateCamera();
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
        Node boardNode = new Node("BoardFootprint");
        Map<String, CellTemplate> templates = loadBoardCellTemplates();

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
            boardNode.attachChild(cell);
        }

        attachDoorProps(boardNode, templates.get(CELL_DOOR));
        rootNode.attachChild(boardNode);
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
            return MONSTER_CELL_MODELS[monsterSlot];
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
        inputManager.addMapping("Drag", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("OrbitLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("OrbitRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("OrbitUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("OrbitDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("ZoomIn", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("ZoomOut", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("ResetCamera", new KeyTrigger(KeyInput.KEY_R));

        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {
            if ("Drag".equals(name)) {
                dragging = isPressed;
            } else if ("ResetCamera".equals(name) && isPressed) {
                resetCamera();
            }
        }, "Drag", "ResetCamera");

        AnalogListener analog = (name, value, tpf) -> {
            if (cameraMode != CameraMode.ORBIT) {
                return;
            }

            float orbitSpeed = 3.0f;
            float zoomSpeed = 18.0f;
            if ("OrbitLeft".equals(name) && dragging) {
                yaw -= value * orbitSpeed;
            } else if ("OrbitRight".equals(name) && dragging) {
                yaw += value * orbitSpeed;
            } else if ("OrbitUp".equals(name) && dragging) {
                pitch = clamp(pitch + value * orbitSpeed, MIN_PITCH, MAX_PITCH);
            } else if ("OrbitDown".equals(name) && dragging) {
                pitch = clamp(pitch - value * orbitSpeed, MIN_PITCH, MAX_PITCH);
            } else if ("ZoomIn".equals(name)) {
                distance = clamp(distance - value * zoomSpeed, MIN_DIST, MAX_DIST);
            } else if ("ZoomOut".equals(name)) {
                distance = clamp(distance + value * zoomSpeed, MIN_DIST, MAX_DIST);
            }
            updateCamera();
        };
        inputManager.addListener(analog,
                "OrbitLeft", "OrbitRight", "OrbitUp", "OrbitDown", "ZoomIn", "ZoomOut");
    }

    private void resetCamera() {
        cameraMode = CameraMode.ORBIT;
        introTimer = INTRO_TIME;
        yaw = ORBIT_START_YAW;
        pitch = ORBIT_START_PITCH;
        distance = ORBIT_START_DIST;
        updateCamera();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (cameraMode == CameraMode.INTRO) {
            introTimer += tpf;
            float t = easeInOut(clamp(introTimer / INTRO_TIME, 0f, 1f));
            yaw = lerp(INTRO_START_YAW, ORBIT_START_YAW, t);
            pitch = lerp(INTRO_START_PITCH, ORBIT_START_PITCH, t);
            distance = lerp(INTRO_START_DIST, ORBIT_START_DIST, t);
            if (introTimer >= INTRO_TIME) {
                cameraMode = CameraMode.ORBIT;
                yaw = ORBIT_START_YAW;
                pitch = ORBIT_START_PITCH;
                distance = ORBIT_START_DIST;
            }
            updateCamera();
        }
    }

    private void updateCamera() {
        pitch = clamp(pitch, MIN_PITCH, MAX_PITCH);
        distance = clamp(distance, MIN_DIST, MAX_DIST);

        float horizontal = distance * FastMath.cos(pitch);
        float x = cameraTarget.x + horizontal * FastMath.sin(yaw);
        float y = cameraTarget.y + distance * FastMath.sin(pitch);
        float z = cameraTarget.z + horizontal * FastMath.cos(yaw);

        cam.setLocation(new Vector3f(x, y, z));
        cam.lookAt(cameraTarget, Vector3f.UNIT_Y);
        cam.setFrustumPerspective(43f, cam.getWidth() / (float) cam.getHeight(), 0.1f, 80f);
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
