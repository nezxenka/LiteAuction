package ru.nezxenka.liteauction.frontend.commands.impl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.nezxenka.liteauction.backend.utils.Parser;
import ru.nezxenka.liteauction.frontend.commands.SubCommand;

import java.util.List;

public class Help extends SubCommand {
    public Help(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        leaveUsage(player);
    }

    @Override
    public List<String> getTabCompletes(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public int getRequiredArgs() {
        return 0;
    }

    @Override
    public String getRequiredPermission() {
        return "";
    }
}
