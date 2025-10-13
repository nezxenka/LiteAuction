package ru.nezxenka.liteauction.api.events;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancel);
}