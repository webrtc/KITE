const {TestUtils, TestStep, Status, KiteTestError} = require('kite-common');
/**
 * Class: GoogleResultPage
 * Extends: TestStep
 * Description:
 */
class GoogleResultPage extends TestStep {
  constructor(kiteBaseTest) {
    super();
    this.driver = kiteBaseTest.driver;
    this.timeout = kiteBaseTest.timeout;
    this.expectedResult = "CoSMo Software | WebRTC Technology & Implementation";
    this.page = kiteBaseTest.page;

    // Test reporter if you want to add attachment(s)
    this.testReporter = kiteBaseTest.reporter;
  }

  stepDescription() {
    return "Open first result on Google result page and verify the page title";
  }

  async step() {
    await this.page.openFirstResult(this.driver);
    let found = await this.page.getTitle(this.driver);
    if (found != this.expectedResult) {
      throw new KiteTestError(Status.FAILED, "The title of the first Google result was not correct: \n" +
      "Expected: " + this.expectedResult + " but found " + found);
    }

    // Screenshot
    let screenshot = await TestUtils.takeScreenshot(this.driver);
    this.testReporter.screenshotAttachment(this.report, "Screenshot step", screenshot); 
  }
}

module.exports = GoogleResultPage;