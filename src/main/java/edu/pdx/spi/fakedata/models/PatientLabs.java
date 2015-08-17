package edu.pdx.spi.fakedata.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

final public class PatientLabs {
  @JsonProperty
  Integer pid;
  @JsonProperty
  String date;
  @JsonProperty
  String testName;
  @JsonProperty
  String description;
  @JsonProperty
  String value;
  @JsonProperty
  String valueUnits;

  public PatientLabs(Map<String, String> lab) {
    pid = Integer.valueOf(lab.get("subject_id"));
    date = lab.get("charttime");
    testName = lab.get("test_name");
    description = lab.get("loinc_description");
    value = lab.get("value");
    valueUnits = lab.get("valueuom");
  }

  public int pid() {
    return this.pid;
  }
}
