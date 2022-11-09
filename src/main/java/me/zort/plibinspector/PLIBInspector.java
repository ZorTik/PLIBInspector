package me.zort.plibinspector;

import me.zort.commandlib.CommandLib;
import me.zort.commandlib.CommandLibBuilder;
import me.zort.plibinspector.command.PLIBInspect;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PLIBInspector extends JavaPlugin {

    private static PLIBInspector INSTANCE;

    public static PLIBInspector get() {
        return INSTANCE;
    }

    private CommandLib commands;
    private boolean debug;

    @Override
    public void onEnable() {
        INSTANCE = this;

        if(!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        this.debug = getConfig().getBoolean("debug", false);
        this.commands = new CommandLibBuilder(this)
                .withMapping(new PLIBInspect(debug))
                .register();
    }

    @Override
    public void onDisable() {
        this.commands.unregisterAll();
    }

}
