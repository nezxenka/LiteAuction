package ru.nezxenka.liteauction.frontend.menus.market.menus;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.nezxenka.liteauction.LiteAuction;
import ru.nezxenka.liteauction.api.events.market.compile.MarketSellItemAddEvent;
import ru.nezxenka.liteauction.api.events.market.compile.PostMarketCompileEvent;
import ru.nezxenka.liteauction.api.events.market.compile.PreMarketCompileEvent;
import ru.nezxenka.liteauction.backend.config.ConfigManager;
import ru.nezxenka.liteauction.backend.config.Pair;
import ru.nezxenka.liteauction.backend.config.utils.ConfigUtils;
import ru.nezxenka.liteauction.backend.config.utils.PlaceholderUtils;
import ru.nezxenka.liteauction.backend.enums.CategoryType;
import ru.nezxenka.liteauction.backend.enums.MarketSortingType;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;
import ru.nezxenka.liteauction.backend.utils.format.Parser;
import ru.nezxenka.liteauction.backend.utils.format.Formatter;
import ru.nezxenka.liteauction.backend.utils.tags.TagUtil;
import ru.nezxenka.liteauction.frontend.menus.abst.AbstractMenu;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Getter
public class Main extends AbstractMenu {
    @Setter
    private MarketSortingType sortingType;
    @Setter
    private CategoryType categoryType;
    @Setter
    private HashSet<String> filters;
    @Setter
    private int page;
    private String player;
    private HashMap<Integer, SellItem> items = new HashMap<>();

    public Main(int page){
        this.sortingType = MarketSortingType.CHEAPEST_FIRST;
        this.categoryType = CategoryType.ALL;
        filters = new HashSet<>();
        this.page = page;
    }

