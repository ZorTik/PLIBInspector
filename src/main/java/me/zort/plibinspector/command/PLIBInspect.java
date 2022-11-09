package me.zort.plibinspector.command;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.AbstractStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.zort.commandlib.annotation.Arg;
import me.zort.commandlib.annotation.Command;
import me.zort.commandlib.annotation.CommandMeta;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@CommandMeta(
        description = "Inspects a PLIB packet type.",
        usage = "/plibinspect inspect <packet type enum> | /plibinspect list"
)
public class PLIBInspect implements TabCompleter {

    private final boolean debug;

    public PLIBInspect(boolean debug) {
        this.debug = debug;
    }

    @Command("/plibinspect inspect {type}")
    public void inspect(CommandSender sender, @Arg("type") String type) {
        PacketType.fromName(type).forEach(t -> reportInfo(sender, t));
    }

    @Command("/plibinspect list")
    public void list(CommandSender sender) {
        PacketType.values().forEach(t -> {
            sender.sendMessage(t.name());
        });
    }

    private void reportInfo(CommandSender sender, PacketType type) {
        AbstractStructure c = new PacketContainer(type);
        Class<? extends AbstractStructure> aClass = c.getClass();
        sender.sendMessage(String.format("Report for %s (%s):", type.name(), type.isClient() ? "Client" : "Server"));
        List<String> globals = new ArrayList<>();
        List<String> specific = new ArrayList<>();
        for(Method method : aClass.getMethods()) {
            if(method.getReturnType().equals(StructureModifier.class)
            && method.getParameterCount() == 0) {
                try {
                    StructureModifier<?> modifier = (StructureModifier<?>) method.invoke(c);
                    int count = (int) modifier.getClass().getMethod("size").invoke(modifier);
                    if(count > 0) {
                        String reportLine = String.format("  %s: %s", method.getName(), count);
                        if(method.getName().equals("getStructures") || method.getName().contains("Modifier")) {
                            globals.add(reportLine);
                        } else {
                            specific.add(reportLine);
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    if(debug) {
                        e.printStackTrace();
                    }
                }
            }
        }
        sender.sendMessage(String.format("--  Global (%d) --", globals.size()));
        globals.forEach(sender::sendMessage);
        sender.sendMessage(String.format("--  Specific (%d) --", specific.size()));
        specific.forEach(sender::sendMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {
        if(args.length <= 1) {
            String filter = args.length > 0 ? args[0] : "";
            Iterator<PacketType> typesIterator = PacketType.values().iterator();
            List<String> types = new ArrayList<>();
            while(typesIterator.hasNext()) {
                PacketType type = typesIterator.next();
                if(type.name().startsWith(filter)) {
                    types.add(type.name());
                }
            }
            return types;
        }
        return Collections.emptyList();
    }

}
