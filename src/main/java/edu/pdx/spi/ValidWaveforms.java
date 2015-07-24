package edu.pdx.spi;

public enum ValidWaveforms {
  ABP("ABP"),
  PAP("PAP"),
  RESP("RESP"),
  ECG("ECG");

  private String queryString;

  ValidWaveforms(String queryName) {
    this.queryString = queryName;
  }

  public static boolean contains(String name) {
    for (ValidWaveforms v : ValidWaveforms.values()) {
      if (v.name().equals(name)) return true;
    }

    return false;
  }
}
