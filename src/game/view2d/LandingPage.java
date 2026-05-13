package game.view2d;

import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Locale;

public class LandingPage extends Application {

    private static volatile String selectedRole = null;

    private static final int W = 1600;
    private static final int H = 900;

    private static final int DOOR_COLS = 4;
    private static final int DOOR_FRAME_W = 640;
    private static final int DOOR_FRAME_H = 640;
    private static final int DOOR_FRAMES = 16;
    private static final long DOOR_FRAME_NS = 63_000_000L;

    private static final SpriteButtonSpec MONSTERS_BUTTON = new SpriteButtonSpec(
            "2d/buttons/monsters.png", 6, 634, 332, 36, 52_000_000L,
            280, 147, 0, 0, 634, 332, Color.web("#C8FFD8"), 0, 0);
    private static final SpriteButtonSpec CARDS_BUTTON = new SpriteButtonSpec(
            "2d/buttons/cards.png", 6, 488, 306, 36, 47_000_000L,
            280, 150, 0, 0, 488, 306, Color.web("#F1D9FF"), 22, 28);
    private static final SpriteButtonSpec SCARER_BUTTON = new SpriteButtonSpec(
            "2d/buttons/scarer.png", 6, 574, 210, 36, 53_000_000L,
            310, 112, 0, 0, 574, 210, Color.web("#FFD1BC"), 22, 0);
    private static final SpriteButtonSpec LAUGHER_BUTTON = new SpriteButtonSpec(
            "2d/buttons/laugher.png", 6, 570, 574, 36, 47_000_000L,
            310, 112, 0, 168, 570, 230, Color.web("#E9FFC1"), 22, 0);
    private static final SpriteButtonSpec LAUNCH_BUTTON = new SpriteButtonSpec(
            "2d/buttons/launch.png", 6, 640, 480, 36, 50_000_000L,
            158, 158, 80, 0, 480, 480, Color.web("#1A1606"), 0, 0);
    private static final SpriteButtonSpec MIKE_SPRITE = new SpriteButtonSpec(
            "2d/buttons/monsters-mike.png", 6, 640, 514, 36, 57_000_000L,
            300, 241, 0, 0, 640, 514, Color.WHITE, 0, 0);
    private static final SpriteButtonSpec HEADER_MONITOR = new SpriteButtonSpec(
            "2d/header-monitor.png", 6, 432, 594, 36, 49_000_000L,
            280, 385, 0, 0, 432, 594, Color.WHITE, 0, 0);

    private static final String[] MONSTER_ASSETS = {
            "2d/monsters/CHR-P01.png",
            "2d/monsters/CHR-P02.png",
            "2d/monsters/CHR-P03.png",
            "2d/monsters/CHR-P04.png",
            "2d/monsters/CHR-P05.png",
            "2d/monsters/CHR-P06.png",
            "2d/monsters/CHR-P07.png",
            "2d/monsters/CHR-P08.png"
    };

    private static final String[] CARD_ASSETS = {
            "2d/cards/CRD-ART01.png",
            "2d/cards/CRD-ART02.png",
            "2d/cards/CRD-ART03.png",
            "2d/cards/CRD-ART04.png",
            "2d/cards/CRD-ART05.png",
            "2d/cards/CRD-ART06.png",
            "2d/cards/CRD-ART07.png",
            "2d/cards/CRD-ART08.png",
            "2d/cards/CRD-ART09.png"
    };

    private static final String[] MONSTER_LABELS = {
            "Monster 01", "Monster 02", "Monster 03", "Monster 04",
            "Monster 05", "Monster 06", "Monster 07", "Monster 08"
    };

    private static final String[] CARD_LABELS = {
            "Energy Steal", "Shield", "Swapper", "Start Over", "Confusion",
            "Card Art 06", "Card Art 07", "Card Art 08", "Card Art 09"
    };

    public static String getSelectedRole() {
        return selectedRole;
    }

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(false);

        Image bg = loadImage("2d/background.png");
        Image doorSheet = loadImage("2d/door-spritesheet.png");
        Image logo = loadImage("2d/logo-door-dash.png");

        Pane root = new Pane();
        root.setPrefSize(W, H);
        root.setStyle("-fx-background-color: #070914;");

        Group designGroup = new Group(root);
        Pane viewport = new Pane(designGroup);
        viewport.setStyle("-fx-background-color: #070914;");

        Canvas canvas = new Canvas(W, H);
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        int[] doorFrame = {0};
        long[] doorLast = {0};

