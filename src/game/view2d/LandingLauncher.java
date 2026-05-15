package game.view2d;

import javafx.application.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LandingLauncher {

    public static void main(String[] args) throws Exception {
        // JavaFX does NOT need -XstartOnFirstThread — it manages its own Cocoa thread.
        // jME/LWJGL/GLFW DOES need -XstartOnFirstThread, so we launch it as a subprocess.
        Application.launch(LandingPage.class, args);

        String role = LandingPage.getSelectedRole();
        String mode = LandingPage.getSelectedMode();
        if (role != null) {
            launchGame(role, mode == null ? "3d" : mode);
        }
        System.exit(0);
    }

    private static void launchGame(String role, String mode) throws IOException {
        String javaExe = ProcessHandle.current().info().command()
                .orElseGet(() -> System.getProperty("java.home") + "/bin/java");
        String cp = System.getProperty("java.class.path");

        List<String> cmd = new ArrayList<>();
        cmd.add(javaExe);
        if ("3d".equalsIgnoreCase(mode)) {
            cmd.add("-XstartOnFirstThread");
        } else {
            addJavaFxRuntimeArgs(cmd, cp);
        }
        cmd.add("-cp");
        cmd.add(cp);
        cmd.add("--add-opens"); cmd.add("java.base/java.lang=ALL-UNNAMED");
        cmd.add("--add-opens"); cmd.add("java.desktop/sun.awt=ALL-UNNAMED");
        cmd.add("--add-opens"); cmd.add("java.desktop/sun.java2d=ALL-UNNAMED");
        cmd.add("2d".equalsIgnoreCase(mode) ? "game.view2d.Board2DApp" : "game.view3d.FactoryShellApp");
        cmd.add("--role=" + role);

        new ProcessBuilder(cmd).inheritIO().start();
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
}
