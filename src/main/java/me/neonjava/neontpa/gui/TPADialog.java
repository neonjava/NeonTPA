package me.neonjava.neontpa.gui;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import me.neonjava.neontpa.NeonTPA;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class TPADialog {
        public static final Key KEY_ACCEPT = Key.key("neontpa:request/accept");
        public static final Key KEY_DENY = Key.key("neontpa:request/deny");

        public static void showRequest(Player target, Player origin, boolean here) {
                // Store metadata to know who we're accepting in the listener
                target.setMetadata("neontpa_pending", new FixedMetadataValue(NeonTPA.getPlugin(), origin.getName()));

                Dialog dialog = Dialog.create(builder -> builder.empty()
                                .base(DialogBase.builder(Component.text("§6§lTeleport Request"))
                                                .body(List.of(
                                                                DialogBody.plainMessage(Component.text("§f"
                                                                                + origin.getName()
                                                                                + " wants to teleport to "
                                                                                + (here ? "them" : "you") + "."))))
                                                .build())
                                .type(DialogType.confirmation(
                                                ActionButton.builder(Component.text("§a§l✔ Accept"))
                                                                .tooltip(Component.text(
                                                                                "Accept the teleport request from "
                                                                                                + origin.getName()))
                                                                .action(DialogAction.customClick(KEY_ACCEPT, null))
                                                                .build(),
                                                ActionButton.builder(Component.text("§c§l✖ Deny"))
                                                                .tooltip(Component
                                                                                .text("Deny the teleport request from "
                                                                                                + origin.getName()))
                                                                .action(DialogAction.customClick(KEY_DENY, null))
                                                                .build())));

                target.showDialog(dialog);
        }
}
