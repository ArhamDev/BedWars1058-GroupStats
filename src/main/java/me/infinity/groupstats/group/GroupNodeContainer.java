package me.infinity.groupstats.group;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GroupNodeContainer extends ConcurrentHashMap<String, GroupNode> {

  @Override
  public GroupNode get(Object key) {
    Optional<GroupNode> groupNode = Optional.ofNullable(super.get(key));
    if (groupNode.isPresent()) {
      return groupNode.get();
    }
    super.put(String.valueOf(key), new GroupNode().empty());
    return groupNode.get();
  }
}
