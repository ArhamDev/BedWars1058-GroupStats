package me.infinity.groupstats.group;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import me.infinity.groupstats.GroupStatsPlugin;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Data
@DatabaseTable(tableName = "bedwars-groupstats")
public class GroupNodeProfile {

  private final GroupStatsPlugin instance = GroupStatsPlugin.getPlugin(GroupStatsPlugin.class);

  @DatabaseField(columnName = "UUID", id = true, dataType = DataType.UUID)
  private UUID uniqueID;

  @DatabaseField(columnName = "JSON_VALUE", dataType = DataType.STRING)
  private String jsonValue;

  public GroupNodeProfile() {
  }

  public GroupNodeProfile(UUID uniqueID) {
    this.uniqueID = uniqueID;
  }

  public GroupNodeContainer getGroupNodeContainer() {
    return new GroupNodeContainer().fromJson(jsonValue);
  }

  public GroupNodeProfile get() throws SQLException {
    Optional<GroupNodeProfile> profileOptional = Optional.ofNullable(instance.getDatabaseManager().getProfileDao().queryForId(uniqueID));
    if (profileOptional.isPresent()) {
      return profileOptional.get();
    } else return save();
  }

  public GroupNodeProfile save() throws SQLException {
    this.jsonValue = this.getGroupNodeContainer().toJson();
    Optional<GroupNodeProfile> profileOptional = Optional.ofNullable(instance.getDatabaseManager().getProfileDao().queryForId(uniqueID));

    if (profileOptional.isPresent()) {
      instance.getDatabaseManager().getProfileDao().update(this);
      instance.getDatabaseManager().getProfileDao().refresh(this);
    } else instance.getDatabaseManager().getProfileDao().create(this);

    return profileOptional.get();
  }
}
