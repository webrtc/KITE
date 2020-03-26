/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.paas;

import io.cosmosoftware.kite.report.KiteLogger;
import org.openqa.selenium.Platform;
import org.webrtc.kite.Utils;
import org.webrtc.kite.config.client.Client;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The Class PaasHandler.
 */
public abstract class PaasHandler implements Callable<Object> {
  
  /**
   * The Constant logger.
   */
  private static final KiteLogger logger = KiteLogger.getLogger(PaasHandler.class.getName());
  /**
   * The client list.
   */
  protected List<Client> clientList = new ArrayList<Client>();
  /**
   * The paas.
   */
  protected Paas paas;
  /**
   * The path to DB.
   */
  private String pathToDB;
  
  /**
   * Instantiates a new paas handler.
   *
   * @param pathToDB the path to DB
   * @param paas     the paas
   */
  public PaasHandler(String pathToDB, Paas paas) {
    this.pathToDB = pathToDB;
    this.paas = paas;
  }
  
  /*
   * (non-Javadoc)
   *
   * @see java.util.concurrent.Callable#call()
   */
  @Override
  public Object call() throws Exception {
    if (!this.isUpdatedInLast24h()) {
      logger.info("Fetching config for: " + this.paas);
      this.fetchConfig();
      this.createAndFillTable();
    }
    return "";
  }
  
  /**
   * Check table exist.
   *
   * @param c the c
   *
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  private boolean checkTableExist(Connection c) throws SQLException {
    String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = '" + this.paas.getType().name()
      + "';";
    
    Statement s = null;
    ResultSet rs = null;
    try {
      s = c.createStatement();
      rs = s.executeQuery(sql);
      if (rs.next())
        return true;
      else
        return false;
    } finally {
      Utils.closeDBResources(s, rs);
    }
  }
  
  /**
   * Clear table.
   *
   * @param c the c
   *
   * @throws SQLException the SQL exception
   */
  private void clearTable(Connection c) throws SQLException {
    String sql = "DELETE FROM " + this.paas.getType().name() + ";";
    
    Statement s = null;
    try {
      s = c.createStatement();
      s.executeUpdate(sql);
    } finally {
      Utils.closeDBResources(s, null);
    }
  }
  
  /**
   * Creates the and fill table.
   *
   * @throws SQLException the SQL exception
   */
  private void createAndFillTable() throws SQLException {
    Connection c = null;
    try {
      c = this.getDatabaseConnection();
      this.createTableIfNotExists(c);
      try {
        // begin transaction
        c.setAutoCommit(false);
        this.clearTable(c);
        this.insertValues(c);
        c.commit();
        // end transaction
      } finally {
        try {
          c.rollback();
        } catch (SQLException sqle) {
          logger.warn("SQLException while rolling back", sqle);
        }
        c.close();
      }
    } finally {
      if (c != null)
        c.close();
    }
  }
  
  /**
   * Creates the table if not exists.
   *
   * @param c the c
   *
   * @throws SQLException the SQL exception
   */
  private void createTableIfNotExists(Connection c) throws SQLException {
    String sql = "CREATE TABLE IF NOT EXISTS " + this.paas.getType().name() + "(BROWSER       TEXT    NOT NULL, "
      + " VERSION       TEXT, " + " PLATFORM      TEXT, " + " PLATFORM_TYPE TEXT, "
      + " LAST_UPDATE   INTEGER);";
    
    Statement s = null;
    try {
      s = c.createStatement();
      s.executeUpdate(sql);
    } finally {
      Utils.closeDBResources(s, null);
    }
  }
  
