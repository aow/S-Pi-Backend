package edu.pdx.spi.fakedata;

import edu.pdx.spi.fakedata.models.Patient;

import java.util.HashMap;
import java.util.Map;

final public class PatientStore {

  final Map<Integer, Patient> patients;

  public PatientStore() {
    patients = new HashMap<>();
    patients.put(1, new Patient(1,"100","Ann Droid",65,"n/a",3386,26692,4201,65,0,99.9,102,"n/a",true,"0/0"));
    patients.put(2, new Patient(2,"101","Mike Rosoft",71,"n/a",124,12906,152,71.5,175.26,98,76,"n/a",true,"117/58"));
    patients.put(3, new Patient(3,"102","Mac Intosh",79,"n/a",4833,23746,6004,118,0,91.6,128,"n/a",true,"98/49"));
    patients.put(4, new Patient(4,"103","Java Script",80,"n/a",1158,19617,1434,55.4,154.94,96.5,87,"n/a",true,"0/0"));
  }

  public Patient getPatient(int id) {
    return patients.get(id);
  }

  public Map<Integer, Patient> getAllPatients() {
    return patients;
  }
}
