package ru.nezxenka.liteauction.frontend.menus.bids.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.liteauction.LiteAuction;
import ru.nezxenka.liteauction.backend.config.ConfigManager;
import ru.nezxenka.liteauction.backend.config.utils.ConfigUtils;
import ru.nezxenka.liteauction.backend.storage.models.BidItem;
import ru.nezxenka.liteauction.backend.utils.tags.ItemHoverUtil;
import ru.nezxenka.liteauction.backend.utils.format.Parser;
import ru.nezxenka.liteauction.frontend.menus.abst.AbstractListener;
import ru.nezxenka.liteauction.frontend.menus.bids.menus.Main;
import ru.nezxenka.liteauction.frontend.menus.bids.menus.RemoveItem;

import java.util.Optional;

import static ru.nezxenka.liteauction.LiteAuction.addItemInventory;

public class RemoveItemListener extends AbstractListener {
    @EventHandler
    public void onClick(InventoryClickEvent event){
        Inventory inventory = event.getView().getTopInventory();
        if(inventory.getHolder() instanceof RemoveItem removeItem) {
            event.setCancelled(true);
            if(event.getClickedInventory() == null || event.getClickedInventory() != inventory){
                return;
            }
            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            try {
                if (ConfigUtils.getSlots("design/menus/bids/remove_item.yml", "approve.slot").contains(slot)) {
                    Optional<BidItem> bidItemOptional = LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().getItem(removeItem.getBidItem().getId()).get();
                    if (bidItemOptional.isEmpty()){
                        player.sendMessage(Parser.color(ConfigManager.getString("design/menus/bids/remove_item.yml", "messages.cannot_take_item", "&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили.")));
                        return;
                    }

                    ItemStack itemStack = removeItem.getBidItem().decodeItemStack();
                    ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + itemStack.getAmount() + " &fбыл снят с продажи."), itemStack);
                    LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "bids " + bidItemOptional.get().getId() + " delete");
                    addItemInventory(player.getInventory(), itemStack, player.getLocation());
                    LiteAuction.getInstance().getDatabaseManager().getBidItemsManager().deleteItem(removeItem.getBidItem().getId());

                    player.closeInventory();
                } else if (ConfigUtils.getSlots("design/menus/bids/remove_item.yml", "cancel.slot").contains(slot)) {
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
        if(inventory.getHolder() instanceof RemoveItem removeItem) {
            if(removeItem.isForceClose()){
                return;
            }
            Bukkit.getScheduler().runTaskLater(LiteAuction.getInstance(), () -> {
                Main main = removeItem.getBack();
                if(main.getViewer() != null) {
                    main.compile().open();
                }
            }, 1);
        }
    }
}
