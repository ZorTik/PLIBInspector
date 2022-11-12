package me.zort.plibinspector.command;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.AbstractStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import me.zort.commandlib.annotation.Arg;
import me.zort.commandlib.annotation.Command;
import me.zort.commandlib.annotation.CommandMeta;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
        String[] parts = type.split("/");
        String typeString = parts[0];
        String[] accessPath = (String[]) ArrayUtils.subarray(parts, 1, parts.length);
        PacketType.fromName(typeString).forEach(t -> handleInspection(sender, t, accessPath));
    }

    @Command("/plibinspect list")
    public void list(CommandSender sender) {
        PacketType.values().forEach(t -> {
            sender.sendMessage(t.name());
        });
    }

    private void handleInspection(CommandSender sender, PacketType type, String[] accessPath) {
        String inputName = String.format("%s/%s", type.name(), String.join("/", accessPath));
        sender.sendMessage(String.format("Inspecting %s...", inputName));
        if(accessPath.length == 0) {
            reportInfo(sender, type);
        } else {
            AbstractStructure current = new PacketContainer(type);
            for (String methodName : accessPath) {
                Class<? extends AbstractStructure> clazz = current.getClass();
                try {
                    Method method = clazz.getMethod(methodName);
                    Object modifierCandidate = method.invoke(current);
                    String errorMessage = null;
                    if(modifierCandidate instanceof StructureModifier) {
                        StructureModifier<?> modifier = (StructureModifier<?>) modifierCandidate;
                        modifier.writeDefaults();
                        List<?> structuresStorage = modifier.getValues();
                        Object structure;
                        if(structuresStorage.size() > 0 && (structure = structuresStorage.get(0)) instanceof AbstractStructure) {
                            current = (AbstractStructure) structure;
                        } else {
                            errorMessage = "There is no present structure in the modifier.";
                        }
                    } else {
                        errorMessage = "Method " + methodName + " does not return a structure modifier!";
                    }
                    Optional.ofNullable(errorMessage)
                            .ifPresent(s -> {
                                throw new RuntimeException(s);
                            });
                } catch (Exception e) {
                    sender.sendMessage("An Error occured on inspection: " + e.getMessage());
                    return;
                }
            }
            reportInfo(sender, current, inputName);
        }
    }

    private void reportInfo(CommandSender sender, PacketType type) {
        reportInfo(sender, new PacketContainer(type), String.format("%s (%s)", type.name(), type.isClient() ? "Client" : "Server"));
    }

    private void reportInfo(CommandSender sender, AbstractStructure c, String structureName) {
        Class<? extends AbstractStructure> aClass = c.getClass();
        sender.sendMessage(String.format("Report for %s:", structureName));
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
