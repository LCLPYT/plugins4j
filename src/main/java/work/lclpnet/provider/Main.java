package work.lclpnet.provider;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        final var dir = Path.of("plugins");
        System.out.println(dir.toAbsolutePath());
    }
}