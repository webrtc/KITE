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
import org.webrtc.kite.Utility;
import org.webrtc.kite.dao.ClientVersionDao;
import org.webrtc.kite.exception.KiteSQLException;
import org.webrtc.kite.pojo.ClientVersion;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet implementation class DashboardServlet
 */
@WebServlet("/versions")
public class ClientVersionServlet extends HttpServlet {

    private static final long serialVersionUID = -6356946115085869023L;
    private static final Log log = LogFactory.getLog(ConfiguratorServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClientVersionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        List<ClientVersion> clientVersionList;
        try {
            clientVersionList =
                    new ClientVersionDao(Utility.getDBConnection(this.getServletContext())).getClientVersionList();
            if (log.isDebugEnabled())
                log.debug("out->: clientVersionList" + clientVersionList);
            request.setAttribute("clientVersionList", clientVersionList);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new KiteSQLException(e.getLocalizedMessage());
        }

        // get UI
        if (log.isDebugEnabled())
            log.debug("Displaying: client_version.vm");
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("client_version.vm");
        requestDispatcher.forward(request, response);
    }

}
