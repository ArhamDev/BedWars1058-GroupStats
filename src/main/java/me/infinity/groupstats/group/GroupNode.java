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

}
