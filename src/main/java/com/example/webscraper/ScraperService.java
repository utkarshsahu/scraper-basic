package com.example.webscraper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

  private CoursesRepo coursesRepo;

  public ResponseEntity<String> loadCourses(CourseLoadRequest req) {
    ApiUrlGenerator urlGen;
    try {
      urlGen = new ApiUrlGenerator(req.includeClosed, req.filterText, req.category, req.status, req.duration, req.examDate, req.credits, req.ncCode, req.howMany);
      String jsonResponse;
      jsonResponse = fetchFromApi(urlGen);
      mongoInsert(jsonResponse);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return new ResponseEntity<String>(HttpStatus.OK);
  }



  private String fetchFromApi(ApiUrlGenerator urlGen) throws Exception {

    URL url = new URL(urlGen.getUrl());
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setReadTimeout(5000);
    conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
    conn.addRequestProperty("User-Agent", "Mozilla");
    conn.addRequestProperty("Referer", "google.com");

    System.out.println("Request URL ... " + url);

    boolean redirect = false;

    // normally, 3xx is redirect
    int status = conn.getResponseCode();
    if (status != HttpURLConnection.HTTP_OK) {
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
            || status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_SEE_OTHER)
        redirect = true;
    }

    System.out.println("Response Code ... " + status);

    if (redirect) {

        // get redirect url from "location" header field
        String newUrl = conn.getHeaderField("Location");

        // get the cookie if need, for login
        String cookies = conn.getHeaderField("Set-Cookie");

        // open the new connnection again
        conn = (HttpURLConnection) new URL(newUrl).openConnection();
        conn.setRequestProperty("Cookie", cookies);
        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        conn.addRequestProperty("User-Agent", "Mozilla");
        conn.addRequestProperty("Referer", "google.com");

        // System.out.println("Redirect to URL : " + newUrl);

    }

    BufferedReader in = new BufferedReader(
                              new InputStreamReader(conn.getInputStream()));
    String inputLine;
    StringBuffer html = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
        html.append(inputLine);
    }
    in.close();

    String conv = html.toString().replace(")]}'", "");

    // System.out.println("URL Content... \n" + conv);
    // System.out.println("Done");

    return conv;
  }

  private void mongoInsert(String jsonResponse) {
    try {

        Mongo mongo = new Mongo("localhost", 27017);
        DB db = mongo.getDB("test");
        DBCollection collection = db.getCollection("courses");
        collection.createIndex("keys");
        JSONObject obj = new JSONObject(jsonResponse);
        JSONArray nodes = obj.getJSONObject("data").getJSONObject("courseList").getJSONArray("edges");

        for (int i = 0; i<nodes.length(); i++) {
          JSONObject node = nodes.getJSONObject(i).getJSONObject("node");
          DBObject obj1 = (DBObject) JSON.parse(node.toString());
          collection.insert(obj1);
        }
        System.out.println("Done writing to Mongo.");

    } catch (Exception e) {
        e.printStackTrace();
    }
  }

}
