package ru.nezxenka.liteauction.api.events.market.compile;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import ru.nezxenka.liteauction.api.events.Cancellable;
import ru.nezxenka.liteauction.api.events.Event;
import ru.nezxenka.liteauction.backend.enums.CategoryType;
import ru.nezxenka.liteauction.backend.enums.MarketSortingType;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;

import java.util.HashSet;
import java.util.List;

@Getter
public class PostMarketCompileEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    private final List<SellItem> items;
    private final int page;
    private final String target;
    private final MarketSortingType sortingType;
    private final CategoryType categoryType;
    private final HashSet<String> filters;

    public PostMarketCompileEvent(
            Player player,
            List<SellItem> items,
            int page,
            String target,
            MarketSortingType sortingType,
            CategoryType categoryType,
            HashSet<String> filters
    ){
        this.player = player;
        this.items = items;
        this.page = page;
        this.target = target;
        this.sortingType = sortingType;
        this.categoryType = categoryType;
        this.filters = filters;
    }
}
