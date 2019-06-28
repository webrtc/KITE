package org.webrtc.kite;

import io.cosmosoftware.kite.report.KiteLogger;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class TestRunThread implements Callable<List<Future<Object>>> {
  private final KiteLogger logger = KiteLogger.getLogger(this.getClass().getName());
  private final TestConfig testConfig;
  private final List<Tuple> tupleList;
  private String name;
  
  public TestRunThread(TestConfig testConfig, List<Tuple> tupleList) {
    this.testConfig = testConfig;
    this.tupleList = tupleList;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  @Override
  public List<Future<Object>> call() {
    List<Future<Object>> listOfResults = null;
    try {
      logger.info("Running " + testConfig + " ...");
      listOfResults = new MatrixRunner(testConfig, tupleList, this.name).run();
    } catch (Exception e) {
      logger.fatal("Error [Interruption]: The execution has been interrupted with the "
        + "following error: " + e.getLocalizedMessage(), e);
    }
    
    StringBuilder testResults = new StringBuilder("The following are results for " + testConfig + ":\n");
    
    if (listOfResults != null) {
      List<Future<Object>> temp = new ArrayList<>(listOfResults);
      for (Future<Object> future : temp) {
        try {
          testResults.append("\r\n").append(future.get().toString());
        } catch (Exception e) {
          logger.error("Exception while test execution", e);
        }
      }
      testResults.append("\r\nEND OF RESULTS\r\n");
      logger.debug(testResults.toString());
    } else {
      logger.warn("No test case was found.");
    }
    return listOfResults;
  }
}
