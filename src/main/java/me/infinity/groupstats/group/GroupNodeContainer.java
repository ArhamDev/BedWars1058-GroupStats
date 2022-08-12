package me.infinity.groupstats.group;

import me.infinity.groupstats.GroupStatsPlugin;

import java.util.HashMap;

public class GroupNodeContainer extends HashMap<String, GroupNode> {

  public GroupNode getGroupNode(String string) {
    return this.get(string);
  }

  public String toJson() {
    return GroupStatsPlugin.getGson().toJson(this);
  }

  public GroupNodeContainer fromJson(String json) {
    return GroupStatsPlugin.getGson().fromJson(json, GroupNodeContainer.class);
  }
}
