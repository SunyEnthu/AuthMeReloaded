package fr.xephi.authme.data.limbo;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.permission.PermissionsManager;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.settings.SpawnLoader;
import fr.xephi.authme.settings.properties.RestrictionSettings;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.inject.Inject;

/**
 * Helper class for the LimboService.
 */
class LimboServiceHelper {

    @Inject
    private SpawnLoader spawnLoader;

    @Inject
    private PermissionsManager permissionsManager;

    @Inject
    private Settings settings;

    /**
     * Creates a LimboPlayer with the given player's details.
     *
     * @param player the player to process
     * @param isRegistered whether the player is registered
     * @return limbo player with the player's data
     */
    LimboPlayer createLimboPlayer(Player player, boolean isRegistered) {
        Location location = spawnLoader.getPlayerLocationOrSpawn(player);
        // For safety reasons an unregistered player should not have OP status after registration
        boolean isOperator = isRegistered && player.isOp();
        boolean flyEnabled = player.getAllowFlight();
        float walkSpeed = player.getWalkSpeed();
        float flySpeed = player.getFlySpeed();
        String playerGroup = permissionsManager.hasGroupSupport()
            ? permissionsManager.getPrimaryGroup(player) : "";
        ConsoleLogger.debug("Limbo: got walk & fly speed {0} and {1} for player `{2}`",
            walkSpeed, flyEnabled, player.getName());

        return new LimboPlayer(location, isOperator, playerGroup, flyEnabled, walkSpeed, flySpeed);
    }

    /**
     * Removes the data that is saved in a LimboPlayer from the player.
     * <p>
     * Note that teleportation on the player is performed by {@link fr.xephi.authme.service.TeleportationService} and
     * changing the permission group is handled by {@link fr.xephi.authme.data.limbo.AuthGroupHandler}.
     *
     * @param player the player to set defaults to
     */
    void revokeLimboStates(Player player) {
        player.setOp(false);
        player.setAllowFlight(false);

        if (!settings.getProperty(RestrictionSettings.ALLOW_UNAUTHED_MOVEMENT)) {
            ConsoleLogger.debug("LimboServiceHelper: Setting speeds to 0 for player `{0}`", player.getName());
            player.setFlySpeed(0.0f);
            player.setWalkSpeed(0.0f);
        }
    }

    /**
     * Merges two existing LimboPlayer instances of a player. Merging is done the following way:
     * <ul>
     *  <li><code>isOperator, allowFlight</code>: true if either limbo has true</li>
     *  <li><code>flySpeed, walkSpeed</code>: maximum value of either limbo player</li>
     *  <li><code>group, location</code>: from old limbo if not empty/null, otherwise from new limbo</li>
     * </ul>
     *
     * @param newLimbo the new limbo player
     * @param oldLimbo the old limbo player
     * @return merged limbo player if both arguments are not null, otherwise the first non-null argument
     */
    LimboPlayer merge(LimboPlayer newLimbo, LimboPlayer oldLimbo) {
        if (newLimbo == null) {
            return oldLimbo;
        } else if (oldLimbo == null) {
            return newLimbo;
        }

        boolean isOperator = newLimbo.isOperator() || oldLimbo.isOperator();
        boolean canFly = newLimbo.isCanFly() || oldLimbo.isCanFly();
        float flySpeed = Math.max(newLimbo.getFlySpeed(), oldLimbo.getFlySpeed());
        float walkSpeed = Math.max(newLimbo.getWalkSpeed(), oldLimbo.getWalkSpeed());
        String group = firstNotEmpty(newLimbo.getGroup(), oldLimbo.getGroup());
        Location location = firstNotNull(oldLimbo.getLocation(), newLimbo.getLocation());

        return new LimboPlayer(location, isOperator, group, canFly, walkSpeed, flySpeed);
    }

    private static String firstNotEmpty(String newGroup, String oldGroup) {
        ConsoleLogger.debug("Limbo merge: new and old perm groups are `{0}` and `{1}`", newGroup, oldGroup);
        if ("".equals(oldGroup)) {
            return newGroup;
        }
        return oldGroup;
    }

    private static Location firstNotNull(Location first, Location second) {
        return first == null ? second : first;
    }
}
