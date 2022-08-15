package me.infinity.groupstats;

import com.andrei1058.bedwars.api.BedWars;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.infinity.groupstats.listener.GameListener;
import me.infinity.groupstats.listener.ProfileListener;
import me.infinity.groupstats.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class GroupStatsPlugin extends JavaPlugin {

  @Getter
  private static final Gson gson = new GsonBuilder()
          .setPrettyPrinting()
          .disableHtmlEscaping()
          .create();

  private BedWars bedwarsAPI;
  private DatabaseManager databaseManager;

  @Override
  public void onEnable() {
    saveDefaultConfig();

    if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
      getLogger().severe("BedWars1058 was not found. Disabling...");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    this.bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars .class).getProvider();
    this.databaseManager = new DatabaseManager(this);

    new ProfileListener(this);
    new GameListener(this);
  }

  @Override
  public void onDisable() {
    this.databaseManager.disconnect();
  }
}
