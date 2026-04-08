package me.neonjava.neontpa.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.neonjava.neontpa.managers.TPAManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class TPACommand {
    public static void register(Commands commands) {
        // /tpa <player>
        commands.register(
                Commands.literal("tpa")
                        .then(Commands.argument("player", ArgumentTypes.players())
                                .executes(ctx -> {
                                    Player origin = (Player) ctx.getSource().getSender();
                                    if (origin == null)
                                        return Command.SINGLE_SUCCESS;
                                    PlayerSelectorArgumentResolver targetSelector = ctx.getArgument("player",
                                            PlayerSelectorArgumentResolver.class);
                                    Collection<Player> targets = targetSelector.resolve(ctx.getSource());
                                    if (targets.isEmpty())
                                        return Command.SINGLE_SUCCESS;
                                    Player target = targets.iterator().next();
                                    TPAManager.getInstance().sendRequest(origin, target, false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .build());

        // /tpaccept [player]
        commands.register(
                Commands.literal("tpaccept")
                        .then(Commands.argument("player", ArgumentTypes.players())
                                .executes(ctx -> {
                                    Player target = (Player) ctx.getSource().getSender();
                                    PlayerSelectorArgumentResolver originSelector = ctx.getArgument("player",
                                            PlayerSelectorArgumentResolver.class);
                                    Collection<Player> origins = originSelector.resolve(ctx.getSource());
                                    if (origins.isEmpty())
                                        return Command.SINGLE_SUCCESS;
                                    Player origin = origins.iterator().next();
                                    TPAManager.getInstance().accept(target, origin.getName());
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(ctx -> {
                            // Default /tpaccept behavior: pick from metadata or any pending
                            Player target = (Player) ctx.getSource().getSender();
                            List<MetadataValue> meta = target.getMetadata("neontpa_pending");
                            if (!meta.isEmpty()) {
                                String name = meta.get(0).asString();
                                TPAManager.getInstance().accept(target, name);
                            } else {
                                target.sendRichMessage("<red>No pending requests to accept.");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .build());

        // /tpacancel
        commands.register(
                Commands.literal("tpacancel")
                        .executes(ctx -> {
                            Player origin = (Player) ctx.getSource().getSender();
                            TPAManager.getInstance().cancelRequest(origin);
                            return Command.SINGLE_SUCCESS;
                        })
                        .build());

        // /tpauto
        commands.register(
                Commands.literal("tpauto")
                        .executes(ctx -> {
                            Player p = (Player) ctx.getSource().getSender();
                            boolean active = TPAManager.getInstance().toggleAutoAccept(p.getUniqueId());
                            p.sendRichMessage("<yellow>Auto-accept toggled: <white>" + (active ? "ON" : "OFF"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .build());

        // /tpatoggle
        commands.register(
                Commands.literal("tpatoggle")
                        .executes(ctx -> {
                            Player p = (Player) ctx.getSource().getSender();
                            boolean active = TPAManager.getInstance().toggleTpa(p.getUniqueId());
                            p.sendRichMessage("<yellow>Incoming TPA toggled: <white>" + (active ? "ON" : "OFF"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .build());

        // /tpaheretoggle
        commands.register(
                Commands.literal("tpaheretoggle")
                        .executes(ctx -> {
                            Player p = (Player) ctx.getSource().getSender();
                            boolean active = TPAManager.getInstance().toggleTpaHere(p.getUniqueId());
                            p.sendRichMessage("<yellow>Incoming TPAHere toggled: <white>" + (active ? "ON" : "OFF"));
                            return Command.SINGLE_SUCCESS;
                        })
                        .build());
    }
}
