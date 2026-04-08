package me.neonjava.neontpa.managers;

import lombok.Getter;
import me.neonjava.neontpa.NeonTPA;
import me.neonjava.neontpa.gui.TPADialog;
import me.neonjava.neontpa.utils.TPAUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TPAManager {
    @Getter
    private static final TPAManager instance = new TPAManager();

    private final Map<UUID, UUID> tpaRequests = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> tpaHereRequests = new ConcurrentHashMap<>();
    private final Set<UUID> tpaDisabled = ConcurrentHashMap.newKeySet();
    private final Set<UUID> tpahereDisabled = ConcurrentHashMap.newKeySet();
    private final Set<UUID> autoAccept = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BukkitTask> activeTeleports = new ConcurrentHashMap<>();

    public void sendRequest(Player origin, Player target, boolean here) {
        if (origin.equals(target)) {
            origin.sendRichMessage("<red>You cannot teleport to yourself!");
            return;
        }

        if (here) {
            if (isTpaHereDisabled(target.getUniqueId())) {
                origin.sendRichMessage("<red>This player has TPA-Here disabled.");
                return;
            }
            tpaHereRequests.put(target.getUniqueId(), origin.getUniqueId());
        } else {
            if (isTpaDisabled(target.getUniqueId())) {
                origin.sendRichMessage("<red>This player has TPA disabled.");
                return;
            }
            tpaRequests.put(target.getUniqueId(), origin.getUniqueId());
        }

        // Auto accept
        if (autoAccept.contains(target.getUniqueId())) {
            accept(target, origin.getName());
            return;
        }

        origin.sendRichMessage("<green>Teleport request sent to <white>" + target.getName() + ".");

        // Chat notification with click to accept
        target.sendRichMessage("\n<gold><bold>TP REQUEST <white>from <yellow>" + origin.getName());
        target.sendRichMessage("<white>wants to teleport to " + (here ? "them" : "you") + ".");
        target.sendMessage(Component.text("§7[ §a§lACCEPT §7] §7[ §c§lDENY §7]")
                .clickEvent(ClickEvent.runCommand("/tpaccept " + origin.getName())));

        // Modern Dialog API
        TPADialog.showRequest(target, origin, here);
    }

    public void cancelRequest(Player origin) {
        tpaRequests.entrySet().removeIf(entry -> entry.getValue().equals(origin.getUniqueId()));
        tpaHereRequests.entrySet().removeIf(entry -> entry.getValue().equals(origin.getUniqueId()));
        origin.sendRichMessage("<red>Outgoing teleport requests cancelled.");
    }

    public void accept(Player target, String originName) {
        Player origin = Bukkit.getPlayer(originName);
        if (origin == null) {
            target.sendRichMessage("<red>Player is no longer online.");
            return;
        }

        UUID originId = origin.getUniqueId();
        boolean isHere = tpaHereRequests.getOrDefault(target.getUniqueId(), UUID.randomUUID()).equals(originId);
        boolean isTo = tpaRequests.getOrDefault(target.getUniqueId(), UUID.randomUUID()).equals(originId);

        if (!isHere && !isTo) {
            target.sendRichMessage("<red>No pending request from <white>" + originName + ".");
            return;
        }

        if (isHere)
            tpaHereRequests.remove(target.getUniqueId());
        else
            tpaRequests.remove(target.getUniqueId());

        startTeleport(isHere ? target : origin, isHere ? origin : target);
    }

    private void startTeleport(Player transport, Player anchor) {
        if (activeTeleports.containsKey(transport.getUniqueId())) {
            transport.sendRichMessage("<red>Teleport initiation already in progress!");
            return;
        }

        // Safety checks per user request: "if player is flying says unsafe area"
        if (anchor.isFlying() || !TPAUtils.isSafe(anchor.getLocation())) {
            transport.sendRichMessage("<red>UNCANCELLED Error: Unsafe Area Detected! (Flying or Dangerous blocks)");
            anchor.sendRichMessage("<red>Teleport safety check failed! (You are either flying or in a dangerous spot)");
            return;
        }

        transport.sendRichMessage("<yellow>Teleporting in 5 seconds... <gray>Don't move!");
        anchor.sendRichMessage("<yellow>" + transport.getName() + " is teleporting to you in 5 seconds...");

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(NeonTPA.getPlugin(), new Runnable() {
            int countdown = 5;
            final Location startLoc = transport.getLocation();

            @Override
            public void run() {
                if (!transport.isOnline() || !anchor.isOnline()) {
                    cancelTeleport(transport.getUniqueId());
                    return;
                }

                // Movement check
                if (transport.getLocation().distanceSquared(startLoc) > 0.05) {
                    transport.sendRichMessage("<red>Movement detected! Teleport cancelled.");
                    anchor.sendRichMessage("<red>" + transport.getName() + " moved! Teleport cancelled.");
                    cancelTeleport(transport.getUniqueId());
                    return;
                }

                if (countdown <= 0) {
                    transport.teleport(anchor.getLocation());
                    transport.sendRichMessage("<green>Teleport completed!");
                    anchor.sendRichMessage("<green>" + transport.getName() + " has arrived!");
                    cancelTeleport(transport.getUniqueId());
                    return;
                }

                transport.sendActionBar(Component.text("§eTeleporting in §b" + countdown + "s... §7(Don't move)"));
                countdown--;
            }
        }, 0, 20);

        activeTeleports.put(transport.getUniqueId(), task);
    }

    public void cancelTeleport(UUID uuid) {
        BukkitTask task = activeTeleports.remove(uuid);
        if (task != null)
            task.cancel();
    }

    public boolean toggleTpa(UUID uuid) {
        if (tpaDisabled.contains(uuid)) {
            tpaDisabled.remove(uuid);
            return true;
        } else {
            tpaDisabled.add(uuid);
            return false;
        }
    }

    public boolean toggleTpaHere(UUID uuid) {
        if (tpahereDisabled.contains(uuid)) {
            tpahereDisabled.remove(uuid);
            return true;
        } else {
            tpahereDisabled.add(uuid);
            return false;
        }
    }

    public boolean toggleAutoAccept(UUID uuid) {
        if (autoAccept.contains(uuid)) {
            autoAccept.remove(uuid);
            return false;
        } else {
            autoAccept.add(uuid);
            return true;
        }
    }

    public boolean isTpaDisabled(UUID uuid) {
        return tpaDisabled.contains(uuid);
    }

    public boolean isTpaHereDisabled(UUID uuid) {
        return tpahereDisabled.contains(uuid);
    }
}
