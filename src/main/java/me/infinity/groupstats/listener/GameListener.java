package me.infinity.groupstats.listener;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import me.infinity.groupstats.GroupStatsPlugin;
import me.infinity.groupstats.group.GroupNode;
import me.infinity.groupstats.group.GroupNodeProfile;
import ninja.smirking.events.bukkit.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class GameListener {

  private final GroupStatsPlugin instance;

  public GameListener(GroupStatsPlugin instance) {
    this.instance = instance;

    Events.observeAll(PlayerBedBreakEvent.class, event -> {
      if (event.getArena().getGroup() == null) {
        instance.getLogger().warning("Arena " + event.getArena().getDisplayName() + " doesn't have an group, statistics of player won't be affected");
        return;
      }
      GroupNodeProfile profile = instance.getDatabaseManager().getCache().get(event.getPlayer().getUniqueId());
      GroupNode groupNode = profile.getGroupNodeContainer().get(event.getArena().getGroup());
      groupNode.setBedsBroken(groupNode.getBedsBroken() + 1);
      try {
        profile.save();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }

      event.getVictimTeam().getMembers().forEach(member -> {
        GroupNodeProfile memberProfile = instance.getDatabaseManager().getCache().get(member.getUniqueId());
        GroupNode memberGroupNode = memberProfile.getGroupNodeContainer().get(event.getArena().getGroup());
        memberGroupNode.setBedsLost(memberGroupNode.getBedsLost() + 1);
        try {
          memberProfile.save();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      });
    });

    Events.observe(PlayerKillEvent.class, event -> {
      if (event.getArena().getGroup() == null) {
        instance.getLogger().warning("Arena " + event.getArena().getDisplayName() + " doesn't have an group, statistics of player won't be affected");
        return;
      }

      GroupNodeProfile victimStats = instance.getDatabaseManager().getCache().get(event.getVictim().getUniqueId());
      GroupNodeProfile killerStats = !event.getVictim().equals(event.getKiller()) ?
              (event.getKiller() == null ? null : instance.getDatabaseManager().getCache().get(event.getKiller().getUniqueId())) : null;

      GroupNode victimContainer = victimStats.getGroupNodeContainer().get(event.getArena().getGroup());

      if (event.getCause().isFinalKill()) {
        victimContainer.setFinalDeaths(victimContainer.getFinalDeaths() + 1);
        victimContainer.setLosses(victimContainer.getLosses() + 1);
        victimContainer.setGamesPlayed(victimContainer.getGamesPlayed() + 1);

        if (killerStats != null) {
          killerStats.getGroupNodeContainer().get(event.getArena().getGroup()).setFinalKills(killerStats.getGroupNodeContainer().get(event.getArena().getGroup()).getFinalKills() + 1);
        }

      } else {
        victimContainer.setDeaths(victimContainer.getDeaths() + 1);
        if (killerStats != null)
          killerStats.getGroupNodeContainer().get(event.getArena().getGroup()).setKills(killerStats.getGroupNodeContainer().get(event.getArena().getGroup()).getKills() + 1);
      }

      try {
        victimStats.save();
        killerStats.save();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    Events.observeAll(GameEndEvent.class, event -> {
      if (event.getArena().getGroup() == null) {
        instance.getLogger().warning("Arena " + event.getArena().getDisplayName() + " doesn't have an group, statistics of player won't be affected");
        return;
      }
      for (UUID uuid : event.getWinners()) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) continue;
        if (!player.isOnline()) continue;

        GroupNodeProfile profile = instance.getDatabaseManager().getCache().get(uuid);
        profile.getGroupNodeContainer().get(event.getArena().getGroup()).setWins(profile.getGroupNodeContainer().get(event.getArena().getGroup()).getWins() + 1);

        try {
          profile.save();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }

        IArena arena = instance.getBedwarsAPI().getArenaUtil().getArenaByPlayer(player);
        if (arena != null && arena.equals(event.getArena())) {
          profile.getGroupNodeContainer().get(event.getArena().getGroup()).setGamesPlayed(profile.getGroupNodeContainer().get(event.getArena().getGroup()).getGamesPlayed() + 1);
          try {
            profile.save();
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        }
      }
    });

    Events.observeAll(PlayerLeaveArenaEvent.class, event -> {
      if (event.getArena().getGroup() == null) {
        instance.getLogger().warning("Arena " + event.getArena().getDisplayName() + " doesn't have an group, statistics of player won't be affected");
        return;
      }
      final Player player = event.getPlayer();

      ITeam team = event.getArena().getExTeam(player.getUniqueId());
      if (team == null) {
        return;
      }

      if (event.getArena().getStatus() == GameState.starting || event.getArena().getStatus() == GameState.waiting) {
        return;
      }

      GroupNodeProfile groupNodeProfile = instance.getDatabaseManager().getCache().get(player.getUniqueId());
      if (groupNodeProfile == null) return;

      if (event.getArena().getStatus() == GameState.playing) {
        if (team.isBedDestroyed()) {
          if (event.getArena().isPlayer(player)) {
            groupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).setFinalDeaths(groupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).getFinalDeaths() + 1);
            groupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).setLosses(groupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).getLosses() + 1);
          }

          Player damager = event.getLastDamager();
          ITeam killerTeam = event.getArena().getTeam(damager);
          if (damager != null && event.getArena().isPlayer(damager) && killerTeam != null) {
            GroupNodeProfile damagerStat = instance.getDatabaseManager().getCache().get(damager.getUniqueId());
            damagerStat.getGroupNodeContainer().get(event.getArena().getGroup()).setFinalKills(damagerStat.getGroupNodeContainer().get(event.getArena().getGroup()).getFinalKills() + 1);
          }
        } else {
          Player damager = event.getLastDamager();
          ITeam killerTeam = event.getArena().getTeam(damager);

          if (event.getLastDamager() != null && event.getArena().isPlayer(damager) && killerTeam != null) {
            groupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).setDeaths(groupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).getDeaths() + 1);

            GroupNodeProfile damagerGroupNodeProfile = instance.getDatabaseManager().getCache().get(damager.getUniqueId());
            damagerGroupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).setKills(damagerGroupNodeProfile.getGroupNodeContainer().get(event.getArena().getGroup()).getKills() + 1);
          }
        }
      }
      try {
        groupNodeProfile.save();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
  }
}