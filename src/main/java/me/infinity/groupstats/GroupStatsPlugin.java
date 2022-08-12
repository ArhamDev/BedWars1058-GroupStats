package me.infinity.groupstats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.infinity.groupstats.listener.GameListener;
import me.infinity.groupstats.manager.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class GroupStatsPlugin extends JavaPlugin {

  @Getter
  private static final Gson gson = new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .create();

  private DatabaseManager databaseManager;

  @Override
  public void onEnable() {
    this.databaseManager = new DatabaseManager(this);
    new GameListener(this);
  }

  @Override
  public void onDisable() {
    this.databaseManager.disconnect();
  }
}
