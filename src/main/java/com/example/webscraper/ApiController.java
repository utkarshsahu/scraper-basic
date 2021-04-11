package com.example.webscraper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {
  private final ScraperService scraperService;

  ApiController(ScraperService service) {
    this.scraperService = service;
  }

  @GetMapping(value = "/courseList")
  public Object getCourses(@RequestBody CourseLoadRequest req) {
    return null;
  }

  @PostMapping(value = "/loadCourses")
  public Object loadCourses(@RequestBody CourseLoadRequest req) {
    return scraperService.loadCourses(req);
  }
}
