package ru.nezxenka.liteauction.backend.communication;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import ru.nezxenka.liteauction.LiteAuction;
import ru.nezxenka.liteauction.backend.storage.models.BidItem;
import ru.nezxenka.liteauction.backend.storage.models.SellItem;
import ru.nezxenka.liteauction.backend.utils.nms.ContainerUtil;
import ru.nezxenka.liteauction.backend.utils.ItemEncrypt;
import ru.nezxenka.liteauction.backend.utils.tags.ItemHoverUtil;
import ru.nezxenka.liteauction.frontend.menus.bids.menus.ItemBids;
import ru.nezxenka.liteauction.frontend.menus.market.menus.Main;
import ru.nezxenka.liteauction.frontend.menus.market.menus.Sell;

import java.util.Arrays;
import java.util.Map;

public abstract class AbstractCommunication {
    protected String channel;

    public AbstractCommunication(String channel){
        this.channel = channel;
    }

    public void connect() {}
    public void onMessage(String channel, String message) {
        try {
            if (channel.equals(this.channel + "_msg")) {
                String[] splitted = message.split(" ");
                String msg = String.join(" ", Arrays.copyOfRange(splitted, 1, splitted.length));
                String playerName = splitted[0];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    player.sendMessage(msg);
                }
            } else if (channel.equals(this.channel + "_hover")) {
                String[] splitted = message.split(" ");
                String msg = String.join(" ", Arrays.copyOfRange(splitted, 2, splitted.length));
                String playerName = splitted[0];
                String encodedItemStack = splitted[1];
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    ItemHoverUtil.sendHoverItemMessage(player, msg, ItemEncrypt.decodeItem(encodedItemStack));
                }
            } else if (channel.equals(this.channel + "_sound")) {
                String[] splitted = message.split(" ");
                String playerName = splitted[0];
                Sound sound = Sound.valueOf(splitted[1].toUpperCase());
                float volume = Float.parseFloat(splitted[2]);
                float pitch = Float.parseFloat(splitted[3]);
                Player player = Bukkit.getPlayer(playerName);
                if (player != null) {
                    if(LiteAuction.getInstance().getDatabaseManager().getSoundsManager().getSoundToggle(playerName).get()) {
                        player.playSound(player.getLocation(), sound, volume, pitch);
                    }
                }
            }
            else if (channel.equals(this.channel + "_update")) {
                String[] splitted = message.split(" ");
                int id = Integer.parseInt(splitted[1]);
                if(splitted[0].equalsIgnoreCase("market")){
                    for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()){
                        if(ContainerUtil.hasActiveContainer(player)){
                            Inventory inventory = ContainerUtil.getActiveContainer(player);
                            InventoryHolder holder = inventory.getHolder();
                            if(holder instanceof Sell gui){
                                if(
                                        gui
                                                .getItems()
                                                .values()
                                                .stream()
                                                .anyMatch(i -> i.getId() == id)
                                ){
                                    Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                        gui.setForceClose(true);
                                        gui.compile().open();
                                        gui.setForceClose(false);
                                    });
                                }
                            }
                            else if(holder instanceof Main gui){
                                for(Map.Entry<Integer, SellItem> entry : gui.getItems().entrySet()){
                                    if(entry.getValue().getId() == id){
                                        Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                            inventory.setItem(entry.getKey(), LiteAuction.getBoughtItem().clone());
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
                else if(splitted[0].equalsIgnoreCase("bids")){
                    String action = splitted[2];
                    for(org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        if (ContainerUtil.hasActiveContainer(player)) {
                            Inventory inventory = ContainerUtil.getActiveContainer(player);
                            InventoryHolder holder = inventory.getHolder();
                            if(holder instanceof ru.nezxenka.liteauction.frontend.menus.bids.menus.Sell gui){
                                if(
                                        gui
                                                .getItems()
                                                .values()
                                                .stream()
                                                .anyMatch(i -> i.getId() == id)
                                ){
                                    Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                        gui.setForceClose(true);
                                        gui.compile().open();
                                        gui.setForceClose(false);
                                    });
                                }
                            }
                            else if(holder instanceof ru.nezxenka.liteauction.frontend.menus.bids.menus.Main gui){
                                if(action.equalsIgnoreCase("delete")){
                                    for(Map.Entry<Integer, BidItem> entry : gui.getItems().entrySet()){
                                        if(entry.getValue().getId() == id){
                                            Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                                inventory.setItem(entry.getKey(), LiteAuction.getBoughtItem().clone());
                                            });
                                        }
                                    }
                                }
                            }
                            else if(holder instanceof ItemBids gui){
                                if(action.equalsIgnoreCase("delete")){
                                    if(gui.getBidItem().getId() == id){
                                        Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                            inventory.setItem(4, LiteAuction.getBoughtItem().clone());
                                        });
                                    }
                                }
                                else if(action.equalsIgnoreCase("refresh")){
                                    if(gui.getBidItem().getId() == id) {
                                        Bukkit.getScheduler().runTask(LiteAuction.getInstance(), () -> {
                                            gui.setForceClose(true);
                                            gui.compile().open();
                                            gui.setForceClose(false);
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignore) { }
    }

    public abstract void publishMessage(String channel, String message);
    public void publishMessage(String channel, int message) {
        this.publishMessage(channel, String.valueOf(message));
    }

    public void close() {}
}
