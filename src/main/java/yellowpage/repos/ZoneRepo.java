package yellowpage.repos;

import java.util.List;

import yellowpage.model.Zone;

public interface ZoneRepo {
  public List<Zone> getZonesByDomain(String domain);
  public List<Zone> getZones();
}
