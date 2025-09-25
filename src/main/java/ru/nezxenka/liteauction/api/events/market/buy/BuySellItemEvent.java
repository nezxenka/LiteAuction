package ru.nezxenka.liteauction.api.events.market.buy;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import ru.nezxenka.liteauction.api.events.Cancellable;
import ru.nezxenka.liteauction.api.events.Event;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;

@Getter
public class BuySellItemEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    private final SellItem item;

    public BuySellItemEvent(Player player, SellItem item){
        this.player = player;
        this.item = item;
    }
}