package me.infinity.groupstats.listener;

import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerFirstSpawnEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import me.infinity.groupstats.GroupStatsPlugin;
import me.infinity.groupstats.group.GroupNode;
import me.infinity.groupstats.group.GroupNodeProfile;
import ninja.smirking.events.bukkit.Events;

import java.sql.SQLException;

public class GameListener {

  private final GroupStatsPlugin instance;

  public GameListener(GroupStatsPlugin instance) {
    this.instance = instance;

    Events.observeAll(PlayerFirstSpawnEvent.class, event -> {
      if (event.getArena().getGroup() == null) {
        instance.getLogger().warning("Arena " + event.getArena().getDisplayName() + " doesn't have an group, statistics of player won't be affected");
        return;
      }
      try {
        GroupNodeProfile profile = new GroupNodeProfile(event.getPlayer().getUniqueId()).get();
        GroupNode groupNode = profile.getGroupNodeContainer().getGroupNode(event.getArena().getGroup());
        groupNode.setGamesPlayed(groupNode.getGamesPlayed() + 1);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });

    Events.observeAll(PlayerBedBreakEvent.class, event -> {
      if (event.getArena().getGroup() == null) {
        instance.getLogger().warning("Arena " + event.getArena().getDisplayName() + " doesn't have an group, statistics of player won't be affected");
        return;
      }
      try {
        GroupNodeProfile profile = new GroupNodeProfile(event.getPlayer().getUniqueId()).get();
        GroupNode groupNode = profile.getGroupNodeContainer().getGroupNode(event.getArena().getGroup());
        groupNode.setBedsBroken(groupNode.getBedsBroken() + 1);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }

      event.getVictimTeam().getMembers().forEach(member -> {
        try {
          GroupNodeProfile profile = new GroupNodeProfile(member.getUniqueId()).get();
          GroupNode groupNode = profile.getGroupNodeContainer().getGroupNode(event.getArena().getGroup());
          groupNode.setBedsLost(groupNode.getBedsLost() + 1);
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
      try {
        GroupNodeProfile profile = new GroupNodeProfile(event.getKiller().getUniqueId()).get();
        GroupNode groupNode = profile.getGroupNodeContainer().getGroupNode(event.getArena().getGroup());
        groupNode.setKills(groupNode.getKills() + 1);
        if (event.getCause().isFinalKill()) groupNode.setFinalKills(groupNode.getFinalKills() + 1);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
      try {
        GroupNodeProfile profile = new GroupNodeProfile(event.getVictim().getUniqueId()).get();
        GroupNode groupNode = profile.getGroupNodeContainer().getGroupNode(event.getArena().getGroup());
        groupNode.setDeaths(groupNode.getDeaths() + 1);
        if (event.getCause().isFinalKill()) groupNode.setFinalDeaths(groupNode.getFinalDeaths() + 1);
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
    });
    Events.observeAll(GameEndEvent.class, event -> {
      event.getLosers().forEach(losers -> {
        try {
          GroupNodeProfile profile = new GroupNodeProfile(losers).get();
          GroupNode groupNode = profile.getGroupNodeContainer().getGroupNode(event.getArena().getGroup());
          groupNode.setLosses(groupNode.getLosses() + 1);
          groupNode.setWinstreak(0);
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      });
      event.getWinners().forEach(winners -> {
        try {
          GroupNodeProfile profile = new GroupNodeProfile(winners).get();
          GroupNode groupNode = profile.getGroupNodeContainer().getGroupNode(event.getArena().getGroup());
          groupNode.setWins(groupNode.getWins() + 1);
          groupNode.setWinstreak(groupNode.getWinstreak() + 1);
          if (groupNode.getWinstreak() > groupNode.getHighestWinstreak()) groupNode.setHighestWinstreak(groupNode.getWinstreak());
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      });
    });
  }

}
