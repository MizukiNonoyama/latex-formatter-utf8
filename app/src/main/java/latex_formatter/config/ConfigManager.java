package latex_formatter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import latex_formatter.Main;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public final class ConfigManager {
    private static ConfigManager instance = null;

    private Config config;

    private ConfigManager() {
        this.load();
    }

    private void load() {
        String configDir = "";
        try {
            Path executionPath = getApplicationPath(Main.class);
            String fileName = executionPath.getFileName().toFile().toString();
            if (fileName.contains(".jar")) {
                configDir = executionPath.getParent().toFile() + File.separator;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            File configFile = new File(configDir + "config.json");
            boolean exist = !configFile.createNewFile();
            if (!exist) {
                this.config = new Config();
                JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(configFile)));
                writer.setIndent("  ");
                gson.toJson(gson.toJsonTree(this.config), writer);
                writer.close();
            } else {
                JsonReader reader = new JsonReader(new BufferedReader(new FileReader(configFile)));
                this.config = gson.fromJson(reader, Config.class);
                reader.close();
            }
        } catch(IOException exception) {
            exception.getStackTrace();
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public static Path getApplicationPath(Class<?> cls) throws URISyntaxException {
        ProtectionDomain pd = cls.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL location = cs.getLocation();
        URI uri = location.toURI();
        Path path = Paths.get(uri);
        return path;
    }
}
