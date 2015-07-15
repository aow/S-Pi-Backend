package edu.pdx.spi.fakedata.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
  String patientId;
  @JsonProperty("hospital_admission_id")
  Integer admissionId;
  @JsonProperty("case_id")
  Integer caseId;
  @JsonProperty("weight")
  Integer weight;
  @JsonProperty("height")
  Integer height;
  @JsonProperty("temperature")
  Double temperature;
  @JsonProperty("heart-rate")
  Integer heartRate;
  @JsonProperty("allergies")
  String allergies;
  @JsonProperty("cardiac")
  Boolean cardiac;
  @JsonProperty("blood_pressure")
  String bloodPressure;

  @JsonIgnore
  String[] firstNames = {"Bob", "Mary", "Sally", "Jane", "John"};
  @JsonIgnore
  String[] lastNames = {"Doe", "Johnson", "Hamilton", "Kennedy"};
  @JsonIgnore
  String[] allergens = {"Peanuts", "Latex", "Penicillin", "Aciclovir"};
  @JsonIgnore
  String[] statuses = {"Critical", "Stable", "Serious"};

  public Patient(int id) {
    Random rn = new Random();
    this.id = id;
    roomNum = String.valueOf(rn.nextInt(200));
    name = firstNames[rn.nextInt(firstNames.length)] + " " + lastNames[rn.nextInt(lastNames.length)];
    age = rn.nextInt(90) + 5;
    status = statuses[rn.nextInt(statuses.length)];
    patientId = String.valueOf(rn.nextInt(10000)) + "-"
        + String.valueOf(rn.nextInt(1000)) + "-"
        + String.valueOf(rn.nextInt(100));
    admissionId = rn.nextInt(100000);
    caseId = rn.nextInt(10000);
    weight = rn.nextInt(200) + 20;
    height = rn.nextInt(200) + 50;
    temperature = Math.abs(rn.nextGaussian() * 90);
    heartRate = rn.nextInt(200);
    allergies = allergens[rn.nextInt(allergens.length)];
    cardiac = rn.nextBoolean();
    bloodPressure = String.valueOf(rn.nextInt(200) + 100) + "/" + String.valueOf(rn.nextInt(100));
  }
}
