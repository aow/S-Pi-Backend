package edu.pdx.spi.fakedata.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PatientNotes {
  @JsonProperty("pid")
  Integer pid;
  @JsonProperty("date")
  String date;
  @JsonProperty
  String note;

  public PatientNotes(Map<String, String> notes) {
    pid = Integer.valueOf(notes.get("id"));
    date = notes.get("date");
    note = notes.get("note");
  }

  public int pid() {
    return this.pid;
  }
}
