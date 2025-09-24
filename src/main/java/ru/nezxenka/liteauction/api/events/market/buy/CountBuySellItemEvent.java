package ru.nezxenka.liteauction.api.events.market.buy;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import ru.nezxenka.liteauction.api.events.Cancellable;
import ru.nezxenka.liteauction.api.events.Event;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;

@Getter
public class CountBuySellItemEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    private final SellItem item;
    private final int amount;

    public CountBuySellItemEvent(Player player, SellItem item, int amount){
        this.player = player;
        this.item = item;
        this.amount = amount;
    }
}
