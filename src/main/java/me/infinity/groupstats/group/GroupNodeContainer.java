package me.infinity.groupstats.group;

import me.infinity.groupstats.GroupStatsPlugin;

import java.util.HashMap;
import java.util.Optional;

public class GroupNodeContainer extends HashMap<String, GroupNode> {

  public GroupNode getGroupNode(String string) {
    Optional<GroupNode> groupNodeOptional = Optional.ofNullable(this.get(string));
    if (groupNodeOptional.isPresent()) {
      return groupNodeOptional.get();
    } else {
      this.putIfAbsent(string, new GroupNode());
    }
    return groupNodeOptional.get();
  }

  public String toJson() {
    return GroupStatsPlugin.getGson().toJson(this);
  }

  public GroupNodeContainer fromJson(String json) {
    return GroupStatsPlugin.getGson().fromJson(json, GroupNodeContainer.class);
  }
}
