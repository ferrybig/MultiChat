package me.ferry.bukkit.plugins.multichat;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author ferrybig
 */
public class MultiChat extends PluginCore implements Listener {

    public final ChatFormatContainer chat = new ChatFormatContainer(this);
    private final MultiChatCommand command = new MultiChatCommand(this);

    @Override
    public void onDisable() {
        for (Player pl : this.getServer().getOnlinePlayers()) {
            this.chat.removePlayer(pl.getUniqueId());
        }
    }

    @Override
    public void onEnable(){
        try {
            this.reloadConfigWithErrors();
        } catch (IOException | InvalidConfigurationException ex) {
			this.getLogger().log(Level.SEVERE, "Problem loading config", ex);
            this.getServer().getPluginManager().disablePlugin(this);
            this.setEnabled(false);
            return;
        }
        
        if(!new File(this.getDataFolder(),"config.yml").exists())
        {
            this.saveDefaultConfig();
            this.reloadConfig();
        }
        for (Player pl : this.getServer().getOnlinePlayers()) {
            this.chat.updatePlayer(pl.getUniqueId());
        }
        this.chat.pushUpdate();
        this.getCommand("multichat").setExecutor(command);
        this.getCommand("multichat").setTabCompleter(command);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        String format = chat.getFormat().get(event.getPlayer().getUniqueId());
        if (format != null) {
            event.setFormat(format);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.chat.updatePlayer(event.getPlayer().getUniqueId());
        this.chat.pushUpdate();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        this.chat.removePlayer(event.getPlayer().getUniqueId());
    }
}
