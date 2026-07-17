package frqme.isa.headlib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods for working with player heads and totems.
 */
public final class HeadUtils {

    private HeadUtils() {
    }

    /**
     * Counts totems of undying in a player's inventory (main + armor + offhand).
     *
     * @param player The player to check
     * @return Total totem count
     */
    public static int countTotems(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                count += item.getAmount();
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                count += item.getAmount();
            }
        }
        if (player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
            count += player.getInventory().getItemInOffHand().getAmount();
        }
        return count;
    }

    /**
     * Builds a chat message with a head prefix.
     * Format: HEAD PlayerName: message
     *
     * @param head     The head Component
     * @param playerName The player's name
     * @param message  The chat message
     * @return The formatted Component
     */
    public static Component buildChatMessage(Component head, String playerName, Component message) {
        return head
                .append(Component.text(" " + playerName + ": ", NamedTextColor.WHITE))
                .append(message);
    }

    /**
     * Builds a HUD line with head + X + totem count.
     * Format: HEAD X 5
     *
     * @param head       The head Component
     * @param totemCount Number of totems
     * @return The formatted Component
     */
    public static Component buildHudLine(Component head, int totemCount) {
        return Component.empty()
                .append(head)
                .append(Component.text("  "))
                .append(Component.text("X", NamedTextColor.RED))
                .append(Component.text("  "))
                .append(Component.text(totemCount, NamedTextColor.GOLD));
    }

    /**
     * Gets all online players with their totem counts.
     *
     * @return Map of player name to totem count
     */
    public static Map<String, Integer> getOnlinePlayerTotems(org.bukkit.Server server) {
        Map<String, Integer> map = new HashMap<>();
        for (Player player : server.getOnlinePlayers()) {
            map.put(player.getName(), countTotems(player));
        }
        return map;
    }
}
