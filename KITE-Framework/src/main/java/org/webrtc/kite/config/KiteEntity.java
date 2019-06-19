/*
 * Copyright (C) CoSMo Software Consulting Pte. Ltd. - All Rights Reserved
 */

package org.webrtc.kite.config;

import io.cosmosoftware.kite.exception.BadEntityException;
import io.cosmosoftware.kite.interfaces.EntityValidator;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * The Class KiteEntity.
 */
public abstract class KiteEntity implements EntityValidator, Serializable, Cloneable {
  
  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;
  
  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new JSONObject(this).toString();
  }
  
  /*
   * (non-Javadoc)
   *
   * @see io.cosmosoftware.kite.interfaces.EntityValidator#validate()
   */
  @Override
  public void validate() throws BadEntityException {
    // Do Nothing
  }
  
  
}
