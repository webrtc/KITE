package org.webrtc.kite.config.test;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.interfaces.JsonBuilder;
import io.cosmosoftware.kite.report.KiteLogger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.openqa.selenium.WebDriver;
import org.webrtc.kite.config.client.Client;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static io.cosmosoftware.kite.util.ReportUtils.getStackTrace;

/**
 * Entity implementation class for Entity: Tuple.
 */
@Entity(name = Tuple.TABLE_NAME)
public class Tuple extends KiteEntity implements JsonBuilder {
  /**
   * The constant TABLE_NAME.
   */
  final static String TABLE_NAME = "tuples";
  private List<Client> clients = new ArrayList<>();
  private String id;
  private String resultId;
  private List<WebDriver> webDrivers;

  protected KiteLogger logger = KiteLogger.getLogger(this.getClass().getName());
  
  /**
   * Instantiates a new Tuple.
   */
  public Tuple() {
    super();
  }
  
  /**
   * Instantiates a new Tuple.
   *
   * @param clients the clients
   */
  public Tuple(List<Client> clients) {
    for (Client c: clients) {
      add(c);
    }
  }
  
  /**
   * Instantiates a new Tuple.
   *
   * @param tuple the tuple
   */
  public Tuple(Tuple tuple) {
    for (Client c: tuple.getClients()) {
      add(c);
    }
  }
  
  /**
   * Instantiates a new Tuple.
   *
   * @param client the client
   * @param size   the size
   */
  public Tuple(Client client, int size) {
    super();
    for (int count = 0; count < size; count++) {
      add(client);
    }
  }
  
  /**
   * Add.
   *
   * @param client the client
   */
  public void add(Client client) {
    //dereference the clients so each can have their own webDriver.
    this.clients.add(new Client(client));
  }
  
  @Override
  public JsonObjectBuilder buildJsonObjectBuilder() throws NullPointerException {
    return Json.createObjectBuilder().add("clients", this.getClientArrayBuilder());
  }
  
  /**
   * Gets client array builder.
   *
   * @return the client array builder
   */
  @Transient
  public JsonArrayBuilder getClientArrayBuilder() {
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (Client client : this.clients) {
      arrayBuilder.add(client.buildJsonObjectBuilder());
    }
    return arrayBuilder;
  }
  
  /**
   * Get client.
   *
   * @param index the index
   *
   * @return the client
   */
  @Transient
  public Client get(int index) {
    return this.clients.get(index);
  }
  
  /**
   * Gets the browsers.
   *
   * @return the browsers
   */
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  public List<Client> getClients() {
    return clients;
  }
  
  /**
   * Sets clients.
   *
   * @param clients the clients
   */
  public void setClients(List<Client> clients) {
    this.clients = clients;
  }
  
  /**
   * Gets the id.
   *
   * @return the id
   */
  @Id
  @GeneratedValue(generator = Tuple.TABLE_NAME)
  @GenericGenerator(name = Tuple.TABLE_NAME, strategy = "io.cosmosoftware.kite.dao.KiteIdGenerator", parameters = {
    @Parameter(name = "prefix", value = "TUPL")})
  public String getId() {
    return this.id;
  }
  
  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }
  
  /**
   * Gets result id.
   *
   * @return the result id
   */
  @Transient
  public String getResultId() {
    return resultId;
  }

  @Transient
  public List<WebDriver> getWebDrivers() {
    try {
      if (this.webDrivers == null) {
        webDrivers = new ArrayList<>();
        for (Client c : this.clients) {
          webDrivers.add(c.getWebDriver());
        }
      }
    } catch (Exception e) {
      logger.error(getStackTrace(e));
    } finally {
      return webDrivers;
    }
  }

  /**
   * Sets result id.
   *
   * @param resultId the result id
   */
  public void setResultId(String resultId) {
    this.resultId = resultId;
  }
  
  
  /**
   * Size int.
   *
   * @return the int
   */
  public int size() {
    return this.clients.size();
  }

  @Transient
  public List<Integer> getMatrix() {
    List<Integer> res = new ArrayList<>();
    for (int i = 0; i < this.clients.size(); i++) {
      res.add(this.clients.get(i).getClientIndex());
    }
    return res;
  }

  public void mergeWith(Tuple another) {
    this.clients.addAll(another.clients);
  }
}
