package ru.nezxenka.liteauction.frontend.menus.market.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.liteauction.LiteAuction;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;
import ru.nezxenka.liteauction.backend.utils.ItemHoverUtil;
import ru.nezxenka.liteauction.backend.utils.Parser;
import ru.nezxenka.liteauction.frontend.menus.abst.AbstractListener;
import ru.nezxenka.liteauction.frontend.menus.market.menus.Main;
import ru.nezxenka.liteauction.frontend.menus.market.menus.RemoveItem;

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
                switch (slot) {
                    case 0:
                    case 1:
                    case 2:
                    case 9:
                    case 10:
                    case 11:
                    case 18:
                    case 19:
                    case 20:
                        Optional<SellItem> sellItemOptional = LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().getItem(removeItem.getSellItem().getId()).get();
                        if (sellItemOptional.isEmpty()){
                            player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                            return;
                        }
                        else if(sellItemOptional.get().getAmount() < removeItem.getSellItem().getAmount()){
                            player.sendMessage(Parser.color("&x&F&F&2&2&2&2▶ &fНевозможно забрать предмет, так как его уже купили."));
                            return;
                        }

                        ItemStack itemStack = removeItem.getSellItem().decodeItemStack();
                        ItemHoverUtil.sendHoverItemMessage(player, Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + removeItem.getSellItem().getAmount() + " &fбыл снят с продажи."), itemStack);
                        LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "market " + removeItem.getSellItem().getId());
                        addItemInventory(player.getInventory(), itemStack.asQuantity(removeItem.getSellItem().getAmount()), player.getLocation());
                        LiteAuction.getInstance().getDatabaseManager().getSellItemsManager().deleteItem(removeItem.getSellItem().getId());

                        player.closeInventory();
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 15:
                    case 16:
                    case 17:
                    case 24:
                    case 25:
                    case 26:
                        player.closeInventory();
                        break;
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
