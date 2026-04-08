package me.neonjava.neontpa.listeners;

import io.papermc.paper.event.player.PlayerCustomClickEvent;
import me.neonjava.neontpa.gui.TPADialog;
import me.neonjava.neontpa.managers.TPAManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class TPAListener implements Listener {

    @EventHandler
    public void onDialogClick(PlayerCustomClickEvent e) {
        if (!(e.getCommonConnection() instanceof io.papermc.paper.connection.PlayerGameConnection con))
            return;
        Player target = con.getPlayer();
        if (e.getIdentifier().equals(TPADialog.KEY_ACCEPT)) {
            List<MetadataValue> meta = target.getMetadata("neontpa_pending");
            if (meta.isEmpty())
                return;
            String originName = meta.get(0).asString();
            target.removeMetadata("neontpa_pending", me.neonjava.neontpa.NeonTPA.getPlugin());
            TPAManager.getInstance().accept(target, originName);
        } else if (e.getIdentifier().equals(TPADialog.KEY_DENY)) {
            target.removeMetadata("neontpa_pending", me.neonjava.neontpa.NeonTPA.getPlugin());
            target.sendRichMessage("<red>Request denied.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TPAManager.getInstance().cancelTeleport(e.getPlayer().getUniqueId());
        e.getPlayer().removeMetadata("neontpa_pending", me.neonjava.neontpa.NeonTPA.getPlugin());
    }
}
