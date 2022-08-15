package me.infinity.groupstats.listener;

import me.infinity.groupstats.GroupStatsPlugin;
import me.infinity.groupstats.group.GroupNodeProfile;
import ninja.smirking.events.bukkit.Events;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class ProfileListener {

  private final GroupStatsPlugin instance;

  public ProfileListener(GroupStatsPlugin instance) {
    this.instance = instance;
    Events.observeAll(AsyncPlayerPreLoginEvent.class, handle -> {
      if (handle.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
        return;
      }
      try {
        GroupNodeProfile groupNodeProfile = new GroupNodeProfile(handle.getUniqueId());
        instance.getDatabaseManager().getCache().put(handle.getUniqueId(), groupNodeProfile.get());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    Events.observeAll(PlayerLoginEvent.class, event -> {
      if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
        instance.getDatabaseManager().getCache().remove(event.getPlayer().getUniqueId());
      }
    });

    Events.observeAll(PlayerQuitEvent.class, handler -> {
      try {
        instance.getDatabaseManager().getCache().get(handler.getPlayer().getUniqueId()).save();
        instance.getDatabaseManager().getCache().remove(handler.getPlayer().getUniqueId());
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
      /*
      instance.getDatabaseManager().getHikariExecutor().execute(() -> {
        try {
          instance.getDatabaseManager().getCache().get(handler.getPlayer().getUniqueId()).save();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
        instance.getDatabaseManager().getCache().remove(handler.getPlayer().getUniqueId());
      });
      */
    });
  }
}
