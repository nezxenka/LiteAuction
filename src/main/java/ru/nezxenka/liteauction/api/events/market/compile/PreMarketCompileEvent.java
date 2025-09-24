package ru.nezxenka.liteauction.api.events.market.compile;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import ru.nezxenka.liteauction.api.events.Cancellable;
import ru.nezxenka.liteauction.api.events.Event;
import ru.nezxenka.liteauction.backend.enums.CategoryType;
import ru.nezxenka.liteauction.backend.enums.MarketSortingType;

import java.util.HashSet;

@Getter
public class PreMarketCompileEvent extends Event implements Cancellable {
    @Setter
    private boolean cancelled = false;
    private final Player player;
    @Setter
    private int page;
    @Setter
    private String target;
    @Setter
    private MarketSortingType sortingType;
    @Setter
    private CategoryType categoryType;
    @Setter
    private HashSet<String> filters;

    public PreMarketCompileEvent(
            Player player,
            int page,
            String target,
            MarketSortingType sortingType,
            CategoryType categoryType,
            HashSet<String> filters
    ){
        this.player = player;
        this.page = page;
        this.target = target;
        this.sortingType = sortingType;
        this.categoryType = categoryType;
        this.filters = filters;
    }
}
