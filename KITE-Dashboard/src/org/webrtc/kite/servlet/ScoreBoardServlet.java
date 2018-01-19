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
import org.webrtc.kite.OverviewResult;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.ConfigTestDao;
import org.webrtc.kite.dao.PublicOverviewDao;
import org.webrtc.kite.dao.ResultTableDao;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.ConfigTest;
import org.webrtc.kite.pojo.Score;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet implementation class DashboardServlet
 */
@WebServlet("/score")
public class ScoreBoardServlet extends HttpServlet {

  private static final long serialVersionUID = 3456562049892798394L;
  private static final Log log = LogFactory.getLog(ScoreBoardServlet.class);

  /**
   * @see HttpServlet#HttpServlet()
   */
  public ScoreBoardServlet() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {


    List<Score> scoreList= new ArrayList<Score>();
    List<ConfigTest> listOfDistinctTest;
    try {
      listOfDistinctTest = new ConfigTestDao(Utility.getDBConnection(this.getServletContext())).getTestList();
      for (ConfigTest test: listOfDistinctTest){
        Long lastRun = new PublicOverviewDao(Utility.getDBConnection(this.getServletContext())).getRunList(test.getTestName()).get(0);
        OverviewResult tmp = new OverviewResult(
                new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getResultList("TN"+test.getTestName()+"_"+lastRun.toString(), test.getTupleSize()));
        //OverviewResult tmp = new OverviewResult(new OverviewDao(Utility.getDBConnection(this.getServletContext())).getOverviewResultList(test.getTestName(), test.getTupleSize()),true);
        tmp.stableFilter();
        Score tmpScore= new Score(test, tmp);
        scoreList.add(tmpScore);
      }
      String scoreJson="{";
      for (Score score:scoreList){
        scoreJson+="\""+score.getTestName()+"\":";
        scoreJson+=score.getScoreJson();
        if (scoreList.indexOf(score)<scoreList.size()-1) {
          scoreJson+=",";
        }
      }
      scoreJson+="}";
      request.setAttribute("scoreJson", scoreJson);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      throw new KiteSQLException(e.getLocalizedMessage());
    }

    if (log.isDebugEnabled())
      log.debug("Displaying: score.vm");
    RequestDispatcher requestDispatcher = request.getRequestDispatcher("score.vm");
    requestDispatcher.forward(request, response);
  }

}
