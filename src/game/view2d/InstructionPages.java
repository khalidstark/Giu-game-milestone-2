package game.view2d;

import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.InputStream;
import java.util.prefs.Preferences;

final class InstructionPages {

    private static final String RESOURCE_ROOT = "2d/instructions/";
    private static final String PREF_KEY = "instructions.v1.seen";
    private static final int CARD_COUNT = 7;

    private final ClassLoader loader;
    private final Pane view;
    private final ImageView cardView;
    private final StackPane backButton;
    private final Text nextText;
    private final Image[] cards = new Image[CARD_COUNT];

    private Runnable onFinished = () -> {};
    private int currentIndex;

    InstructionPages(ClassLoader loader, double width, double height) {
        this.loader = loader;
        this.view = new Pane();
        this.view.setPrefSize(width, height);
        this.view.setMinSize(width, height);
        this.view.setMaxSize(width, height);
        this.view.setStyle("-fx-background-color: rgba(2, 5, 14, 0.92);");
        this.view.setFocusTraversable(true);

        loadCards();

        cardView = new ImageView();
        cardView.setFitHeight(850);
        cardView.setFitWidth(478);
        cardView.setPreserveRatio(true);
        cardView.setSmooth(true);
        cardView.setLayoutX((width - 478) / 2.0);
        cardView.setLayoutY(25);
        cardView.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.65)));

        backButton = makeButton("BACK", Color.web("#DCEBFF"));
        backButton.setLayoutX(285);
        backButton.setLayoutY(height - 116);
        backButton.setOnMouseClicked(e -> previous());

        nextText = buttonText("NEXT", Color.web("#FFF4D6"));
        StackPane nextButton = makeButton(nextText);
        nextButton.setLayoutX(width - 445);
        nextButton.setLayoutY(height - 116);
        nextButton.setOnMouseClicked(e -> next());

        view.getChildren().addAll(cardView, backButton, nextButton);
        view.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                next();
            } else if (e.getCode() == KeyCode.LEFT) {
                previous();
            }
        });
        showCard(0);
    }

    static boolean shouldPlay() {
        return !preferences().getBoolean(PREF_KEY, false);
    }

    static void markSeen() {
        preferences().putBoolean(PREF_KEY, true);
    }

    Pane getView() {
        return view;
    }

    void requestFocus() {
        view.requestFocus();
    }

    void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished == null ? () -> {} : onFinished;
    }

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(LandingPage.class);
    }

    private void loadCards() {
        for (int i = 0; i < CARD_COUNT; i++) {
            String path = String.format("%scard-%02d.png", RESOURCE_ROOT, i + 1);
            try (InputStream is = loader.getResourceAsStream(path)) {
                cards[i] = is == null ? null : new Image(is);
            } catch (Exception ignored) {
                cards[i] = null;
            }
        }
    }

    private void previous() {
        if (currentIndex <= 0) {
            return;
        }
        showCard(currentIndex - 1);
    }

    private void next() {
        if (currentIndex >= CARD_COUNT - 1) {
            markSeen();
            onFinished.run();
            return;
        }
        showCard(currentIndex + 1);
    }

    private void showCard(int index) {
        currentIndex = Math.max(0, Math.min(CARD_COUNT - 1, index));
        cardView.setImage(cards[currentIndex]);
        backButton.setVisible(currentIndex > 0);
        backButton.setManaged(currentIndex > 0);
        nextText.setText(currentIndex == CARD_COUNT - 1 ? "START" : "NEXT");
    }

    private StackPane makeButton(String label, Color color) {
        return makeButton(buttonText(label, color));
    }

    private StackPane makeButton(Text text) {
        StackPane button = new StackPane(text);
        button.setPrefSize(160, 54);
        button.setMinSize(160, 54);
        button.setMaxSize(160, 54);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: rgba(18, 22, 32, 0.92);"
                + "-fx-border-color: #FF9F10; -fx-border-width: 2;"
                + "-fx-background-radius: 14; -fx-border-radius: 14;");
        button.setEffect(new DropShadow(18, Color.rgb(255, 159, 16, 0.35)));
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.04);
            button.setScaleY(1.04);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        return button;
    }

    private Text buttonText(String label, Color color) {
        Text text = new Text(label);
        text.setFill(color);
        text.setFont(Font.font("System", FontWeight.BLACK, 20));
        text.setEffect(new DropShadow(8, Color.rgb(255, 159, 16, 0.48)));
        return text;
    }
}
