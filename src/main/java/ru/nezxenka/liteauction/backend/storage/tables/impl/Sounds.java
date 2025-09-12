package ru.nezxenka.liteauction.backend.storage.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import ru.nezxenka.liteauction.backend.config.ConfigManager;
import ru.nezxenka.liteauction.backend.storage.tables.AbstractTable;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class Sounds extends AbstractTable {
    public Sounds(HikariDataSource dataSource) {
        super(dataSource);
    }

    public CompletableFuture<Void> createTable() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS sounds (" +
                        "player VARCHAR(16) PRIMARY KEY, " +
                        "toggle BOOLEAN NOT NULL DEFAULT TRUE)";
                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> getSoundToggle(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT toggle FROM sounds WHERE player = ?")) {

                statement.setString(1, player);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean("toggle");
                    }
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> setSoundToggle(String player, boolean toggle) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO sounds (player, toggle) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE toggle = ?")) {

                statement.setString(1, player);
                statement.setBoolean(2, toggle);
                statement.setBoolean(3, toggle);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}