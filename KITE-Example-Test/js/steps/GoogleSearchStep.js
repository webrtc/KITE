const {TestStep} = require('kite-common');
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
    this.page = kiteBaseTest.page;
    this.target = "CoSMo Software Consulting";
  }

  stepDescription() {
    return "Open " + this.url + " and look for " + this.target;
  }

  async step() {
    await this.page.open(this);
    await this.page.searchFor(this.target);
  }
}

module.exports = GoogleSearchStep;