package me.infinity.groupstats.group;

import lombok.Data;

@Data
public class GroupNode {

  private int gamesPlayed;
  private int bedsBroken;
  private int bedsLost;
  private int kills;
  private int deaths;
  private int finalKills;
  private int finalDeaths;
  private int wins;
  private int losses;
  private int winstreak;
  private int highestWinstreak;

  public GroupNode empty() {
    GroupNode toReturn = new GroupNode();
    toReturn.setGamesPlayed(0);
    toReturn.setBedsBroken(0);
    toReturn.setBedsLost(0);
    toReturn.setKills(0);
    toReturn.setDeaths(0);
    toReturn.setFinalKills(0);
    toReturn.setFinalDeaths(0);
    toReturn.setWins(0);
    toReturn.setLosses(0);
    toReturn.setWinstreak(0);
    toReturn.setHighestWinstreak(0);
    return toReturn;
  }

}
