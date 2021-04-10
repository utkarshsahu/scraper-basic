package com.example.webscraper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.Block;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import org.bson.Document;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
// import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import us.codecraft.xsoup.Xsoup;

public class App1 {
  public static void main(String... args) throws Exception{
    ApiUrlGenerator urlGen = new ApiUrlGenerator(true, "", "", "UPCOMING", "", "", "YES", "NCERT", 105);
    String jsonResponse = fetchFromApi(urlGen);
    mongoInsert(jsonResponse);
    // scrape();
  }


 static String fetchFromApi(ApiUrlGenerator urlGen) throws Exception {

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

static void mongoInsert(String jsonResponse) {
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

static void scrape() {
  try {
    Mongo mongo = new Mongo("localhost", 27017);
    DB db = mongo.getDB("test");
    DBCollection collection = db.getCollection("courses");

    collection.find().forEach(it -> {

        String url1 = it.get("url").toString();
        org.jsoup.nodes.Document doc1 = null;
        try {
          doc1 = Jsoup.connect(url1).get();

        } catch (IOException e) {
          e.printStackTrace();
        }

        Elements result = Xsoup.compile("[@class=\"courseTitle\"]|[@class=\"instructorName\"").evaluate(doc1).getElements();
        System.out.println("-----" + doc1.title() + "---------");

        for (Element elem : result) {
          String which_title = elem.text();

          if (which_title.equals("Books and references")) {

            Element par = elem.parent();
            Elements nodes = par.getElementsByClass("previewContent");
            JSONArray arr = new JSONArray();
            for(Element node: nodes) {
              arr.put(node.text());
            }
            // System.out.println(arr.toString());
            it.put("booksAndReferences", arr);
          }
          if (which_title.equalsIgnoreCase("Course layout")) {
            // Element par = elem.parent();
            // Elements nodes = par.getElementsByClass("previewContent");
            // JSONArray arr = new JSONArray();
            // for(Element node: nodes) {
            //   arr.put(node.text());
            // }
            // it.put("booksAndReferences", arr);
          }
          if (which_title.equalsIgnoreCase("Course certificate")) {
            Element par = elem.parent();
            Elements nodes = par.getElementsByClass("previewContent");
            String s = "";
            for(Element node: nodes) {
              s += (node.text() + " ");
            }
            // System.out.println(s);
            it.put("courseCertificate", s);
          }
          String instructorName = "";
          if (elem.hasClass("instructorName")) {
            Pattern pattern = Pattern.compile("By (.*)\\s*\\|");
            Matcher matcher = pattern.matcher(which_title);
            if (matcher.find())
            {
                System.out.println(matcher.group(1));
                instructorName = matcher.group(1);
                it.put("instructorName", matcher.group(1));
            }
            // which_title.fin
           }

          if (which_title.equalsIgnoreCase("Instructor bio")) {
            Element par = elem.parent();
            Elements nodes = par.getElementsContainingText(instructorName);
            // JSONArray arr = new JSONArray();
            String s = "";
            for(Element node: nodes) {
              s += (node.text() + " ");
            }
            // System.out.println(s);
            it.put("instructorBio", s);
         }
         collection.save(it);
        }

    });
  } catch(Exception e) {
    e.printStackTrace();
  }
}
}
