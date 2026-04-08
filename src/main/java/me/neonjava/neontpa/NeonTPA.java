package me.neonjava.neontpa;

import lombok.Getter;
import me.neonjava.neontpa.commands.TPACommand;
import me.neonjava.neontpa.listeners.TPAListener;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

@Getter
public final class NeonTPA extends JavaPlugin {
    @Getter
    private static NeonTPA plugin;

    @Override
    public void onEnable() {
        plugin = this;

        // Register events
        getServer().getPluginManager().registerEvents(new TPAListener(), this);

        // Register commands with new Lifecycle API
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            TPACommand.register(commands);
        });

        getLogger().info("NeonTPA enabled!");
    }
}
