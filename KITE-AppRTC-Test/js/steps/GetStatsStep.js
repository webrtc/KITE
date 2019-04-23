const {TestUtils, TestStep} = require('kite-common');

/**
 * Class: GetStapStep
 * Extends: TestStep
 * Description:
 */
class GetStatsStep extends TestStep {
  constructor(driver, statsCollectionDuration, statsCollectionInterval) {
    super();
    this.driver = driver;
    this.statsCollectionDuration = statsCollectionDuration;
    this.statsCollectionInterval = statsCollectionInterval;
    this.pc = "appController.call_.pcClient_.pc_";
  }

  stepDescription() {
    return "Get the peer connection's stats";
  }

  async step(allureTestReport, reporter) {
    let getStats = await TestUtils.getStats(this.driver, this.pc, this.statsCollectionDuration, this.statsCollectionInterval);
    reporter.textAttachment(this.report, 'Peer connection\'s stats', JSON.stringify(getStats), "json");
  }
}

module.exports = GetStatsStep;