package edu.pdx.spi.fakedata.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Patient {
  @JsonProperty("id")
  Integer id;
  @JsonProperty("bed")
  String roomNum;
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
  PatientNotes notes;

  @JsonIgnore
  String[] statuses = {"Critical", "Stable", "Serious"};

  public Patient(int id, String bed, String name, int age, String status, int patientId,
                 int hospId, int caseId, double weight, double height, double temperature,
                 double heartRate, String allergies, boolean cardiac, String bloodPressure) {
    this.id = id;
    this.roomNum = bed;
    this.name = name;
    this.age = age;
    this.status = status;
    this.patientId = patientId;
    this.admissionId = hospId;
    this.caseId = caseId;
    this.weight = weight;
    this.height = height;
    this.temperature = temperature;
    this.heartRate = heartRate;
    this.allergies = allergies;
    this.cardiac = cardiac;
    this.bloodPressure = bloodPressure;
    this.notes = new PatientNotes(id);
  }
}
