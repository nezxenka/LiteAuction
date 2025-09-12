package ru.nezxenka.liteauction.backend.storage.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nezxenka.liteauction.backend.enums.AuctionType;
import ru.nezxenka.liteauction.backend.enums.BidsSortingType;
import ru.nezxenka.liteauction.backend.enums.CategoryType;
import ru.nezxenka.liteauction.backend.enums.MarketSortingType;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiData {
    private int id;
    private String player;
    private AuctionType auctionType;
    private CategoryType categoryType;
    private MarketSortingType marketSortingType;
    private BidsSortingType bidsSortingType;
    private Set<String> additionalFilters;
}