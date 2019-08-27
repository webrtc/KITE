package org.webrtc.kite.config.test;

import io.cosmosoftware.kite.config.KiteEntity;
import io.cosmosoftware.kite.interfaces.JsonBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.webrtc.kite.config.client.Client;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity implementation class for Entity: Tuple.
 */
@Entity(name = Tuple.TABLE_NAME)
public class Tuple extends KiteEntity implements JsonBuilder {
  /**
   * The constant TABLE_NAME.
   */
  final static String TABLE_NAME = "tuples";
  private List<Client> clients = new ArrayList<Client>();
  private String id;
  private String resultId;
  
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
    this();
    this.clients = clients;
  }
  
  /**
   * Instantiates a new Tuple.
   *
   * @param tuple the tuple
   */
  public Tuple(Tuple tuple) {
    this();
    this.clients = tuple.getClients();
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
      this.clients.add(client);
    }
  }
  
  /**
   * Add.
   *
   * @param client the client
   */
  public void add(Client client) {
    this.clients.add(client);
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
  
    
}
