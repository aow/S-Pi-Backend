package edu.pdx.spi.fakedata;

import edu.pdx.spi.fakedata.models.Patient;
import edu.pdx.spi.fakedata.models.PatientLabs;
import edu.pdx.spi.fakedata.models.PatientMed;
import edu.pdx.spi.fakedata.models.PatientNotes;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URL;
import java.util.*;

final public class PatientStore {
  final Map<Integer, Patient> patients;
  final Map<Integer, Integer> idMapping;
  Reader in;

  public PatientStore() {
    patients = new HashMap<>();
    idMapping = new HashMap<>();

    getParsedFile(getClass().getClassLoader().getResourceAsStream("patient_overview.csv")).get().forEach(record -> {
      Patient p = new Patient(record.toMap());
      patients.put(p.id(), p);
      idMapping.put(p.pid(), p.id());
    });

    getParsedFile(getClass().getClassLoader().getResourceAsStream("patient_progress.csv")).get().forEach(record -> {
      PatientNotes pn = new PatientNotes(record.toMap());
      patients.get(idMapping.get(pn.pid())).addNote(pn);
    });

    getParsedFile(getClass().getClassLoader().getResourceAsStream("patient_labs.csv")).get().forEach(record -> {
      PatientLabs pl = new PatientLabs(record.toMap());
      patients.get(idMapping.get(pl.pid())).addLab(pl);
    });

    getParsedFile(getClass().getClassLoader().getResourceAsStream("patient_medicine.csv")).get().forEach(record -> {
      PatientMed pm = new PatientMed(record.toMap());
      patients.get(idMapping.get(pm.pid())).addMed(pm);
    });
  }

  public Patient getPatient(int id) {
    return patients.get(id);
  }

  public Map<Integer, Patient> getAllPatients() {
    return patients;
  }

  private Optional<Iterable<CSVRecord>> getParsedFile(InputStream fileName) {
    in = new InputStreamReader(fileName);
    try {
      Iterable<CSVRecord> patient = CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces().parse(in);
      return Optional.ofNullable(patient);
    } catch (IOException e) {
      System.out.println("Error parsing csv file");
      return Optional.empty();
    }
  }
}
