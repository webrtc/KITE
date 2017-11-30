

package org.webrtc.kite.servlet;
import org.webrtc.kite.OverviewResult;
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.OverviewDao;
import org.webrtc.kite.dao.ResultTableDao;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteSQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/getjson")
public class GetJsonDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetJsonDataServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub


        String testName = request.getParameter("name");
        if (testName == null)
            throw new KiteNoKeyException("name");
        String strSize = request.getParameter("size");
        if (strSize == null)
            throw new KiteNoKeyException("tuple size");
        int size = Integer.parseInt(strSize);
        String strval = request.getParameter("val");
        if (strval == null)
            throw new KiteNoKeyException("value");
        String location = request.getParameter("location");
        if (location == null)
            throw new KiteNoKeyException("location");
        OverviewResult listOfResult;
        try{
            if(location.equalsIgnoreCase("result"))
                listOfResult = new OverviewResult(new ResultTableDao(Utility.getDBConnection(this.getServletContext())).getJsonResultList(testName,size, strval),false, false);
            else
                listOfResult = new OverviewResult(new OverviewDao(Utility.getDBConnection(this.getServletContext())).getRequestedOverviewResultList(testName, size,strval),true, false);

            if (listOfResult.getListOfResultTable().size()>0)
                response.getWriter().print(listOfResult.getJsonData());
            else {
                String res="{";
                res+="\"name\":\"result\",";
                res+="\"children\": []}";
                response.getWriter().print(res);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new KiteSQLException(e.getLocalizedMessage());
        }


    }
}