    public Main compile(){
        try {
            PreMarketCompileEvent preEvent = new PreMarketCompileEvent(viewer, page, player, sortingType, categoryType, filters);
            LiteAuction.getEventManager().triggerEvent(preEvent);
            if(preEvent.isCancelled()){
                return this;
            }
            this.page = preEvent.getPage();
            this.player = preEvent.getTarget();
            this.sortingType = preEvent.getSortingType();
            this.categoryType = preEvent.getCategoryType();
            this.filters = preEvent.getFilters();

            items.clear();
            List<Integer> slots = ConfigUtils.getSlots("design/menus/market/main.yml", "active-items.slot");
            int slotIndex = 0;
            int itemCount = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItemsCount(player, sortingType, filters, categoryType).get();
            List<SellItem> items = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItems(player, sortingType, filters, categoryType, page, slots.size()).get();
            int slotsCount = slots.size();
            int pages = itemCount / slotsCount + (itemCount % slotsCount == 0 ? 0 : 1);

            PostMarketCompileEvent postEvent = new PostMarketCompileEvent(viewer, items, page, player, sortingType, categoryType, filters);
            LiteAuction.getEventManager().triggerEvent(postEvent);
            if(postEvent.isCancelled()){
                return this;
            }
            items = postEvent.getItems();

            inventory = ConfigUtils.buildInventory(this, "design/menus/market/main.yml", "inventory-type",
                    PlaceholderUtils.replace(
                            ConfigManager.getString("design/menus/market/main.yml", "gui-title", "&0Аукцион (%current_page%/%pages_amount%)"),
                            true,
                            new Pair<>("%current_page%", String.valueOf(page)),
                            new Pair<>("%pages_amount%", String.valueOf(pages))
                    )
            );
            for(int i = 0; i < items.size() && slotIndex < slotsCount; i++) {
                int slot = slots.get(slotIndex);
                SellItem sellItem = items.get(i);

                MarketSellItemAddEvent event = new MarketSellItemAddEvent(sellItem, inventory, slotIndex);
                LiteAuction.getEventManager().triggerEvent(event);
                if(event.isCancelled()){
                    slotIndex++;
                    continue;
                }

                this.items.put(slot, sellItem);
                ItemStack itemStack = sellItem.decodeItemStack();
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();
                if(itemMeta != null && itemMeta.getLore() != null){
                    lore = itemMeta.getLore();
                }
                lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.main").stream().map(s -> PlaceholderUtils.replace(
                        s,
                        true,
                        new Pair<>("%categories%", String.join(ConfigManager.getString("design/menus/main.yml", "category-splitter", "&f, &x&0&0&D&8&F&F"), TagUtil.getItemCategories(sellItem.getTags()))),
                        new Pair<>("%seller%", sellItem.getPlayer()),
                        new Pair<>("%expirytime%", Formatter.getTimeUntilExpiration(sellItem)),
                        new Pair<>("%price%", String.valueOf(sellItem.getPrice())),
                        new Pair<>("%full_price%", String.valueOf(sellItem.getPrice() * sellItem.getAmount())),
                        new Pair<>("%format:price%", Formatter.formatPrice(sellItem.getPrice())),
                        new Pair<>("%format:full_price%", Formatter.formatPrice(sellItem.getPrice() * sellItem.getAmount()))
                )).toList());
                if(sellItem.isByOne()){
                    lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.by-one").stream().map(s -> PlaceholderUtils.replace(
                            s,
                            true
                    )).toList());
                }
                if(sellItem.getPlayer().equalsIgnoreCase(viewer.getName())){
                    lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.seller").stream().map(s -> PlaceholderUtils.replace(
                            s,
                            true
                    )).toList());
                }
                else{
                    lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.buy").stream().map(s -> PlaceholderUtils.replace(
                            s,
                            true
                    )).toList());
                    if(!sellItem.isByOne()) {
                        lore.addAll(ConfigManager.getStringList("design/menus/market/main.yml", "active-items.lore.buy-by-one").stream().map(s -> PlaceholderUtils.replace(
                                s,
                                true
                        )).toList());
                    }
                }
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                itemStack.setAmount(sellItem.getAmount());
                inventory.setItem(slot, itemStack);
                slotIndex++;
            }

            if(true){
                int item_count = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getPlayerItemsCount(viewer.getName()).get();
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "on-sell-items",
                        "&x&0&0&D&8&F&F ❏ Товары на продаже ❏",
                        new Pair<>("%item_count%", String.valueOf(item_count))
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                int item_count = LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().getPlayerItemsCount(viewer.getName()).get();
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "unsold-items",
                        "&x&0&0&D&8&F&F ❏ Просроченные товары ❏",
                        new Pair<>("%item_count%", String.valueOf(item_count))
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "update",
                        "&x&0&0&D&8&F&F⇵ &x&D&5&D&B&D&CОбновить аукцион"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "prev-page",
                        "&x&0&0&D&8&F&F◀ Предыдущая страница"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "switch",
                        "&x&0&0&D&8&F&F Помощь по аукциону"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "next-page",
                        "&6Следующая страница ▶"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                Pair<ItemStack, Integer> entry = ConfigUtils.buildItem(
                        "design/menus/market/main.yml",
                        "help",
                        "&x&0&0&D&8&F&F Помощь по системе аукциона:"
                );
                inventory.setItem(entry.getRight(), entry.getLeft());
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.valueOf(ConfigManager.getString(
                        "design/menus/market/main.yml",
                        "sorting.material",
                        "HOPPER"
                )));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color(ConfigManager.getString(
                        "design/menus/market/main.yml",
                        "sorting.displayname",
                        "&x&0&0&D&8&F&F Сортировка"
                )));
                List<String> lore = new ArrayList<>();
                for(MarketSortingType sortingType : MarketSortingType.values()){
                    if(this.sortingType == sortingType){
                        lore.add(Parser.color(ConfigManager.getString(
                                "design/menus/market/main.yml",
                                "sorting.prefix.selected",
                                "&o&6&6✔&6 &6"
                        ) + sortingType.getDisplayName()));
                    }
                    else{
                        lore.add(Parser.color(Parser.color(ConfigManager.getString(
                                "design/menus/market/main.yml",
                                "sorting.prefix.unselected",
                                "&o&x&9&C&F&9&F&F● &x&D&5&D&B&D&C"
                        ) + sortingType.getDisplayName())));
                    }
                }
                itemMeta.setLore(lore);
                if(ConfigManager.getBoolean(
                        "design/menus/market/main.yml",
                        "sorting.glow",
                        false
                )){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(ConfigManager.getInt(
                        "design/menus/market/main.yml",
                        "sorting.slot",
                        52
                ), itemStack);
            }
            if(true){
                ItemStack itemStack = new ItemStack(Material.valueOf(ConfigManager.getString(
                        "design/menus/market/main.yml",
                        "category.material",
                        "CHEST_MINECART"
                )));
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(Parser.color(ConfigManager.getString(
                        "design/menus/market/main.yml",
                        "category.displayname",
                        "&x&0&0&D&8&F&F Категории предметов"
                )));
                List<String> lore = new ArrayList<>();
                for(CategoryType categoryType : CategoryType.values()){
                    if(this.categoryType == categoryType){
                        lore.add(Parser.color(ConfigManager.getString(
                                "design/menus/market/main.yml",
                                "category.prefix.selected",
                                "&o&6&6✔&6 &6"
                        ) + categoryType.getDisplayName()));
                    }
                    else{
                        lore.add(Parser.color(Parser.color(ConfigManager.getString(
                                "design/menus/market/main.yml",
                                "category.prefix.unselected",
                                "&o&x&9&C&F&9&F&F● &f"
                        ) + categoryType.getDisplayName())));
                    }
                }
                itemMeta.setLore(lore);
                if(ConfigManager.getBoolean(
                        "design/menus/market/main.yml",
                        "category.glow",
                        false
                )){
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                itemStack.setItemMeta(itemMeta);
                inventory.setItem(ConfigManager.getInt(
                        "design/menus/market/main.yml",
                        "category.slot",
                        53
                ), itemStack);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Main setTarget(String player){
        this.player = player;
        return this;
    }
}
