package net.joshb.deathmessages.api;

import net.joshb.deathmessages.DeathMessages;
import net.joshb.deathmessages.config.Settings;
import net.joshb.deathmessages.config.UserData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerManager {
    private static final List<PlayerManager> players = new ArrayList<>();
    private final Player p;
    private final UUID uuid;
    private final String name;
    private final String displayName;
    public boolean saveUserData = Settings.getInstance().getConfig().getBoolean("Saved-User-Data");
    private boolean messagesEnabled;
    private boolean isBlacklisted;
    private DamageCause damageCause;
    private Entity lastEntityDamager;
    private Entity lastExplosiveEntity;
    private Projectile lastProjectileEntity;
    private Material climbing;
    private Location explosionCauser;
    private Location location;
    private int cooldown = 0;
    private BukkitTask cooldownTask;
    private Inventory cachedInventory;
    private BukkitTask lastEntityTask;

    public PlayerManager(Player p) {
        this.p = p;
        this.uuid = p.getUniqueId();
        this.name = p.getName();
        this.displayName = p.getDisplayName();
        if (saveUserData && !UserData.getInstance().getConfig().contains(p.getUniqueId().toString())) {
            UserData.getInstance().getConfig().set(p.getUniqueId() + ".username", p.getName());
            UserData.getInstance().getConfig().set(p.getUniqueId() + ".messages-enabled", true);
            UserData.getInstance().getConfig().set(p.getUniqueId() + ".is-blacklisted", false);
            UserData.getInstance().save();
        }
        if (saveUserData) {
            messagesEnabled = UserData.getInstance().getConfig().getBoolean(p.getUniqueId() + ".messages-enabled");
            isBlacklisted = UserData.getInstance().getConfig().getBoolean(p.getUniqueId() + ".is-blacklisted");
        } else {
            messagesEnabled = true;
            isBlacklisted = false;
        }
        players.add(this);
    }

    public static PlayerManager getPlayer(Player p) {
        for (PlayerManager pm : players) {
            if (pm.getUUID().equals(p.getUniqueId()))
                return pm;
        }
        return null;
    }

    public static PlayerManager getPlayer(UUID uuid) {
        for (PlayerManager pm : players) {
            if (pm.getUUID().equals(uuid))
                return pm;
        }
        return null;
    }

    public Player getPlayer() {
        return Objects.requireNonNull(p);
    }

    public UUID getUUID() {
        return Objects.requireNonNull(uuid);
    }

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public boolean getMessagesEnabled() {
        return messagesEnabled;
    }

    public void setMessagesEnabled(boolean b) {
        messagesEnabled = b;
        if (saveUserData) {
            UserData.getInstance().getConfig().set(this.p.getUniqueId() + ".messages-enabled", b);
            UserData.getInstance().save();
        }
    }

    public boolean isBlacklisted() {
        return this.isBlacklisted;
    }

    public void setBlacklisted(boolean b) {
        isBlacklisted = b;
        if (saveUserData) {
            UserData.getInstance().getConfig().set(this.p.getUniqueId() + ".is-blacklisted", b);
            UserData.getInstance().save();
        }
    }

    public void setLastDamageCause(DamageCause dc) {
        damageCause = dc;
    }

    public DamageCause getLastDamage() {
        return damageCause;
    }

    public Entity getLastEntityDamager() {
        return lastEntityDamager;
    }

    public void setLastEntityDamager(Entity e) {
        setLastExplosiveEntity(null);
        setLastProjectileEntity(null);
        this.lastEntityDamager = e;
        if (e == null) return;
        if (lastEntityTask != null) {
            lastEntityTask.cancel();
        }
        lastEntityTask = new BukkitRunnable() {
            @Override
            public void run() {
                setLastEntityDamager(null);
            }
        }.runTaskLater(DeathMessages.plugin, Settings.getInstance().getConfig().getInt("Expire-Last-Damage.Expire-Mob") * 20);
    }

    public Entity getLastExplosiveEntity() {
        return lastExplosiveEntity;
    }

    public void setLastExplosiveEntity(Entity e) {
        lastExplosiveEntity = e;
    }

    public Projectile getLastProjectileEntity() {
        return lastProjectileEntity;
    }

    public void setLastProjectileEntity(Projectile lastProjectileEntity) {
        this.lastProjectileEntity = lastProjectileEntity;
    }

    public Material getLastClimbing() {
        return climbing;
    }

    public void setLastClimbing(Material climbing) {
        this.climbing = climbing;
    }

    public Location getExplosionCauser() {
        return explosionCauser;
    }

    public void setExplosionCauser(Location location) {
        this.explosionCauser = location;
    }

    public Location getLastLocation() {
        return location;
    }

    public void setLastLocation(Location location) {
        this.location = location;
    }

    public boolean isInCooldown() {
        return (cooldown > 0);
    }

    public void setCooldown() {
        cooldown = Settings.getInstance().getConfig().getInt("Cooldown");
        cooldownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (PlayerManager.this.cooldown <= 0)
                    cancel();
                cooldown--;
            }
        }.runTaskTimer(DeathMessages.plugin, 0, 20);
    }

    public Inventory getCachedInventory() {
        return getCachedInventory();
    }

    public void setCachedInventory(Inventory inventory) {
        cachedInventory = inventory;
    }
}
