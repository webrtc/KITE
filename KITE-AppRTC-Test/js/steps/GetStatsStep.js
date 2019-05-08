const {TestUtils, TestStep} = require('kite-common');

/**
 * Class: GetStapStep
 * Extends: TestStep
 * Description:
 */
class GetStatsStep extends TestStep {
  constructor(kiteBaseTest) {
    super();
    this.driver = kiteBaseTest.driver;
    this.statsCollectionTime = kiteBaseTest.statsCollectionTime;
    this.statsCollectionInterval = kiteBaseTest.statsCollectionInterval;
    this.pc = "appController.call_.pcClient_.pc_";
    this.selectedStats = kiteBaseTest.selectedStats;

    // Test reporter if you want to add attachment(s)
    this.testReporter = kiteBaseTest.reporter;
  }

  stepDescription() {
    return "Get the peer connection's stats";
  }

  async step() {
    let getStats = await TestUtils.getStats(this);
    this.testReporter.textAttachment(this.report, 'Peer connection\'s stats', JSON.stringify(getStats), "json");
  }
}

module.exports = GetStatsStep;