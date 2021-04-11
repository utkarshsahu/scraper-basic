package com.example.webscraper;

public class CourseLoadRequest {

  boolean includeClosed;
  String filterText;
  String category;
  String status;
  String duration;
  String examDate;
  String credits;
  String ncCode;
  int howMany;

  public CourseLoadRequest(boolean includeClosed, String filterText, String category, String status, String duration,
      String examDate, String credits, String ncCode, int howMany) {
    this.includeClosed = includeClosed;
    this.filterText = filterText;
    this.category = category;
    this.status = status;
    this.duration = duration;
    this.examDate = examDate;
    this.credits = credits;
    this.ncCode = ncCode;
    this.howMany = howMany;
  }

}
