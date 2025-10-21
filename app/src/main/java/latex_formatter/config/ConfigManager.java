package latex_formatter.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;

public final class ConfigManager {
    private static ConfigManager instance = null;

    private Config config;

    private ConfigManager() {
        this.load();
    }

    private void load() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            File configFile = new File("config.json");
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
}
