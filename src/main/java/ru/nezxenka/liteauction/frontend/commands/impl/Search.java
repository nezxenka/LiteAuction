package ru.nezxenka.liteauction.frontend.commands.impl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.liteauction.LiteAuction;
import ru.nezxenka.liteauction.backend.config.ConfigManager;
import ru.nezxenka.liteauction.backend.enums.AuctionType;
import ru.nezxenka.liteauction.backend.storage.models.GuiData;
import ru.nezxenka.liteauction.backend.utils.ItemNameUtil;
import ru.nezxenka.liteauction.backend.utils.format.Parser;
import ru.nezxenka.liteauction.backend.utils.tags.TagUtil;
import ru.nezxenka.liteauction.frontend.commands.SubCommand;

import java.util.*;

public class Search extends SubCommand {
    public Search(String name) {
        super(name);
    }

    @Override
    public void execute(Player player, Command command, String[] args) {
        ItemNameUtil.loadTranslationsIfNeeded();

        if(args.length < 2) {
            ItemStack itemStack = player.getItemInHand() == null || player.getItemInHand().getType().isAir() ? player.getInventory().getItemInOffHand() : player.getItemInHand();
            if(itemStack == null || itemStack.getType().isAir()){
                player.sendMessage(Parser.color(
                        ConfigManager.getString("design/commands/search.yml", "air-search", "&#FB2222▶ &fПо вашему запросу не найдено ни одного фильтра.")
                ));
                return;
            }
            try {
                GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
                if(guiData.getAuctionType() == AuctionType.MARKET || !ConfigManager.getBoolean("settings/settings.yml", "enable-bids", true)) {
                    ru.nezxenka.liteauction.frontend.menus.market.menus.Main main = new ru.nezxenka.liteauction.frontend.menus.market.menus.Main(1);
                    main.setPlayer(player);
                    main.setSortingType(guiData.getMarketSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.setFilters(TagUtil.getPartialTags(itemStack));
                    main.compile().open();
                }
                else {
                    ru.nezxenka.liteauction.frontend.menus.bids.menus.Main main = new ru.nezxenka.liteauction.frontend.menus.bids.menus.Main(1);
                    main.setPlayer(player);
                    main.setSortingType(guiData.getBidsSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.compile().open();
                    main.setFilters(TagUtil.getPartialTags(itemStack));
                }
            } catch (Exception e) {
                player.sendMessage(Parser.color(
                        ConfigManager.getString("design/commands/main.yml", "error", "&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия.")
                ));
            }
        }
        else{
            String find = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if(!ItemNameUtil.containsTag(find)){
                player.sendMessage(Parser.color(
                        ConfigManager.getString("design/commands/search.yml", "air-search", "&#FB2222▶ &fПо вашему запросу не найдено ни одного фильтра.")
                ));
                return;
            }
            try {
                GuiData guiData = LiteAuction.getInstance().getDatabaseManager().getGuiDatasManager().getOrDefault(player.getName()).get();
                if(guiData.getAuctionType() == AuctionType.MARKET || !ConfigManager.getBoolean("settings/settings.yml", "enable-bids", true)) {
                    ru.nezxenka.liteauction.frontend.menus.market.menus.Main main = new ru.nezxenka.liteauction.frontend.menus.market.menus.Main(1);
                    main.setPlayer(player);
                    main.setSortingType(guiData.getMarketSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.setFilters(ItemNameUtil.escapeTag(find));
                    main.compile().open();
                }
                else {
                    ru.nezxenka.liteauction.frontend.menus.bids.menus.Main main = new ru.nezxenka.liteauction.frontend.menus.bids.menus.Main(1);
                    main.setPlayer(player);
                    main.setSortingType(guiData.getBidsSortingType());
                    main.setCategoryType(guiData.getCategoryType());
                    main.setFilters(ItemNameUtil.escapeTag(find));
                    main.compile().open();
                }
            } catch (Exception e) {
                player.sendMessage(Parser.color(
                        ConfigManager.getString("design/commands/main.yml", "error", "&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия.")
                ));
            }
        }
    }

    @Override
    public List<String> getTabCompletes(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return List.of();
        }

        ItemNameUtil.loadTranslationsIfNeeded();
        String all = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).toLowerCase();

        List<String> allTags = new ArrayList<>();
        List<String> completions = new ArrayList<>();
        allTags.addAll(ItemNameUtil.getReverseTranslations().keySet());
        allTags.addAll(ItemNameUtil.getCustomTags().keySet());

        try {
            for (int i = 0; i < allTags.size(); i++) {
                String current = allTags.get(i);
                int startIndex = all.length() - args[args.length - 1].length();

                if (startIndex < 0) {
                    startIndex = 0;
                } else if (startIndex > current.length()) {
                    startIndex = current.length();
                }

                if(current.toLowerCase().startsWith(all.toLowerCase())) {
                    completions.add(current.substring(startIndex).startsWith(" ") ? current.substring(startIndex + 1) : current.substring(startIndex));
                }
            }
        } catch (Exception e) {
            return List.of();
        }

        return completions;
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