  /**
   * Fetch config.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract void fetchConfig() throws IOException;
  
  /**
   * Gets the available config list.
   *
   * @param username  the username
   * @param accesskey the accesskey
   *
   * @return the available config list
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected List<JsonObject> getAvailableConfigList(String username, String accesskey) throws IOException {
    URL myurl = new URL(this.paas.getType().restApiUrl());
    
    JsonReader reader = null;
    List<JsonObject> availableConfigList = null;
    
    HttpURLConnection con = null;
    InputStream is = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      if (username != null && accesskey != null)
        Authenticator.setDefault(new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, accesskey.toCharArray());
          }
        });
      con = (HttpURLConnection) myurl.openConnection();
      is = con.getInputStream();
      isr = new InputStreamReader(is);
      br = new BufferedReader(isr);
      reader = Json.createReader(br);
      availableConfigList = reader.readArray().getValuesAs(JsonObject.class);
    } finally {
      if (reader != null)
        reader.close();
      if (br != null)
        try {
          br.close();
        } catch (IOException ioe) {
          logger.warn(ioe.getClass().getSimpleName(), ioe);
        }
      if (isr != null)
        try {
          isr.close();
        } catch (IOException ioe) {
          logger.warn(ioe.getClass().getSimpleName(), ioe);
        }
      if (is != null)
        try {
          is.close();
        } catch (IOException ioe) {
          logger.warn(ioe.getClass().getSimpleName(), ioe);
        }
      if (con != null)
        con.disconnect();
    }
    
    return availableConfigList;
  }
  
  /**
   * Gets the database connection.
   *
   * @return the database connection
   * @throws SQLException the SQL exception
   */
  private Connection getDatabaseConnection() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:" + pathToDB);
  }
  
  /**
   * Gets the paas.
   *
   * @return the paas
   */
  public Paas getPaas() {
    return paas;
  }
  
  /**
   * Insert values.
   *
   * @param c the c
   *
   * @throws SQLException the SQL exception
   */
  private void insertValues(Connection c) throws SQLException {
    String sql = "INSERT INTO " + this.paas.getType().name()
      + "(BROWSER, VERSION, PLATFORM, PLATFORM_TYPE, LAST_UPDATE) " + "VALUES " + "(?, ?, ?, ?, ?);";
    
    PreparedStatement ps = null;
    try {
      ps = c.prepareStatement(sql);
      for (Client client : clientList) {
        ps.setString(1, client.getBrowserSpecs().getBrowserName());
        ps.setString(2, client.getBrowserSpecs().getVersion());
        ps.setString(3, client.getBrowserSpecs().getPlatform().name());
        ps.setString(4, client.retrieveFamilyOrPlatform().name());      
        ps.setLong(5, System.currentTimeMillis());
        ps.executeUpdate();
      }
    } finally {
      Utils.closeDBResources(ps, null);
    }
  }
  
  /**
   * Checks if is updated in last 24 h.
   *
   * @return true, if is updated in last 24 h
   * @throws SQLException the SQL exception
   */
  private boolean isUpdatedInLast24h() throws SQLException {
    Connection c = null;
    Statement s = null;
    ResultSet rs = null;
    try {
      c = this.getDatabaseConnection();
      
      if (this.checkTableExist(c)) {
        String sql = "SELECT * FROM " + this.paas.getType().name() + " LIMIT 1;";
        s = c.createStatement();
        rs = s.executeQuery(sql);
        if (rs.next())
          return (System.currentTimeMillis() - rs.getLong("LAST_UPDATE") < 86400000);
        else
          return false;
      } else
        return false;
    } finally {
      Utils.closeDBResources(s, rs);
      if (c != null)
        c.close();
    }
  }
  
  /**
   * Search.
   *
   * @param client the client
   *
   * @return true, if successful
   *
   * @throws SQLException the SQL exception
   */
  public boolean search(Client client) throws SQLException {
    boolean result = false;
    if (!client.isApp()) {
      String browserName = client.getBrowserSpecs().getBrowserName();
      String version = client.getBrowserSpecs().getVersion().split("\\.")[0].trim();
      Platform platform = client.getBrowserSpecs().getPlatform();
      
      String sql = "SELECT * FROM " + this.paas.getType().name() + " WHERE BROWSER = '" + browserName + "'";
      if (version != null && !version.isEmpty())
        sql += " AND VERSION LIKE '" + version + "%'";
      if (platform != Platform.ANY) {
        Platform family = platform.family();
        if (family == null)
          sql += " AND PLATFORM_TYPE = '" + platform.name() + "'";
        else
          sql += " AND PLATFORM = '" + platform.name() + "'";
      }
      sql += ";";
      
      Connection c = null;
      Statement s = null;
      ResultSet rs = null;
      try {
        c = this.getDatabaseConnection();
        s = c.createStatement();
        rs = s.executeQuery(sql);
        if (rs.next())
          result = true;
      } finally {
        Utils.closeDBResources(s, rs);
        if (c != null)
          c.close();
      }
    }
    return result;
  }
  
}
