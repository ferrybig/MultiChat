package me.ferry.bukkit.plugins.multichat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;

/**
 *
 * @author ferrybig
 */
public class ChatFormatContainer {

    private volatile Map<UUID, String> chatFormats = Collections.emptyMap();
    private final Map<UUID, String> chatFormatsBukkitThread = new HashMap<>();
    private final MultiChat plugin;

    public ChatFormatContainer(MultiChat plugin) {
        Validate.notNull(plugin, "Plugin may not be null");
        this.plugin = plugin;
    }

    @Nonnull
    public Map<UUID, String> getFormat() {
        return chatFormats;
    }

    public void updateAll() {
        for (UUID id : new ArrayList<>(chatFormatsBukkitThread.keySet())) {
            updatePlayer(id);
        }
    }

    public void updatePlayer(UUID id) {
        Validate.notNull(id, "id may not be null");
        Player pl = this.plugin.getServer().getPlayer(id);
        if (pl == null) {
            removePlayer(id);
        } else {
            String format = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("format", "+p+dn+s: +m"));
            if (format.contains("+p")) {
                format = format.replace("+p", getPrefix(pl));
            }
            if (format.contains("+s")) {
                format = format.replace("+s", getSuffix(pl));
            }
            if (format.contains("+dn")) {
                format = format.replace("+dn", pl.getDisplayName());
            }
            if (format.contains("+m")) {
                format = format.replace("+m", "%2$s");
            }
            this.chatFormatsBukkitThread.put(id, format);
        }
    }

    public void removePlayer(UUID player) {
        Validate.notNull(player, "id may not be null");
        this.chatFormatsBukkitThread.remove(player);
    }

    public void pushUpdate() {
        this.chatFormats = new HashMap<>(this.chatFormatsBukkitThread);
    }

    private CharSequence getPrefix(Permissible pl) {
        return getGroupProperty(pl, "prefix");
    }

    private CharSequence getSuffix(Permissible pl) {
        return getGroupProperty(pl, "suffix");
    }

    private String getGroupProperty(Permissible player, String prop) {
        Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
        for (PermissionAttachmentInfo perm : perms) {
            if (perm.getPermission().startsWith("group.") && perm.getValue()) {
                String group = perm.getPermission().substring(6);
                String property = this.plugin.getConfig().
                        getString("group." + group + "." + prop, null);
                if (property == null) {
                    continue;
                }
                return ChatColor.translateAlternateColorCodes('&', property);
            }
        }
        return "";
    }
}
