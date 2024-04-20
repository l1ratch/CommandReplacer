package l1ratch.commandreplacer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CommandReplacer extends JavaPlugin {

    private List<CustomCommand> customCommands;

    @Override
    public void onEnable() {
        // Инициализация конфига и загрузка кастомных команд
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        loadCustomCommands();

        // Регистрация слушателя команд с высоким приоритетом
        getCommand("commandreplacer").setExecutor(new CommandBlockerCommand(this));
    }

    private void loadCustomCommands() {
        customCommands = new ArrayList<>();
        for (String commandKey : getConfig().getConfigurationSection("commands").getKeys(false)) {
            String aliasesKey = "commands." + commandKey + ".aliases";
            List<String> aliases = getConfig().getStringList(aliasesKey);
            String text = getConfig().getString("commands." + commandKey + ".text");
            if (text != null) {
                text = text.replace('&', '\u00A7'); // Замена символа & на §
            }
            List<String> commandList = getConfig().getStringList("commands." + commandKey + ".command");
            customCommands.add(new CustomCommand(aliases, text, commandList));
        }
    }

    public String getCustomCommandResponse(String commandLabel) {
        for (CustomCommand customCommand : customCommands) {
            if (customCommand.getAliases().contains(commandLabel.toLowerCase())) {
                return customCommand.getText();
            }
        }
        return null;
    }

    public List<String> getCustomCommandActions(String commandLabel) {
        for (CustomCommand customCommand : customCommands) {
            if (customCommand.getAliases().contains(commandLabel.toLowerCase())) {
                return customCommand.getCommands();
            }
        }
        return null;
    }
}

class CustomCommand {
    private List<String> aliases;
    private String text;
    private List<String> commands;

    public CustomCommand(List<String> aliases, String text, List<String> commands) {
        this.aliases = aliases;
        this.text = text;
        this.commands = commands;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getText() {
        return text;
    }

    public List<String> getCommands() {
        return commands;
    }
}

class CommandBlockerCommand implements org.bukkit.command.CommandExecutor {

    private CommandReplacer plugin;

    public CommandBlockerCommand(CommandReplacer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("commandreplacer.reload")) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /commandreplacer reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("commandreplacer.reload")) {
                sender.sendMessage("You don't have permission to reload the configuration.");
                return true;
            }
            plugin.reloadConfig();
            plugin.loadCustomCommands();
            sender.sendMessage("Configuration reloaded.");
            return true;
        }

        return false;
    }
}
