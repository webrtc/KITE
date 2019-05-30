package org.webrtc.kite.config;

import java.util.ArrayList;
import java.util.List;

public class Tuple extends ArrayList<EndPoint> {
  
  public Tuple() {
    super();
  }
  
  public Tuple(List<EndPoint> endPoints) {
    super();
    addAll(endPoints);
  }
  
  public Tuple(EndPoint endPoint, int size) {
    super();
    for (int count = 0 ; count < size; count++) {
      if (endPoint instanceof Browser) {
        add(new Browser( (Browser) endPoint));
      } else {
        add(new App( (App) endPoint));
        
      }
    }
  }
  

}
