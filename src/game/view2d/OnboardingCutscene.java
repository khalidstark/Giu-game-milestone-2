package game.view2d;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.net.URL;

final class OnboardingCutscene {

    private static final String VIDEO_RESOURCE = "2d/onboarding/finalversiononboarding.mp4";

    private final double width;
    private final double height;
    private final Pane view;
    private final MediaPlayer mediaPlayer;
    private final MediaView mediaView;

    private Runnable onFinished = () -> {};
    private boolean finished;

    OnboardingCutscene(ClassLoader loader, double width, double height) {
        this.width = width;
        this.height = height;
        this.view = new Pane();
        this.view.setPrefSize(width, height);
        this.view.setMinSize(width, height);
        this.view.setMaxSize(width, height);
        this.view.setStyle("-fx-background-color: #02050E;");
        this.view.getChildren().add(new Rectangle(width, height, Color.web("#02050E")));

        URL url = loader.getResource(VIDEO_RESOURCE);
        if (url == null) {
            this.mediaPlayer = null;
            this.mediaView = null;
            return;
        }

        MediaPlayer player = null;
        MediaView playerView = null;
        try {
            Media media = new Media(url.toExternalForm());
            player = new MediaPlayer(media);
            playerView = new MediaView(player);
            playerView.setFitWidth(width);
            playerView.setFitHeight(height);
            playerView.setPreserveRatio(false);
            player.setOnReady(() -> configureCoverViewport(media));
            player.setOnEndOfMedia(this::finish);
            player.setOnError(this::finish);
            view.getChildren().add(playerView);
        } catch (Exception ignored) {
            player = null;
            playerView = null;
        }
        this.mediaPlayer = player;
        this.mediaView = playerView;
    }

    Pane getView() {
        return view;
    }

    void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished == null ? () -> {} : onFinished;
    }

    void play() {
        if (finished) {
            return;
        }
        if (mediaPlayer == null) {
            finish();
            return;
        }
        try {
            mediaPlayer.seek(Duration.ZERO);
            mediaPlayer.play();
        } catch (Exception ignored) {
            finish();
        }
    }

    void dispose() {
        if (mediaPlayer == null) {
            return;
        }
        try {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        } catch (Exception ignored) {
        }
    }

    private void configureCoverViewport(Media media) {
        if (mediaView == null) {
            return;
        }
        double mediaW = media.getWidth();
        double mediaH = media.getHeight();
        if (mediaW <= 0 || mediaH <= 0) {
            return;
        }

        double targetRatio = width / height;
        double mediaRatio = mediaW / mediaH;
        double cropX = 0;
        double cropY = 0;
        double cropW = mediaW;
        double cropH = mediaH;

        if (mediaRatio > targetRatio) {
            cropW = mediaH * targetRatio;
            cropX = (mediaW - cropW) / 2.0;
        } else {
            cropH = mediaW / targetRatio;
            cropY = (mediaH - cropH) / 2.0;
        }

        mediaView.setViewport(new Rectangle2D(cropX, cropY, cropW, cropH));
    }

    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        dispose();
        Runnable complete = onFinished::run;
        if (Platform.isFxApplicationThread()) {
            complete.run();
        } else {
            Platform.runLater(complete);
        }
    }
}
