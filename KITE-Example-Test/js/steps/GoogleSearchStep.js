const {TestUtils, TestStep} = require('kite-common');
const {googleSearchPage} = require('../pages');
/**
 * Class: GoogleSearchStep
 * Extends: TestStep
 * Description:
 */
class GoogleSearchStep extends TestStep {
  constructor(kiteBaseTest, url) {
    super();
    this.driver = kiteBaseTest.driver;
    this.timeout = kiteBaseTest.timeout;
    this.url = url;
    this.target = "CoSMo Software Consulting";
  }

  stepDescription() {
    return "Open " + this.url + " and look for " + this.target;
  }

  async step() {
    await TestUtils.open(this);
    await googleSearchPage.searchFor(this);
  }
}

module.exports = GoogleSearchStep;