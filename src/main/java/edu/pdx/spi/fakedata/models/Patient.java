package edu.pdx.spi.fakedata.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Patient {
  @JsonProperty("id")
  Integer id;
  @JsonProperty("bed")
  Integer roomNum;
  @JsonProperty("name")
  String name;
  @JsonProperty("age")
  Integer age;
  @JsonProperty("status")
  String status;
  @JsonProperty("patient_id")
  Integer patientId;
  @JsonProperty("hospital_admission_id")
  Integer admissionId;
  @JsonProperty("case_id")
  Integer caseId;
  @JsonProperty("weight")
  Double weight;
  @JsonProperty("height")
  Double height;
  @JsonProperty("temperature")
  Double temperature;
  @JsonProperty("heart-rate")
  Double heartRate;
  @JsonProperty("allergies")
  String allergies;
  @JsonProperty("cardiac")
  Boolean cardiac;
  @JsonProperty("blood_pressure")
  String bloodPressure;
  @JsonProperty("clinical_notes")
  List<PatientNotes> notes;
  @JsonProperty("labs")
  List<PatientLabs> labs;
  @JsonProperty("meds")
  List<PatientMed> meds;

  @JsonIgnore
  String[] statuses = {"Critical", "Stable", "Serious"};

  public Patient(Map<String, String> patient) {
    Random rn = new Random();
    this.id = Integer.valueOf(patient.get("id"));
    this.roomNum = rn.nextInt(100);
    this.name = patient.get("name");
    this.age = Integer.valueOf(patient.get("age"));
    this.status = patient.get("status");
    this.patientId = Integer.valueOf(patient.get("patient_id"));
    this.admissionId = Integer.valueOf(patient.get("hospital_admission_id"));
    this.caseId = Integer.valueOf(patient.get("case_id"));
    this.weight = Double.valueOf(patient.get("weight"));
    this.height = Double.valueOf(patient.get("height"));
    this.temperature = Double.valueOf(patient.get("temperature"));
    this.heartRate = Double.valueOf(patient.get("heart-rate"));
    this.allergies = patient.get("allergies");
    this.cardiac = "t".equals(patient.get("cardiac"));
    this.bloodPressure = patient.get("blood_pressure");
    this.notes = new ArrayList<>();
    this.labs = new ArrayList<>();
    this.meds = new ArrayList<>();
  }

  public int id() {
    return this.id;
  }

  public int pid() {
    return this.patientId;
  }

  public void addNote(PatientNotes pn) {
    notes.add(pn);
  }

  public void addLab(PatientLabs pl) {
    labs.add(pl);
  }

  public void addMed(PatientMed pm) {
    meds.add(pm);
  }
}
