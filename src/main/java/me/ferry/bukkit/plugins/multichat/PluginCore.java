package me.ferry.bukkit.plugins.multichat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Fernando
 */
public class PluginCore extends JavaPlugin {

    private final String configFileName = "config.yml";
    private FileConfiguration newConfig = null;
    private File configFile = null;

    @Override
    public FileConfiguration getConfig() {
        if (newConfig == null) {
            reloadConfig();
        }
        return newConfig;
    }

    public FileConfiguration getConfigWithErrors()
            throws IOException, InvalidConfigurationException {
        if (newConfig == null) {
            reloadConfigWithErrors();
        }
        return newConfig;
    }

    @Override
    public void reloadConfig() {
        boolean errorState = false;
        try {
            this.reloadConfigWithErrors();
        } catch (FileNotFoundException ex) {
        } catch (IOException | InvalidConfigurationException ex) {
            this.getLogger().log(Level.SEVERE, "Cannot load " + configFile, ex);
            errorState = true;
        }
        if (errorState && (configFile.canRead()) && (configFile.canWrite())) {
            getLogger().warning(
                    "Broken configuration detected! backing up configuration!");
            if (configFile.renameTo(
                    new File(getDataFolder(), "config-error.yml"))) {
                this.getLogger().warning("Backed up to: config-error.yml");
            } else {
                this.getLogger().warning("Based on the plugin, "
                        + "you may have lost the configuration now!");
            }
        }
    }

    public void reloadConfigWithErrors()
            throws IOException, InvalidConfigurationException {
        if (this.configFile == null) {
            this.configFile = new File(this.getDataFolder(), 
                    this.configFileName);
        }
        newConfig = new YamlConfiguration();
        if (this.configFile.exists()) {
            newConfig.load(configFile);
        }
        InputStream defConfigStream = getResource(this.configFileName);
        if (defConfigStream != null) {
            newConfig.setDefaults(
                    YamlConfiguration.loadConfiguration(
							new InputStreamReader(defConfigStream)));
        }
    }

    @Override
    public void saveConfig() {
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE,
                    "Could not save config to " + configFile, ex);
        }
    }

    @Override
    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile
                    = new File(this.getDataFolder(), this.configFileName);
        }
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    private String prefix;

    public final void sendMessage(CommandSender sender, String msg) {
        if (this.prefix == null) {
            this.prefix = getConfig().getString("chattag","[FerryChat]");
            this.prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        }
        sender.sendMessage(this.prefix + msg);
    }

}
