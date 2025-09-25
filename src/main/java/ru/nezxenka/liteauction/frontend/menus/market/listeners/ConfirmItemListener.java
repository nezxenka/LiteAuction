package ru.nezxenka.liteauction.frontend.menus.market.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.liteauction.LiteAuction;
import ru.nezxenka.liteauction.api.events.market.buy.BuySellItemEvent;
import ru.nezxenka.liteauction.backend.config.ConfigManager;
import ru.nezxenka.liteauction.backend.config.utils.ConfigUtils;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;
import ru.nezxenka.liteauction.backend.utils.format.Formatter;
import ru.nezxenka.liteauction.backend.utils.tags.ItemHoverUtil;
import ru.nezxenka.liteauction.backend.utils.format.Parser;
import ru.nezxenka.liteauction.frontend.menus.abst.AbstractListener;
import ru.nezxenka.liteauction.frontend.menus.market.menus.ConfirmItem;
import ru.nezxenka.liteauction.frontend.menus.market.menus.Main;

import java.util.Optional;

import static ru.nezxenka.liteauction.LiteAuction.addItemInventory;

public class ConfirmItemListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof ConfirmItem confirmItem) {
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if (ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "approve.slot").contains(slot)) {
                    Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(confirmItem.getSellItem().getId()).get();
                    if (sellItemOptional.isEmpty()){
                        player.sendMessage(Parser.color(ConfigManager.getString("design/menus/market/confirm_item.yml", "messages.cannot_take_item", "&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили.")));
                        return;
                    }
                    else if(sellItemOptional.get().getAmount() < confirmItem.getSellItem().getAmount()){
                        player.sendMessage(Parser.color(ConfigManager.getString("design/menus/market/confirm_item.yml", "messages.cannot_take_item", "&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили.")));
                        return;
                    }
                    SellItem sellItem = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(confirmItem.getSellItem().getId()).get().get();
                    int price = sellItem.getPrice() * sellItem.getAmount();
                    double money = LiteAuction.getEconomyEditor().getBalance(player.getName());
                    if(money < price){
                        player.sendMessage(Parser.color(ConfigManager.getString("design/menus/market/confirm_item.yml", "messages.not_enough_money", "&#FB2222▶ &fУ вас &#FB2222недостаточно средств &fдля совершения покупки.")));
                        player.playSound(player.getLocation(), Sound.ENTITY_VINDICATOR_AMBIENT, 1f, 1f);
                        player.closeInventory();
                        return;
                    }

                    BuySellItemEvent postEvent = new BuySellItemEvent(player, sellItem);
                    LiteAuction.getEventManager().triggerEvent(postEvent);
                    if(postEvent.isCancelled()){
                        return;
                    }

                    ItemStack itemStack = confirmItem.getSellItem().decodeItemStack();
                    ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#FEA900▶ &fВы купили &#FEA900%item%&f &#FEA900x" + confirmItem.getSellItem().getAmount() + " &fу &#FEA900" + sellItem.getPlayer() + " &fза &#FEA900" + Formatter.formatPrice(price)), itemStack);
                    player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 1f, 1f);

                    LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "market " + confirmItem.getSellItem().getId());
                    LiteAuction.getInstance().getCommunicationManager().publishMessage("hover", sellItem.getPlayer() + " " + Parser.color(ItemHoverUtil.getHoverItemMessage("&#00D4FB▶ &#00D5FB" + player.getName() + " &fкупил у вас &#9AF5FB%item%&f &#9AF5FBx" + sellItem.getAmount() + " &fза &#FEA900" + price + Formatter.CURRENCY_SYMBOL, sellItem.decodeItemStack().asQuantity(sellItem.getAmount()))));
                    LiteAuction.getInstance().getCommunicationManager().publishMessage("sound", sellItem.getPlayer() + " " + Sound.ENTITY_WANDERING_TRADER_YES.toString().toLowerCase() + " 1.0 1.0");

                    LiteAuction.getEconomyEditor().addBalance(sellItem.getPlayer(), price);
                    LiteAuction.getEconomyEditor().subtractBalance(player.getName(), price);

                    addItemInventory(player.getInventory(), itemStack.asQuantity(confirmItem.getSellItem().getAmount()), player.getLocation());
                    LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(confirmItem.getSellItem().getId());

                    player.closeInventory();
                } else if (ConfigUtils.getSlots("design/menus/market/confirm_item.yml", "cancel.slot").contains(slot)) {
                    player.closeInventory();
                }
            } catch (Exception e) {
                player.closeInventory();
                player.sendMessage(Parser.color("&#FB2222▶ &fПроизошла &#FB2222ошибка &fпри выполнении действия."));
            }
        }
    }

    @EventHandler
    public void on(InventoryCloseEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof ConfirmItem confirmItem) {
            if(confirmItem.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = confirmItem.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
