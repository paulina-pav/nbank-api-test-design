package api.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }

        String envKey = switch (key) {
            case "server" -> "APIBASEURL";
            case "ui.baseUrl" -> "UIBASEURL";
            case "ui.remote" -> "UI_REMOTE";
            case "browser" -> "BROWSER";
            case "browser.size" -> "BROWSER_SIZE";
            case "browser.holdOpen" -> "BROWSER_HOLD_OPEN";
            default -> key.replace(".", "_").toUpperCase();
        };

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return INSTANCE.properties.getProperty(key);
    }
}
