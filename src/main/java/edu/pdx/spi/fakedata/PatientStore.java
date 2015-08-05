package edu.pdx.spi.fakedata;

import edu.pdx.spi.fakedata.models.Patient;

import java.util.HashMap;
import java.util.Map;

final public class PatientStore {

  final Map<Integer, Patient> patients;

  public PatientStore() {
    patients = new HashMap<>();
    patients.put(1, new Patient(1));
    patients.put(2, new Patient(2));
    patients.put(3, new Patient(3));
    patients.put(4, new Patient(4));
  }

  public Patient getPatient(int id) {
    return patients.get(id);
  }

  public Map<Integer, Patient> getAllPatients() {
    return patients;
  }
}
