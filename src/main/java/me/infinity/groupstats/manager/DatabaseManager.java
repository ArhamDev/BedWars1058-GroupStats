package me.infinity.groupstats.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.infinity.groupstats.GroupStatsPlugin;
import me.infinity.groupstats.group.GroupNodeProfile;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Getter
public class DatabaseManager {

  @Getter
  private final GroupStatsPlugin instance;
  private final HikariDataSource hikariDataSource;
  private final Executor hikariExecutor = Executors.newFixedThreadPool(4);

  private Dao<GroupNodeProfile, UUID> profileDao;
  private Map<UUID, GroupNodeProfile> cache;

  private ConnectionSource connectionSource;

  private String address, database, username, password;
  private int port;
  private boolean ssl, dbEnabled;

  public DatabaseManager(GroupStatsPlugin instance) {
    this.instance = instance;
    this.loadCredentials();
    HikariConfig hikariConfig = new HikariConfig();

    if (dbEnabled) {
      hikariConfig.setJdbcUrl("jdbc:mysql://" + address + ":" + port + "/" + database);
    } else {
      File database = new File(instance.getDataFolder(), "database.db");
      if (!database.exists()) {
        try {
          database.createNewFile();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
      hikariConfig.setJdbcUrl("jdbc:sqlite:" + database);
      hikariConfig.setDriverClassName("org.sqlite.JDBC");
    }

    hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
    hikariConfig.addDataSourceProperty("useUnicode", true);
    hikariConfig.addDataSourceProperty("useSSL", ssl);
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setUsername(username);
    hikariConfig.setPassword(password);
    hikariConfig.setPoolName("LifeSteal-Pool");
    hikariDataSource = new HikariDataSource(hikariConfig);

    try {
      this.connectionSource = new DataSourceConnectionSource(hikariDataSource, hikariConfig.getJdbcUrl());
      this.profileDao = DaoManager.createDao(connectionSource, GroupNodeProfile.class);
      TableUtils.createTableIfNotExists(connectionSource, GroupNodeProfile.class);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    this.cache = new HashMap<>();
  }

  private void loadCredentials() {
    FileConfiguration configuration = instance.getConfig();
    this.dbEnabled = configuration.getBoolean("DATABASE.ENABLED");
    this.address = configuration.getString("DATABASE.ADDRESS");
    this.port = configuration.getInt("DATABASE.PORT");
    this.database = configuration.getString("DATABASE.DATABASE");
    this.username = configuration.getString("DATABASE.USERNAME");
    this.password = configuration.getString("DATABASE.PASSWORD");
    this.ssl = configuration.getBoolean("DATABASE.SSL");
  }

  public void disconnect() {
    if (connectionSource != null) connectionSource.closeQuietly();
    if (hikariDataSource != null) hikariDataSource.close();
  }
}