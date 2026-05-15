package game.view2d;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

final class SoundManager2D {

    static final String THEME = "2d/audio/theme-song.mp3";
    static final String BUTTON = "2d/audio/ui/button.m4a";
    static final String LOBBY_CHARACTER_SCREAM = "2d/audio/ui/lobby-character-scream.m4a";
    static final String POWERUP = "2d/audio/board/powerup.m4a";
    static final String CELL_CONTAMINATION_SOCK = "2d/audio/board/cell-contamination-sock.m4a";
    static final String DOOR_SCARER = "2d/audio/board/door-scarer.m4a";
    static final String DOOR_LAUGHER = "2d/audio/board/door-laugher.m4a";
    static final String MONSTER_CELL_SCARER = "2d/audio/board/monster-cell-scarer.m4a";
    static final String MONSTER_CELL_LAUGHER = "2d/audio/board/monster-cell-laugher.m4a";

    private final ClassLoader loader;
    private final double themeVolume;
    private final List<MediaPlayer> activeSfx = new ArrayList<>();
    private MediaPlayer themePlayer;
    private int pausingSfxCount;
    private boolean themeWanted;

    SoundManager2D(ClassLoader loader, double themeVolume) {
        this.loader = loader;
        this.themeVolume = themeVolume;
    }

    void startTheme() {
        themeWanted = true;
        if (pausingSfxCount > 0) {
            return;
        }
        MediaPlayer player = themePlayer();
        if (player == null) {
            return;
        }
        try {
            player.play();
        } catch (Exception ignored) {
        }
    }

    void stopTheme() {
        themeWanted = false;
        if (themePlayer == null) {
            return;
        }
        try {
            themePlayer.stop();
        } catch (Exception ignored) {
        }
    }

    void playSfx(String resourcePath) {
        playSfx(resourcePath, false, 1.0);
    }

    void playSfx(String resourcePath, double rate) {
        playSfx(resourcePath, false, rate);
    }

    void playSfxPausingTheme(String resourcePath) {
        playSfx(resourcePath, true, 1.0);
    }

    void dispose() {
        stopTheme();
        if (themePlayer != null) {
            try {
                themePlayer.dispose();
            } catch (Exception ignored) {
            } finally {
                themePlayer = null;
            }
        }
        List<MediaPlayer> players = new ArrayList<>(activeSfx);
        activeSfx.clear();
        pausingSfxCount = 0;
        for (MediaPlayer player : players) {
            try {
                player.stop();
                player.dispose();
            } catch (Exception ignored) {
            }
        }
    }

    private void playSfx(String resourcePath, boolean pauseTheme, double rate) {
        URL url = loader.getResource(resourcePath);
        if (url == null) {
            return;
        }
        MediaPlayer player = null;
        try {
            player = new MediaPlayer(new Media(url.toExternalForm()));
            player.setRate(rate > 0 && Double.isFinite(rate) ? rate : 1.0);
            activeSfx.add(player);
            if (pauseTheme) {
                pausingSfxCount++;
                pauseThemeForSfx();
            }
            MediaPlayer sfxPlayer = player;
            Runnable cleanup = () -> finishSfx(sfxPlayer, pauseTheme);
            player.setOnEndOfMedia(cleanup);
            player.setOnError(cleanup);
            player.play();
        } catch (Exception ignored) {
            if (player != null) {
                activeSfx.remove(player);
                try {
                    player.dispose();
                } catch (Exception ignoredAgain) {
                }
            }
            if (pauseTheme) {
                pausingSfxCount = Math.max(0, pausingSfxCount - 1);
                resumeThemeAfterSfx();
            }
        }
    }

    private MediaPlayer themePlayer() {
        if (themePlayer != null) {
            return themePlayer;
        }
        URL url = loader.getResource(THEME);
        if (url == null) {
            return null;
        }
        try {
            themePlayer = new MediaPlayer(new Media(url.toExternalForm()));
            themePlayer.setCycleCount(MediaPlayer.INDEFINITE);
            themePlayer.setVolume(themeVolume);
            return themePlayer;
        } catch (Exception ignored) {
            themePlayer = null;
            return null;
        }
    }

    private void pauseThemeForSfx() {
        if (themePlayer == null) {
            return;
        }
        try {
            themePlayer.pause();
        } catch (Exception ignored) {
        }
    }

    private void resumeThemeAfterSfx() {
        if (pausingSfxCount > 0 || !themeWanted) {
            return;
        }
        MediaPlayer player = themePlayer();
        if (player == null) {
            return;
        }
        try {
            player.play();
        } catch (Exception ignored) {
        }
    }

    private void finishSfx(MediaPlayer player, boolean pauseTheme) {
        if (!activeSfx.remove(player)) {
            return;
        }
        try {
            player.stop();
            player.dispose();
        } catch (Exception ignored) {
        }
        if (pauseTheme) {
            pausingSfxCount = Math.max(0, pausingSfxCount - 1);
            resumeThemeAfterSfx();
        }
    }
}
