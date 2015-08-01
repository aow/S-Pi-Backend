package edu.pdx.spi.dataproviders;

import edu.pdx.spi.utils.QueryCache;

public interface DataProvider {
  void startAlert(String responseChannel, String id, String ip);
  void startStream(String responseChannel, String type, String id, String ip);
  QueryCache getCache();
}
