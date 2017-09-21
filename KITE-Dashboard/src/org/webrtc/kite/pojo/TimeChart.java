package org.webrtc.kite.pojo;

import java.util.List;

/**
 * A class containing information on the date and frequency of the runs in database.
 */
public class TimeChart {
  private List<String> dateList;
  private List<Integer> dateFreqList;

  /**
   * Constructs a new TimeChart object from given information.
   *
   * @param dateList list of dates on which there were tests.
   * @param dateFreqList frequency of these dates' apparition in the database.
   */
  public TimeChart(List<String> dateList, List<Integer> dateFreqList) {
    this.dateList = dateList;
    this.dateFreqList = dateFreqList;
  }

  /**
   * Returns the list of dates.
   */
  public List<String> getDateList() {
    return dateList;
  }

  /**
   * Returns the list of dates' frequency.
   */
  public List<Integer> getDateFreqList() {
    return dateFreqList;
  }
}
