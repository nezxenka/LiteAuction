package ru.nezxenka.liteauction.backend.config.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.nezxenka.liteauction.backend.config.ConfigManager;
import ru.nezxenka.liteauction.backend.config.Pair;
import ru.nezxenka.liteauction.backend.exceptions.UnsupportedConfigurationException;
import ru.nezxenka.liteauction.backend.utils.format.Parser;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ConfigUtils {
    public static List<Integer> getSlots(String filePath, String path){
        List<Integer> slots = new ArrayList<>();
        switch (ConfigManager.getString(filePath, path + ".mode", "range").toLowerCase()){
            case "single" -> slots.add(ConfigManager.getInt(filePath, path + ".value", 0));
            case "range" -> {
                try {
                    String[] split = ConfigManager.getString(filePath, path + ".value", "0-44").split("-", 2);
                    int from = Integer.parseInt(split[0]);
                    int to = Integer.parseInt(split[1]);
                    for (int i = from; i <= to; i++) {
                        slots.add(i);
                    }
                }
                catch (Exception e){
                    throw new UnsupportedConfigurationException("Unsupported slots value");
                }
            }
            case "array" -> slots.addAll(ConfigManager.getIntList(filePath, path + ".value"));
            default -> throw new UnsupportedConfigurationException("Invalid slots type");
        }
        return slots;
    }

    public static Inventory buildInventory(InventoryHolder holder, String filePath, String path, String title){
        Inventory inventory;
        try{
            InventoryType inventoryType = InventoryType.valueOf(ConfigManager.getString(filePath, path, "54"));
            inventory = Bukkit.createInventory(holder,
                    inventoryType,
                    Parser.color(title)
            );
        } catch (IllegalArgumentException e) {
            inventory = Bukkit.createInventory(holder,
                    ConfigManager.getInt(filePath, path, 54),
                    Parser.color(title)
            );
        }
        return inventory;
    }

    public static Pair<ItemStack, Integer> buildItem(String filePath, String path, String defDisplayname, Pair<String, String>... replaces){
        ItemStack itemStack = new ItemStack(Material.valueOf(ConfigManager.getString(filePath, path + ".material", "CHEST")));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(PlaceholderUtils.replace(
                ConfigManager.getString(filePath, path + ".displayname", defDisplayname),
                true,
                replaces
        ));
        List<String> lore = new ArrayList<>();
        lore.addAll(ConfigManager.getStringList(filePath, path + ".lore").stream().map(s -> PlaceholderUtils.replace(
                s,
                true,
                replaces
        )).toList());
        itemMeta.setLore(lore);
        if(ConfigManager.getBoolean(filePath, ".glow", false)) {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(itemMeta);
        return new Pair<>(itemStack, ConfigManager.getInt(filePath, path + ".slot", 0));
    }
}
