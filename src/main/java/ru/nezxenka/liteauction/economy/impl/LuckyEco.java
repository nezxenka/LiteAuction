package ru.nezxenka.liteauction.economy.impl;

import org.bukkit.Bukkit;
import ru.nezxenka.liteauction.economy.EconomyEditor;
import ru.nezxenka.luckyeco.LuckyEcoAPI;

public class LuckyEco extends EconomyEditor {
    private LuckyEcoAPI economy;

    public LuckyEco(){
        setupEconomy();
    }

    private void setupEconomy(){
        if (Bukkit.getServer().getPluginManager().getPlugin("StickEco") == null) {
            return;
        }
        economy = new LuckyEcoAPI();
    }

    @Override
    public double getBalance(String player) {
        if (economy == null) return 0;
        return economy.getBalance(player.toLowerCase());
    }

    @Override
    public void addBalance(String player, double count) {
        if (economy == null || count <= 0) return;
        economy.addBalance(player.toLowerCase(), count);
    }

    @Override
    public void subtractBalance(String player, double count) {
        if (economy == null || count <= 0) return;
        economy.subtractBalance(player.toLowerCase(), count);
    }
}