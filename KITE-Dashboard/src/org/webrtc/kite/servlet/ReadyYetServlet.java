/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webrtc.kite.BrowserMapping;
import org.webrtc.kite.TestMapping;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.BrowserDao;
import org.webrtc.kite.dao.ResultDao;
import org.webrtc.kite.dao.TestDao;
import org.webrtc.kite.dao.WPTDao;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.Browser;
import org.webrtc.kite.pojo.Test;

import javax.json.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Servlet implementation class DashboardServlet */
@WebServlet("/score")
public class ReadyYetServlet extends HttpServlet {

  private static final long serialVersionUID = 3456562049892798394L;
  private static final Log log = LogFactory.getLog(ReadyYetServlet.class);

  /** @see HttpServlet#HttpServlet() */
  public ReadyYetServlet() {
    super();
  }

  /** @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response) */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String testCategory = request.getParameter("testCategory");
    String test = request.getParameter("test");

    List<Test> listOfDistinctTest;
    try {
      listOfDistinctTest =
          new TestDao(Utility.getDBConnection(this.getServletContext())).getDistinctTestList();
      request.setAttribute("listOfTest", listOfDistinctTest);
      JsonObjectBuilder score = Json.createObjectBuilder();
      if (test == null) {
        List<String> WPTList =
            new WPTDao(Utility.getDBConnection(this.getServletContext())).getWPTList();
        List<String> OtherTestList =
            new TestDao(Utility.getDBConnection(this.getServletContext())).getNonWPTList();
        for (Browser browser : BrowserMapping.BrowserList) {
          JsonObjectBuilder browserScore = Json.createObjectBuilder();
          // Map<String, JsonArrayBuilder> scoreArrayMap = new HashMap<>();
          Map<String, List<Integer>> scoreArrayMap = new HashMap<>();
          int browserId =
              new BrowserDao(Utility.getDBConnection(this.getServletContext())).getId(browser);
          if (browserId != -1) {
            if (testCategory == null) {
              // WPT first
              for (String testName : WPTList) {
                JsonObject result =
                    new WPTDao(Utility.getDBConnection(this.getServletContext()))
                        .getLatestTestResultByBrowser(testName, "[" + browserId + "]");
                testName = testName.split("WPT_")[1].replaceAll("_", ".");
                if (result != null) {
                  String category = TestMapping.TestCategoryMapping.get(testName);
                  if (!scoreArrayMap.keySet().contains(category)) {
                    List<Integer> scoreList = new ArrayList<>();
                    scoreList.add(0);
                    scoreList.add(0);
                    // scoreArrayMap.put(category, Json.createArrayBuilder());
                    scoreArrayMap.put(category, scoreList);
                  }

                  List<Integer> scoreList = scoreArrayMap.get(category);
                  scoreList.set(0, scoreList.get(0) + result.getInt("total"));
                  scoreList.set(1, scoreList.get(1) + result.getInt("passed"));
                  // browserScore.add(testName, result);
                }
              }

              // Other Tests
              for (String testName : OtherTestList) {
                int tupleSize =
                    new TestDao(Utility.getDBConnection(this.getServletContext()))
                        .getTupleSizeTestName(testName);
                JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
                jsonObjectBuilder.add("total", 0);
                jsonObjectBuilder.add("passed", 0);
                JsonObject result = null;
                int total = 0;
                int passed = 0;
                if (tupleSize != -1) {
                  if (tupleSize == 1) {
                    String resultString =
                        new ResultDao(Utility.getDBConnection(this.getServletContext()))
                            .getLatestResultByBrowser(testName, "[" + browserId + "]");
                    if (resultString != null) {
                      if (resultString.equalsIgnoreCase("SUCCESSFUL")) {
                        passed++;
                      }
                    }
                    total++;
                  } else {
                    for (List<Browser> browsers :
                        Utility.buildTuples(BrowserMapping.BrowserList, tupleSize)) {
                      if (browsers.contains(browser)) {
                        String resultString = null;
                        JsonArrayBuilder idArray = Json.createArrayBuilder();
                        //browsers.remove(browser);
                        for (Browser browser1 : browsers) {
                          int browser1Id =
                                  new BrowserDao(Utility.getDBConnection(this.getServletContext()))
                                          .getId(browser1);
                          if (browser1Id != -1) {
                            idArray.add(browser1Id);
                          }
                        }
                        JsonArray ids = idArray.build();
                        if (ids.size() == browsers.size()) {
                          resultString =
                                  new ResultDao(Utility.getDBConnection(this.getServletContext()))
                                          .getLatestResultByBrowser(testName, ids.toString());
                        }
                        if (resultString != null) {
                          if (resultString.equalsIgnoreCase("SUCCESSFUL")) {
                            passed++;
                          }
                        }
                        total++;
                      }
                    }
                  }
                  jsonObjectBuilder.add("total", total).add("passed", passed);
                }
                result = jsonObjectBuilder.build();
                if (result != null) {
                  String category = TestMapping.TestCategoryMapping.get(testName);
                  if (!scoreArrayMap.keySet().contains(category)) {
                    List<Integer> scoreList = new ArrayList<>();
                    scoreList.add(0);
                    scoreList.add(0);
                    // scoreArrayMap.put(category, Json.createArrayBuilder());
                    scoreArrayMap.put(category, scoreList);
                  }
                  List<Integer> scoreList = scoreArrayMap.get(category);
                  scoreList.set(0, scoreList.get(0) + result.getInt("total"));
                  scoreList.set(1, scoreList.get(1) + result.getInt("passed"));
                  // browserScore.add(testName, result);
                }
              }
            } else {
              // layer 2
              List<String> testList = TestMapping.TestMapping.get(testCategory);
              for (String originalTestName : testList) {
                String testName =
                    new WPTDao(Utility.getDBConnection(this.getServletContext()))
                        .testExist(originalTestName);
                if (testName != null) {
                  if (testName.startsWith("WPT")) {
                    JsonObject result =
                        new WPTDao(Utility.getDBConnection(this.getServletContext()))
                            .getLatestTestResultByBrowser(testName, "[" + browserId + "]");
                    if (result != null) {
                      if (!scoreArrayMap.keySet().contains(originalTestName)) {
                        List<Integer> scoreList = new ArrayList<>();
                        scoreList.add(0);
                        scoreList.add(0);
                        // scoreArrayMap.put(category, Json.createArrayBuilder());
                        scoreArrayMap.put(originalTestName, scoreList);
                      }
                      List<Integer> scoreList = scoreArrayMap.get(originalTestName);
                      scoreList.set(0, scoreList.get(0) + result.getInt("total"));
                      scoreList.set(1, scoreList.get(1) + result.getInt("passed"));
                      // browserScore.add(testName, result);
                    }
                  } else {
                    int tupleSize =
                        new TestDao(Utility.getDBConnection(this.getServletContext()))
                            .getTupleSizeTestName(testName);
                    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
                    jsonObjectBuilder.add("total", 0);
                    jsonObjectBuilder.add("passed", 0);
                    JsonObject result = null;
                    if (tupleSize != -1) {
                      if (tupleSize == 1) {
                        jsonObjectBuilder.add("total", 1);
                        String resultString =
                            new ResultDao(Utility.getDBConnection(this.getServletContext()))
                                .getLatestResultByBrowser(testName, "[" + browserId + "]");
                        if (resultString != null) {
                          if (resultString.equalsIgnoreCase("SUCCESSFUL")) {
                            jsonObjectBuilder.add("passed", 1);
                          } else {
                            jsonObjectBuilder.add("passed", 0);
                          }
                        }
                      } else {
                        int total = 0;
                        int passed = 0;
                        for (List<Browser> browsers :
                            Utility.buildTuples(BrowserMapping.BrowserList, tupleSize)) {
                          if (browsers.contains(browser)) {
                            String resultString = null;
                            JsonArrayBuilder idArray = Json.createArrayBuilder();
                            //browsers.remove(browser);
                            for (Browser browser1 : browsers) {
                              int browser1Id =
                                      new BrowserDao(Utility.getDBConnection(this.getServletContext()))
                                              .getId(browser1);
                              if (browser1Id != -1) {
                                idArray.add(browser1Id);
                              }
                            }
                            JsonArray ids = idArray.build();
                            if (ids.size() == browsers.size()) {
                              resultString =
                                      new ResultDao(Utility.getDBConnection(this.getServletContext()))
                                              .getLatestResultByBrowser(testName, ids.toString());
                            }
                            if (resultString != null) {
                              if (resultString.equalsIgnoreCase("SUCCESSFUL")) {
                                passed++;
                              }
                            }
                            total++;
                          }
                        }
                        jsonObjectBuilder.add("total", total).add("passed", passed);
                      }
                    }
                    result = jsonObjectBuilder.build();
                    if (result != null) {
                      if (!scoreArrayMap.keySet().contains(originalTestName)) {
                        List<Integer> scoreList = new ArrayList<>();
                        scoreList.add(0);
                        scoreList.add(0);
                        // scoreArrayMap.put(category, Json.createArrayBuilder());
                        scoreArrayMap.put(originalTestName, scoreList);
                      }
                      List<Integer> scoreList = scoreArrayMap.get(originalTestName);
                      scoreList.set(0, scoreList.get(0) + result.getInt("total"));
                      scoreList.set(1, scoreList.get(1) + result.getInt("passed"));
                      // browserScore.add(testName, result);
                    }
                  }
                } else {
                  browserScore.add(
                      originalTestName,
                      Json.createObjectBuilder().add("total", 0).add("passed", 0));
                }
              }
            }
          }
          if (testCategory == null) {
            for (String category : TestMapping.TestMapping.keySet()) {
              if (scoreArrayMap.keySet().contains(category)) {
                List<Integer> scoreList = scoreArrayMap.get(category);
                browserScore.add(
                    category,
                    Json.createObjectBuilder()
                        .add("total", scoreList.get(0))
                        .add("passed", scoreList.get(1))
                        .add("isTest", false));
              } else {
                browserScore.add(
                    category,
                    Json.createObjectBuilder()
                        .add("total", 0)
                        .add("passed", 0)
                        .add("isTest", false));
              }
            }
          } else {
            // layer 2
            List<String> testList = TestMapping.TestMapping.get(testCategory);
            for (String testName : testList) {
              List<Integer> scoreList = scoreArrayMap.get(testName);
              if (scoreList == null) {
                browserScore.add(
                    testName,
                    Json.createObjectBuilder()
                        .add("total", 0)
                        .add("passed", 0)
                        .add("isTest", true));
              } else {
                browserScore.add(
                    testName,
                    Json.createObjectBuilder()
                        .add("total", scoreList.get(0))
                        .add("passed", scoreList.get(1))
                        .add("isTest", true));
              }
            }
          }
          score.add(browser.getDetailedName(), browserScore);
        }
        score.add("isTest", false);
        if (testCategory != null){
          score.add("path", testCategory);
        }
        request.setAttribute("scoreJson", score.build().toString());
      } else {
        String testName =
          new WPTDao(Utility.getDBConnection(this.getServletContext()))
              .testExist(test);
        for (Browser browser : BrowserMapping.BrowserList) {
          int browserId =
              new BrowserDao(Utility.getDBConnection(this.getServletContext())).getId(browser);
          if (browserId != -1 && testName!=null) {
            JsonObject result =null;
            if (testName.startsWith("WPT")) {
               result =
                  new WPTDao(Utility.getDBConnection(this.getServletContext()))
                      .getLatestTestResultByBrowser(testName, "[" + browserId + "]");
               if (result != null) {
                 score.add(browser.getDetailedName(), Json.createObjectBuilder().add(test, result));
               }
            } else {
              int tupleSize =
                  new TestDao(Utility.getDBConnection(this.getServletContext()))
                      .getTupleSizeTestName(testName);
              JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
              if (tupleSize != -1) {
                if (tupleSize == 1) {
                  String resultString =
                      new ResultDao(Utility.getDBConnection(this.getServletContext()))
                          .getLatestResultByBrowser(testName, "[" + browserId + "]");
                  if (resultString!=null){
                    jsonObjectBuilder.add(test, resultString);
                  } else {
                    jsonObjectBuilder.add(test, Json.createObjectBuilder());
                  }
                  JsonObjectBuilder resultObject = Json.createObjectBuilder();
                  resultObject.add("tests", Json.createObjectBuilder().add(test,jsonObjectBuilder));
                  score.add(browser.getDetailedName(), Json.createObjectBuilder().add(test,resultObject) );
                } else {
                  int total = 0;
                  int passed = 0;
                  for (List<Browser> browsers :
                      Utility.buildTuples(BrowserMapping.BrowserList, tupleSize)) {
                    if (browsers.contains(browser)) {
                      String resultString = null;
                      JsonArrayBuilder idArray = Json.createArrayBuilder();
                      //browsers.remove(browser);
                      for (Browser browser1 : browsers) {
                        int browser1Id =
                            new BrowserDao(Utility.getDBConnection(this.getServletContext()))
                                .getId(browser1);
                        if (browser1Id != -1) {
                          idArray.add(browser1Id);
                        }
                      }
                      JsonArray ids = idArray.build();
                      if (ids.size() == browsers.size()) {
                        resultString =
                                new ResultDao(Utility.getDBConnection(this.getServletContext()))
                                        .getLatestResultByBrowser(testName, ids.toString());
                      }
                      if (resultString != null) {
                        if (resultString.equalsIgnoreCase("SUCCESSFUL")) {
                          passed++;
                        }
                      }
                      total++;
                    }
                  }
                  jsonObjectBuilder.add(test, passed+ "/" + total);
                  JsonObjectBuilder resultObject = Json.createObjectBuilder();
                  resultObject.add("tests", Json.createObjectBuilder().add(test,jsonObjectBuilder));
                  score.add(browser.getDetailedName(), Json.createObjectBuilder().add(test,resultObject) );
                }
              }
            }
          } else {
            JsonObjectBuilder resultObject = Json.createObjectBuilder();
            resultObject.add("tests", Json.createObjectBuilder().add(test,Json.createObjectBuilder()));
            score.add(browser.getDetailedName(), Json.createObjectBuilder().add(test,resultObject) );
          }
        }
        score.add("isTest", true).add("test", test).add("path", testCategory);
        request.setAttribute("scoreJson", score.build().toString());
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    if (log.isDebugEnabled()) log.debug("Displaying: score.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("score.vm");
    requestDispatcher.forward(request, response);
  }
}
