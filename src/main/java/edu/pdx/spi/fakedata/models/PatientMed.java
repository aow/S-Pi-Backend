package edu.pdx.spi.fakedata.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

final public class PatientMed {
  @JsonProperty
  Integer pid;
  @JsonProperty
  String date;
  @JsonProperty
  String label;
  @JsonProperty
  String dose;
  @JsonProperty
  String doseUnits;
  @JsonProperty
  String route;

  public PatientMed(Map<String, String> med) {
    this.pid = Integer.valueOf(med.get("subject_id"));
    date = med.get("charttime");
    label = med.get("label");
    dose = med.get("dose");
    doseUnits = med.get("doseuom");
    route = med.get("route");
  }

  public int pid() {
    return this.pid;
  }
}
