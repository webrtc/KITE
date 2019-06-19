/*
 * Copyright 2018 Cosmo Software
 */

package org.webrtc.kite.config.paas;

import io.cosmosoftware.kite.report.KiteLogger;
import io.cosmosoftware.kite.util.CircularLinkedList;
import org.webrtc.kite.config.client.Client;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The Class PaasManager.
 */
public class PaasManager {
  
  /**
   * The Constant logger.
   */
  private static final KiteLogger logger = KiteLogger.getLogger(PaasManager.class.getName());
  
  /**
   * The paas handler list.
   */
  private List<PaasHandler> paasHandlerList;
  
  /**
   * Instantiates a new paas manager.
   *
   * @param paasHandlerList the paas handler list
   */
  public PaasManager(List<PaasHandler> paasHandlerList) {
    this.paasHandlerList = paasHandlerList;
  }
  
  /**
   * Assign paas.
   *
   * @param pathToDB   the path to DB
   * @param clientList the client list
   * @param paasList   the paas list
   */
  public static void assignPaas(String pathToDB, List<Client> clientList, List<Paas> paasList) {
    
    List<PaasHandler> paasHandlerList = new ArrayList<>();
    for (Paas paas : paasList) {
      PaasHandler paasHandler = paas.makePaasHandler(pathToDB);
      if (paasHandler != null)
        paasHandlerList.add(paasHandler);
    }
    
    if (paasHandlerList.size() <= 0) {
      logger.info("All Paas are appeared to be local");
      CircularLinkedList<Paas> localPass = new CircularLinkedList(paasList);
      for (Client client : clientList) {
        if (client.getPaas() == null) {
          client.setPaas(localPass.get());
        }
      }
    } else {
      PaasManager paasManager = new PaasManager(paasHandlerList);
      paasManager.communicateWithRemotes();
      
      for (Client client : clientList) {
        if (client.getPaas() == null) {
          Paas paas = paasManager.findAppropriatePaas(client);
          if (paas != null)
            client.setPaas(paas);
        }
      }
    }
  }
  
  /**
   * Communicate with remotes.
   */
  public void communicateWithRemotes() {
    List<Future<Object>> futureObjectList = null;
    ExecutorService executorService = Executors.newFixedThreadPool(this.paasHandlerList.size());
    try {
      futureObjectList = executorService.invokeAll(this.paasHandlerList);
    } catch (InterruptedException e) {
      logger.warn(e.getClass().getSimpleName(), e);
    } finally {
      executorService.shutdown();
    }
    
    if (futureObjectList != null) {
      for (Future<Object> future : futureObjectList) {
        try {
          future.get();
        } catch (Exception e) {
          logger.error(e.getClass().getSimpleName(), e);
        }
      }
    }
  }
  
  /**
   * Find appropriate paas.
   *
   * @param client the client
   *
   * @return the paas
   */
  public Paas findAppropriatePaas(Client client) {
    for (PaasHandler paasHandler : this.paasHandlerList) {
      try {
        if (paasHandler.search(client)) {
          return paasHandler.getPaas();
        }
      } catch (SQLException e) {
        logger.warn("SQLException while searching for " + paasHandler.getClass().getName(), e);
      }
    }
    return null;
  }
  
}
