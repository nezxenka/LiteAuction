package ru.nezxenka.liteauction.api.events.market.buy;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.nezxenka.liteauction.api.events.Cancellable;
import ru.nezxenka.liteauction.api.events.Event;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;

@Getter
public class PreClickSellItemEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    private final SellItem sellItem;
    private final Inventory inventory;
    private final int slot;

    public PreClickSellItemEvent(Player player, SellItem sellItem, Inventory inventory, int slot){
        this.player = player;
        this.sellItem = sellItem;
        this.inventory = inventory;
        this.slot = slot;
    }
}
