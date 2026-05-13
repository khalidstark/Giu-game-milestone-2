package game.view2d;

import javafx.application.Application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LandingLauncher {

    public static void main(String[] args) throws Exception {
        // JavaFX does NOT need -XstartOnFirstThread — it manages its own Cocoa thread.
        // jME/LWJGL/GLFW DOES need -XstartOnFirstThread, so we launch it as a subprocess.
        Application.launch(LandingPage.class, args);

        String role = LandingPage.getSelectedRole();
        if (role != null) {
            launchGame(role);
        }
        System.exit(0);
    }

    private static void launchGame(String role) throws IOException {
        String javaExe = ProcessHandle.current().info().command()
                .orElseGet(() -> System.getProperty("java.home") + "/bin/java");
        String cp = System.getProperty("java.class.path");

        List<String> cmd = new ArrayList<>();
        cmd.add(javaExe);
        cmd.add("-XstartOnFirstThread");
        cmd.add("-cp");
        cmd.add(cp);
        cmd.add("--add-opens"); cmd.add("java.base/java.lang=ALL-UNNAMED");
        cmd.add("--add-opens"); cmd.add("java.desktop/sun.awt=ALL-UNNAMED");
        cmd.add("--add-opens"); cmd.add("java.desktop/sun.java2d=ALL-UNNAMED");
        cmd.add("game.view3d.FactoryShellApp");
        cmd.add("--role=" + role);

        new ProcessBuilder(cmd).inheritIO().start();
    }
}
