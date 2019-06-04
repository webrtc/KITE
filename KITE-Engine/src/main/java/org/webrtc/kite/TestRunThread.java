package org.webrtc.kite;

import org.apache.log4j.Logger;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.config.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class TestRunThread implements Callable<Object> {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private final TestConf testConf;
  private final List<Tuple> tupleList;
  
  public TestRunThread(TestConf testConf, List<Tuple> tupleList) {
    this.testConf = testConf;
    this.tupleList = tupleList;
  }
  public TestRunThread(TestConf testConf, Tuple tuple) {
    this.testConf = testConf;
    this.tupleList = new ArrayList<>();
    this.tupleList.add(tuple);
  }
  
  @Override
  public Object call() {
    List<Future<Object>> listOfResults = null;
    try {
      logger.info("Running " + testConf + " ...");
      listOfResults = new MatrixRunner(testConf, tupleList).run();
    } catch (Exception e) {
      logger.fatal("Error [Interruption]: The execution has been interrupted with the "
        + "following error: " + e.getLocalizedMessage(), e);
    }
  
    StringBuilder testResults = new StringBuilder("The following are results for " + testConf + ":\n");
    if (listOfResults != null) {
      for (Future<Object> future : listOfResults) {
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
    return testResults;
  }
}
