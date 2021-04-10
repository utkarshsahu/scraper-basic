package com.example.webscraper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ApiUrlGenerator {
  boolean includeClosed;
  String filterText, category;
  String examDate;
  String duration;
  StatusEnum status;
  NcCodeEnum ncCode;
  CreditsEnum credits;
  String url;

  Logger logger = Logger.getLogger(ApiUrlGenerator.class.getName());

  public ApiUrlGenerator(boolean includeClosed, String filterText, String category, String status, String duration, String examDate, String credits, String ncCode, int howMany) throws ParseException, UnsupportedEncodingException {
    this.includeClosed = includeClosed;
    this.filterText = filterText;
    this.category = category;
    this.status = StatusEnum.valueOf(status.toUpperCase());
    this.duration = duration == "" ? "all" : duration;
    if (!examDate.isEmpty()) {
      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      try {
        this.examDate = formatter.format(formatter.parse(examDate));
      } catch (ParseException e) {
        logger.severe("Wrong date format");
        throw new ParseException("Wrong date format", e.getErrorOffset());
      }
    }
    this.examDate = "";
    this.credits = CreditsEnum.valueOf(credits.toUpperCase());
    this.ncCode = ncCode == "" ? NcCodeEnum.ALL : NcCodeEnum.valueOf(ncCode.toUpperCase());
    this.url = this.buildUrl(howMany);
  }

  private String buildUrl(int howMany) throws UnsupportedEncodingException {

    String tot = "http://swayam.gov.in/modules/gql/query?q=%s&expanded_gcb_tags=gcb-markdown";
    String template = "{courseList(args: {includeClosed: %s, filterText: \"%s\", category: \"%s\", status: \"%s\", duration: \"%s\", examDate: \"%s\", credits: \"%s\", ncCode: \"%s\"}, first: %d) {edges {node {  id, title, url, explorerSummary,  explorerInstructorName, enrollment {enrolled},   openForRegistration, showInExplorer,  startDate, endDate, examDate, enrollmentEndDate, estimatedWorkload, category {name, category, parentId},  tags {name}, featured, coursePictureUrl, credits, weeks, nodeCode, instructorInstitute, ncCode}}, pageInfo {endCursor, hasNextPage}}, examDates{date}}";

    String builtStr = String.format(template, Boolean.toString(this.includeClosed), this.filterText, this.category, this.status.toString(), this.duration, this.examDate, this.credits.getFlag(), this.ncCode.toString(), howMany);
    builtStr = URLEncoder.encode(builtStr, StandardCharsets.UTF_8.toString());


    return String.format(tot, builtStr);
  }

  public String getUrl() {
    // System.out.println(this.url);
    return this.url;
  }
}



// {courseList(args: {includeClosed: false, filterText: "", category: "asdasda", status: "Upcoming", tags: "", duration: "all", examDate: "2021-03-21", credits: "true", ncCode: "all",   }, first:100) {edges {node {  id, title, url, explorerSummary,  explorerInstructorName, enrollment {enrolled},   openForRegistration, showInExplorer,  startDate, endDate, examDate, enrollmentEndDate, estimatedWorkload, category {name, category, parentId},  tags {name}, featured, coursePictureUrl, credits, weeks, nodeCode, instructorInstitute, ncCode}}, pageInfo {endCursor, hasNextPage}},   examDates{date}}