        double doorDisplayW = 720;
        double doorDisplayH = 720;
        double doorX = W / 2.0 - doorDisplayW / 2.0 - 8;
        double doorY = 190;

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - doorLast[0] >= DOOR_FRAME_NS) {
                    doorLast[0] = now;
                    doorFrame[0] = (doorFrame[0] + 1) % DOOR_FRAMES;
                }

                if (bg != null) {
                    gc.drawImage(bg, 0, 0, W, H);
                } else {
                    gc.setFill(Color.web("#080a18"));
                    gc.fillRect(0, 0, W, H);
                }

                gc.setFill(Color.rgb(0, 0, 0, 0.22));
                gc.fillRect(0, 0, W, H);

                if (doorSheet != null) {
                    int col = doorFrame[0] % DOOR_COLS;
                    int row = doorFrame[0] / DOOR_COLS;
                    gc.drawImage(doorSheet,
                            col * DOOR_FRAME_W, row * DOOR_FRAME_H, DOOR_FRAME_W, DOOR_FRAME_H,
                            doorX, doorY, doorDisplayW, doorDisplayH);
                }
            }
        };
        timer.start();

        ImageView logoView = new ImageView();
        if (logo != null) {
            logoView.setImage(logo);
        }
        logoView.setFitWidth(360);
        logoView.setPreserveRatio(true);
        logoView.setLayoutX(42);
        logoView.setLayoutY(32);
        logoView.setEffect(glow(Color.web("#4AD9FF"), 10));

        StackPane headerMonitor = makeLoopingSprite(HEADER_MONITOR);
        headerMonitor.setLayoutX(W / 2.0 - HEADER_MONITOR.displayW / 2.0);
        headerMonitor.setLayoutY(-130);

        VBox rightMenu = new VBox(14);
        rightMenu.setLayoutX(W - 320);
        rightMenu.setLayoutY(330);
        rightMenu.getChildren().addAll(
                makeAnimatedButton(CARDS_BUTTON, false,
                        () -> showGallery(root, "CARDS", CARD_ASSETS, CARD_LABELS, 3, 190, 250))
        );

        StackPane monstersMenuButton = makeAnimatedButton(MONSTERS_BUTTON, false,
                () -> showGallery(root, "MONSTERS", MONSTER_ASSETS, MONSTER_LABELS, 4, 168, 168));
        monstersMenuButton.setLayoutX(38);
        monstersMenuButton.setLayoutY(380);

        StackPane mikeCharacter = makeHoverSprite(MIKE_SPRITE,
                () -> showGallery(root, "MONSTERS", MONSTER_ASSETS, MONSTER_LABELS, 4, 168, 168));
        mikeCharacter.setLayoutX(W / 2.0 - MIKE_SPRITE.displayW / 2.0 - 8);
        mikeCharacter.setLayoutY(540);

        String[] pendingRole = {null};
        StackPane launchButton = makeAnimatedButton(LAUNCH_BUTTON, false, () -> {
            if (pendingRole[0] != null) {
                chooseRole(pendingRole[0]);
            }
        });
        launchButton.setLayoutX(W / 2.0 - LAUNCH_BUTTON.displayW / 2.0);
        launchButton.setLayoutY(612);
        launchButton.setVisible(false);

        HBox roleButtons = new HBox(24);
        roleButtons.setAlignment(Pos.CENTER);
        StackPane scarerButton = makeAnimatedButton(SCARER_BUTTON, true, () ->
                selectPendingRole("scarer", pendingRole, launchButton, roleButtons));
        StackPane laugherButton = makeAnimatedButton(LAUGHER_BUTTON, true, () ->
                selectPendingRole("laugher", pendingRole, launchButton, roleButtons));
        roleButtons.getChildren().addAll(
                scarerButton,
                laugherButton
        );

        VBox roleBox = new VBox(roleButtons);
        roleBox.setAlignment(Pos.CENTER);
        roleBox.setLayoutX(W / 2.0 - 322);
        roleBox.setLayoutY(H - 125);

        root.getChildren().addAll(logoView, headerMonitor, rightMenu,
                monstersMenuButton, mikeCharacter, launchButton, roleBox);

        Scene scene = new Scene(viewport, W, H);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.S) {
                selectPendingRole("scarer", pendingRole, launchButton, roleButtons);
            } else if (e.getCode() == KeyCode.L) {
                selectPendingRole("laugher", pendingRole, launchButton, roleButtons);
            } else if ((e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) && pendingRole[0] != null) {
                chooseRole(pendingRole[0]);
            } else if (e.getCode() == KeyCode.ESCAPE) {
                closeTopOverlay(root);
            }
        });

        stage.setTitle("DooR DasH - Choose Your Role");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(960);
        stage.setMinHeight(540);
        stage.setOnCloseRequest(e -> {
            selectedRole = null;
            Platform.exit();
        });
        stage.setAlwaysOnTop(true);
        stage.show();
        stage.toFront();
        stage.requestFocus();
        Platform.runLater(() -> { stage.toFront(); stage.setAlwaysOnTop(false); });
        viewport.widthProperty().addListener((obs, oldValue, newValue) -> fitDesignToViewport(viewport, designGroup));
        viewport.heightProperty().addListener((obs, oldValue, newValue) -> fitDesignToViewport(viewport, designGroup));
        fitDesignToViewport(viewport, designGroup);
    }

    private StackPane makeAnimatedButton(SpriteButtonSpec spec, boolean autoLoop, Runnable onClick) {
        Image sheet = loadImage(spec.imagePath);
        Canvas sprite = new Canvas(spec.displayW, spec.displayH);
        GraphicsContext buttonGc = sprite.getGraphicsContext2D();
        int[] frame = {0};
        long[] last = {0};

        Runnable draw = () -> drawButtonFrame(buttonGc, sheet, spec, frame[0]);
        draw.run();

        StackPane pane = new StackPane(sprite);
        setButtonSize(pane, spec.displayW, spec.displayH);
        pane.setCursor(Cursor.HAND);

        ScaleTransition hover = new ScaleTransition(Duration.millis(120), pane);
        AnimationTimer animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - last[0] < spec.frameNs) {
                    return;
                }
                last[0] = now;
                if (frame[0] >= spec.frames - 1) {
                    if (autoLoop) {
                        frame[0] = 0;
                    } else {
                        stop();
                    }
                    draw.run();
                    return;
                }
                frame[0]++;
                draw.run();
            }
        };
        Runnable startAnimation = () -> {
            if (autoLoop) {
                return;
            }
            animation.stop();
            frame[0] = 0;
            last[0] = 0;
            draw.run();
            animation.start();
        };

        pane.setOnMouseEntered(e -> {
            startAnimation.run();
            hover.stop();
            hover.setToX(1.04);
            hover.setToY(1.04);
            hover.play();
        });
        pane.setOnMousePressed(e -> startAnimation.run());
        pane.setOnTouchPressed(e -> startAnimation.run());
        pane.setOnMouseClicked(e -> {
            startAnimation.run();
            onClick.run();
        });
        pane.setOnMouseExited(e -> {
            if (!autoLoop) {
                animation.stop();
                frame[0] = 0;
                last[0] = 0;
                draw.run();
            }
            hover.stop();
            hover.setToX(1.0);
            hover.setToY(1.0);
            hover.play();
        });
        if (autoLoop) {
            animation.start();
        }
        return pane;
    }

    private StackPane makeLoopingSprite(SpriteButtonSpec spec) {
        Image sheet = loadImage(spec.imagePath);
        Canvas sprite = new Canvas(spec.displayW, spec.displayH);
        GraphicsContext spriteGc = sprite.getGraphicsContext2D();
        int[] frame = {0};
        long[] last = {0};
        Runnable draw = () -> drawButtonFrame(spriteGc, sheet, spec, frame[0]);
        draw.run();

        StackPane pane = new StackPane(sprite);
        setButtonSize(pane, spec.displayW, spec.displayH);

        AnimationTimer animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - last[0] < spec.frameNs) {
                    return;
                }
                last[0] = now;
                frame[0] = frame[0] >= spec.frames - 1 ? 0 : frame[0] + 1;
                draw.run();
            }
        };
        animation.start();
        return pane;
    }

    private StackPane makeHoverSprite(SpriteButtonSpec spec, Runnable onClick) {
        Image sheet = loadImage(spec.imagePath);
        Canvas sprite = new Canvas(spec.displayW, spec.displayH);
        GraphicsContext spriteGc = sprite.getGraphicsContext2D();
        int[] frame = {0};
        long[] last = {0};
        Runnable draw = () -> drawButtonFrame(spriteGc, sheet, spec, frame[0]);
        draw.run();

        StackPane pane = new StackPane(sprite);
        setButtonSize(pane, spec.displayW, spec.displayH);
        pane.setCursor(Cursor.HAND);

        AnimationTimer animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - last[0] < spec.frameNs) {
                    return;
                }
                last[0] = now;
                frame[0] = frame[0] >= spec.frames - 1 ? 0 : frame[0] + 1;
                draw.run();
            }
        };
        Runnable start = () -> {
            frame[0] = 0;
            last[0] = 0;
            draw.run();
            animation.start();
        };
        Runnable stop = () -> {
            animation.stop();
            frame[0] = 0;
            last[0] = 0;
            draw.run();
        };
        pane.setOnMouseEntered(e -> start.run());
        pane.setOnMouseExited(e -> stop.run());
        pane.setOnMousePressed(e -> start.run());
        pane.setOnTouchPressed(e -> start.run());
        pane.setOnMouseClicked(e -> onClick.run());
        return pane;
    }

    private void selectPendingRole(String role, String[] pendingRole, StackPane launchButton, HBox roleButtons) {
        pendingRole[0] = role;
        launchButton.setVisible(true);
        for (int i = 0; i < roleButtons.getChildren().size(); i++) {
            StackPane button = (StackPane) roleButtons.getChildren().get(i);
            boolean selected = (i == 0 && role.equals("scarer")) || (i == 1 && role.equals("laugher"));
            Color accent = i == 0 ? Color.web("#FF905C") : Color.web("#D7FF67");
            button.setStyle(selected
                    ? "-fx-background-color: rgba(255,255,255,0.06);"
                    + "-fx-border-color: " + toRgba(accent, 0.95) + ";"
                    + "-fx-border-width: 3;"
                    + "-fx-border-radius: 14;"
                    : "");
            button.setEffect(selected ? glow(accent, 20) : null);
        }
    }

    private void fitDesignToViewport(Pane viewport, Group designGroup) {
        double scale = Math.min(viewport.getWidth() / W, viewport.getHeight() / H);
        if (!Double.isFinite(scale) || scale <= 0) {
            scale = 1.0;
        }
        designGroup.getTransforms().setAll(new Scale(scale, scale, 0, 0));
        designGroup.setLayoutX((viewport.getWidth() - W * scale) / 2.0);
        designGroup.setLayoutY((viewport.getHeight() - H * scale) / 2.0);
    }

    private void drawButtonFrame(GraphicsContext gc, Image sheet, SpriteButtonSpec spec, int frame) {
        gc.clearRect(0, 0, spec.displayW, spec.displayH);
        if (sheet == null) {
            gc.setFill(Color.rgb(10, 24, 54, 0.94));
            gc.fillRoundRect(0, 0, spec.displayW, spec.displayH, 22, 22);
            gc.setStroke(spec.labelColor);
            gc.setLineWidth(3);
            gc.strokeRoundRect(0, 0, spec.displayW, spec.displayH, 22, 22);
            return;
        }
        int col = frame % spec.cols;
        int row = frame / spec.cols;
        gc.drawImage(sheet,
                col * spec.frameW + spec.cropX, row * spec.frameH + spec.cropY,
                spec.cropW, spec.cropH,
                0, 0, spec.displayW, spec.displayH);
    }

    private void setButtonSize(StackPane pane, double w, double h) {
        pane.setPrefSize(w, h);
        pane.setMinSize(w, h);
        pane.setMaxSize(w, h);
    }

    private void chooseRole(String role) {
        selectedRole = role;
        Platform.exit();
    }

    private void showGallery(Pane root, String title, String[] imagePaths, String[] labels,
                             int columns, double imageW, double imageH) {
        closeTopOverlay(root);

        StackPane overlay = new StackPane();
        overlay.setId("gallery-overlay");
        overlay.setPrefSize(W, H);
        overlay.setStyle("-fx-background-color: rgba(2, 5, 14, 0.76);");

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(24, 28, 28, 28));
        panel.setMaxSize(960, 690);
        panel.setStyle("-fx-background-color: rgba(10, 18, 35, 0.94);"
                + "-fx-border-color: #5cf5ff; -fx-border-width: 2;"
                + "-fx-effect: dropshadow(gaussian, rgba(92,245,255,0.55), 20, 0.35, 0, 0);");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text heading = styledText(title, 30, Color.web("#F8FBFF"), FontWeight.BOLD);
        heading.setEffect(glow(Color.web("#5CF5FF"), 12));
        StackPane close = makeSmallButton("CLOSE", Color.web("#FFB56E"), () -> root.getChildren().remove(overlay));
        HBox spacer = new HBox();
        spacer.setPrefWidth(650);
        header.getChildren().addAll(heading, spacer, close);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(18);
        grid.setVgap(18);

        for (int i = 0; i < imagePaths.length; i++) {
            VBox tile = galleryTile(imagePaths[i], labels[i], imageW, imageH);
            grid.add(tile, i % columns, i / columns);
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(900, 570);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        panel.getChildren().addAll(header, scroll);
        overlay.getChildren().add(panel);
        root.getChildren().add(overlay);
    }

    private VBox galleryTile(String imagePath, String label, double imageW, double imageH) {
        ImageView image = loadButtonImage(imagePath, imageW, imageH);
        image.setPreserveRatio(true);
        Text caption = styledText(label, 14, Color.web("#DCEBFF"), FontWeight.BOLD);
        VBox tile = new VBox(8, image, caption);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(12));
        tile.setStyle("-fx-background-color: rgba(17, 28, 52, 0.86);"
                + "-fx-border-color: rgba(124, 241, 255, 0.65); -fx-border-width: 1;");
        return tile;
    }

    private StackPane makeSmallButton(String label, Color color, Runnable onClick) {
        Text text = styledText(label, 14, color, FontWeight.BOLD);
        StackPane button = new StackPane(text);
        button.setCursor(Cursor.HAND);
        button.setPrefSize(96, 34);
        button.setStyle("-fx-background-color: rgba(30, 38, 68, 0.92);"
                + "-fx-border-color: " + toRgb(color) + "; -fx-border-width: 1.5;");
        addHoverScale(button, 1.04);
        button.setOnMouseClicked(e -> onClick.run());
        return button;
    }

    private StackPane glassPanel(double w, double h, Color accent) {
        StackPane panel = new StackPane();
        panel.setPrefSize(w, h);
        panel.setMaxSize(w, h);
        panel.setStyle("-fx-background-color: rgba(8, 14, 30, 0.68);"
                + "-fx-border-color: " + toRgb(accent) + "; -fx-border-width: 1.5;");
        return panel;
    }

    private void closeTopOverlay(Pane root) {
        if (!root.getChildren().isEmpty()
                && "gallery-overlay".equals(root.getChildren().get(root.getChildren().size() - 1).getId())) {
            root.getChildren().remove(root.getChildren().size() - 1);
        }
    }

    private void addHoverScale(StackPane pane, double scale) {
        ScaleTransition hover = new ScaleTransition(Duration.millis(120), pane);
        pane.setOnMouseEntered(e -> {
            hover.stop();
            hover.setToX(scale);
            hover.setToY(scale);
            hover.play();
        });
        pane.setOnMouseExited(e -> {
            hover.stop();
            hover.setToX(1.0);
            hover.setToY(1.0);
            hover.play();
        });
    }

    private ImageView loadButtonImage(String resourcePath, double w, double h) {
        Image img = loadImage(resourcePath);
        ImageView iv = img == null ? new ImageView() : new ImageView(img);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(false);
        return iv;
    }

    private Image loadImage(String path) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(path);
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Text styledText(String content, double size, Color color, FontWeight weight) {
        Text t = new Text(content);
        t.setFont(Font.font("System", weight, size));
        t.setFill(color);
        return t;
    }

    private DropShadow glow(Color color, double radius) {
        DropShadow shadow = new DropShadow(radius, color);
        shadow.setInput(new Glow(0.35));
        return shadow;
    }

    private String toRgb(Color color) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

    private String toRgba(Color color, double alpha) {
        return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.3f)",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255),
                alpha);
    }

    private static final class SpriteButtonSpec {
        final String imagePath;
        final int cols;
        final int frameW;
        final int frameH;
        final int frames;
        final long frameNs;
        final double displayW;
        final double displayH;
        final double cropX;
        final double cropY;
        final double cropW;
        final double cropH;
        final Color labelColor;
        final double fontSize;
        final double labelOffsetY;

        SpriteButtonSpec(String imagePath, int cols, int frameW, int frameH, int frames, long frameNs,
                         double displayW, double displayH,
                         double cropX, double cropY, double cropW, double cropH,
                         Color labelColor, double fontSize, double labelOffsetY) {
            this.imagePath = imagePath;
            this.cols = cols;
            this.frameW = frameW;
            this.frameH = frameH;
            this.frames = frames;
            this.frameNs = frameNs;
            this.displayW = displayW;
            this.displayH = displayH;
            this.cropX = cropX;
            this.cropY = cropY;
            this.cropW = cropW;
            this.cropH = cropH;
            this.labelColor = labelColor;
            this.fontSize = fontSize;
            this.labelOffsetY = labelOffsetY;
        }
    }
}
