package edu.pdx.spi.fakedata.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PatientNotes {
  @JsonProperty("id")
  Integer id;
  @JsonProperty("date")
  String date;
  @JsonProperty
  List<String> notes;

  public PatientNotes(int id) {
    notes = new ArrayList<>();

    switch (id) {
      case 1: {
        notes.add("Neuro: PT awake/alert/oriented, follows commands well. MAEs. Pupils equal and reactive. No neuro deficits noted.");
        notes.add("Resp: 2L NC on, resp easy and regular, O2sat 93-97%, Lungs clear with bibasilar diminished BS. Denies any SOB. No exertional dsypnea noted.");
        notes.add("CV: NSR without ectopy alarms on. HR 70-80, SBP controlled 90-115, MAP 60-70. Esmolol drip titrated prn, currently infusing @ 80 mcg/kg/min. Pt denies CP. MRI completed awaiting results for aortic arch aneurysm. Pulses to ext weak acquired with doppler. A to R radial positional. No edema noted. Skin warm/dry/intact. Heparin SQ BID.");
        date = "3299-01-16";
        this.id = id;
        break;
      }
      case 2: {
        notes.add("Admitting Diagnosis: SEPSIS");
        notes.add("UNDERLYING MEDICAL CONDITION:64 year old woman with new right IJ; septic");
        notes.add("REASON FOR THIS EXAMINATION:eval for PNA, line placement");
        date = "2578-05-14";
        this.id = id;
        break;
      }
      case 3: {
        notes.add("Reason: Evaluate for stroke basilar/pontine area");
        notes.add("Admitting Diagnosis: BACK PAIN");
        notes.add("UNDERLYING MEDICAL CONDITION: 79 year old man with lower extremity weakness FYI- bullet in buttocks");
        notes.add("REASON FOR THIS EXAMINATION:Evaluate for stroke basilar/pontine area");
        notes.add("WET READ: KMcd MON [**2669-6-14**] 2:03 AM no diffusion abnormalities > no evidence of acute stroke.");
        date = "2669-06-17";
        this.id = id;
        break;
      }
      case 4: {
        notes.add("data: admitted from [**Hospital **] hosp last evening. vss.  hr 60-80's af occas pvc's and paced beats.");
        notes.add("resp: with paissy muir valve in placed-able to talk clearly-at times slightly confused. in no distress- on trach collar w/ valve fio2 .35");
        notes.add("02sat 99%. cs-course on expiration dec in bases. coughing and clearing own secretions. suction x1 for thin white. pmv removed o/n.");
        notes.add("npo after mn for proceedure in am.");
        date = "3162-11-01";
        this.id = id;
        break;
      }
    }
  }
}
