package edu.pdx.spi.fakedata.models;

public class Patient {
  Integer id;
  Integer roomNum;
  String firstName;
  String lastName;

  public Patient(int id, int room, String first, String last) {
    this.id = id;
    this.roomNum = room;
    this.firstName = first;
    this.lastName = last;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public int getRoomNum() {
    return roomNum;
  }

  public void setRoomNum(int roomNum) {
    this.roomNum = roomNum;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public String toString() {
    return "Patient[" +
        "id=" + id +
        ", roomNum=" + roomNum +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ']';
  }
}
