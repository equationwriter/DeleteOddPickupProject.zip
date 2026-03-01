package me.max.deleteoddpickup;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class DeleteOddPickup extends JavaPlugin implements Listener {

    private boolean lettersOnly;
    private boolean stripColorCodes;
    private String deletedMessage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("DeleteOddPickup enabled.");
    }

    private void loadSettings() {
        FileConfiguration cfg = getConfig();
        lettersOnly = cfg.getBoolean("lettersOnly", false);
        stripColorCodes = cfg.getBoolean("stripColorCodes", true);
        deletedMessage = ChatColor.translateAlternateColorCodes('&',
                cfg.getString("deletedMessage", "&cDeleted: odd-length item name."));
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        Item itemEntity = e.getItem();
        ItemStack stack = itemEntity.getItemStack();

        if (shouldDelete(stack)) {
            e.setCancelled(true);
            itemEntity.remove();
            p.sendMessage(deletedMessage);
        }
    }

    private boolean shouldDelete(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String name = nameForCounting(item);
        if (name == null || name.isEmpty()) return false;
        return (countChars(name) % 2) != 0;
    }

    private String nameForCounting(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String dn = meta.getDisplayName();
            if (stripColorCodes) dn = ChatColor.stripColor(dn);
            return dn == null ? null : dn.trim();
        }
        String mat = item.getType().name().toLowerCase().replace('_', ' ');
        String[] parts = mat.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1))
              .append(' ');
        }
        return sb.toString().trim();
    }

    private int countChars(String s) {
        if (lettersOnly) {
            int c = 0;
            for (int i = 0; i < s.length(); i++) {
                if (Character.isLetter(s.charAt(i))) c++;
            }
            return c;
        } else {
            int c = 0;
            for (int i = 0; i < s.length(); i++) {
                if (!Character.isWhitespace(s.charAt(i))) c++;
            }
            return c;
        }
    }
}
