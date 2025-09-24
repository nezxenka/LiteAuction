package ru.nezxenka.liteauction.backend.communication.impl;

import ru.nezxenka.liteauction.backend.communication.AbstractCommunication;

public class Local extends AbstractCommunication {
    public Local() {
        super("local");
    }

    @Override
    public void publishMessage(String channel, String message) {
        super.onMessage(super.channel + "_" + channel, message);
    }
}
