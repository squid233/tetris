package io.github.squid233.tetris;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class FileUtil {
    public static String loadString(String filename) {
        try (var reader = new BufferedReader(
            new InputStreamReader(
                Objects.requireNonNull(FileUtil.class.getClassLoader().getResourceAsStream(filename))
            )
        )) {
            final StringBuilder sb = new StringBuilder(512);
            String read = reader.readLine();
            if (read != null) {
                sb.append(read);
            }
            while ((read = reader.readLine()) != null) {
                sb.append('\n').append(read);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + filename, e);
        }
    }
}
