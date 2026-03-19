package helpers.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Tomcat 上で動作する簡易ロガー
 *
 * 特徴:
 * - init不要
 * - Logger.class の配置場所から Web アプリのルートを自動判定
 * - Logs/yyyy-MM-dd.log に追記
 *
 * 想定構成:
 *   <webapp-root>/
 *     ├─ Logs/
 *     └─ WEB-INF/
 *         ├─ classes/
 *         └─ lib/
 *
 * 使用例:
 *   Logger.info("開始しました");
 *   Logger.warn("警告です");
 *   Logger.error("エラーが発生しました", e);
 */
public final class Logger {

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static volatile Path cachedWebRootPath;

    private Logger() {
    }

    public static void info(String message) {
        write("INFO", message, null);
    }

    public static void warn(String message) {
        write("WARN", message, null);
    }

    public static void error(String message) {
        write("ERROR", message, null);
    }

    public static void error(String message, Throwable throwable) {
        write("ERROR", message, throwable);
    }

    /**
     * 現在のログ出力先ファイルを取得します。
     */
    public static Path getCurrentLogFilePath() {
        Path webRoot = resolveWebRootPath();
        String fileName = LocalDate.now().format(FILE_DATE_FORMAT) + ".log";
        return webRoot.resolve("Logs").resolve(fileName);
    }

    /**
     * 判定した Web アプリケーションルートを取得します。
     * これは getServletContext().getRealPath("/") 相当の想定です。
     */
    public static Path getResolvedWebRootPath() {
        return resolveWebRootPath();
    }

    private static synchronized void write(String level, String message, Throwable throwable) {
        String safeMessage = message == null ? "" : message;
        String now = LocalDateTime.now().format(LOG_TIME_FORMAT);

        Path logFile = getCurrentLogFilePath();
        Path logDir = logFile.getParent();

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(now).append("]");
        sb.append("[").append(level).append("]");
        sb.append("[Thread:").append(Thread.currentThread().getName()).append("] ");
        sb.append(safeMessage);
        sb.append(System.lineSeparator());

        if (throwable != null) {
            sb.append(getStackTrace(throwable));
            sb.append(System.lineSeparator());
        }

        try {
            Files.createDirectories(logDir);
            Files.writeString(
                    logFile,
                    sb.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("ログファイルへの書き込みに失敗しました: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * Webアプリのルートを自動判定します。
     *
     * 優先順位:
     * 1. CodeSource から判定
     * 2. ClassLoader resource から判定
     * 3. catalina.base
     * 4. カレントディレクトリ
     */
    private static Path resolveWebRootPath() {
        if (cachedWebRootPath != null) {
            return cachedWebRootPath;
        }

        synchronized (Logger.class) {
            if (cachedWebRootPath != null) {
                return cachedWebRootPath;
            }

            Path resolved = tryResolveFromCodeSource();

            if (resolved == null) {
                resolved = tryResolveFromClassLoader();
            }

            if (resolved == null) {
                String catalinaBase = System.getProperty("catalina.base");
                if (catalinaBase != null && !catalinaBase.isBlank()) {
                    resolved = Paths.get(catalinaBase).toAbsolutePath().normalize();
                }
            }

            if (resolved == null) {
                resolved = Paths.get(".").toAbsolutePath().normalize();
            }

            cachedWebRootPath = resolved;
            return cachedWebRootPath;
        }
    }

    /**
     * Logger.class の CodeSource から Web ルートを推定
     */
    private static Path tryResolveFromCodeSource() {
        try {
            CodeSource codeSource = Logger.class.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                return null;
            }

            Path location = Paths.get(codeSource.getLocation().toURI())
                    .toAbsolutePath()
                    .normalize();

            return resolveWebRootFromLocation(location);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ClassLoader resource から Web ルートを推定
     */
    private static Path tryResolveFromClassLoader() {
        try {
            URL url = Logger.class.getClassLoader().getResource("");
            if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
                return null;
            }

            Path location = Paths.get(url.toURI())
                    .toAbsolutePath()
                    .normalize();

            return resolveWebRootFromLocation(location);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 配置場所から Web アプリルートを推定
     *
     * 対応例:
     * - .../WEB-INF/classes
     * - .../WEB-INF/lib/xxx.jar
     */
    private static Path resolveWebRootFromLocation(Path location) {
        if (location == null) {
            return null;
        }

        String fileName = location.getFileName() != null ? location.getFileName().toString() : "";

        // .../WEB-INF/classes
        if (Files.isDirectory(location) && "classes".equals(fileName)) {
            Path webInf = location.getParent();
            if (webInf != null && webInf.getFileName() != null
                    && "WEB-INF".equals(webInf.getFileName().toString())) {
                Path webRoot = webInf.getParent();
                if (webRoot != null) {
                    return webRoot.toAbsolutePath().normalize();
                }
            }
        }

        // .../WEB-INF/lib/xxx.jar
        if (fileName.endsWith(".jar")) {
            Path libDir = location.getParent();
            if (libDir != null && libDir.getFileName() != null
                    && "lib".equals(libDir.getFileName().toString())) {
                Path webInf = libDir.getParent();
                if (webInf != null && webInf.getFileName() != null
                        && "WEB-INF".equals(webInf.getFileName().toString())) {
                    Path webRoot = webInf.getParent();
                    if (webRoot != null) {
                        return webRoot.toAbsolutePath().normalize();
                    }
                }
            }
        }

        return null;
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}