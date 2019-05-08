const {TestUtils, TestStep} = require('kite-common');


/**
 * Class: OpenAppUrlStep
 * Extends: TestStep
 * Description:
 */
class OpenAppUrlStep extends TestStep {
  constructor(KiteBaseTest) {
    super();
    this.driver = KiteBaseTest.driver;
    this.url = KiteBaseTest.url;
    this.timeout = KiteBaseTest.timeout;
  }

  stepDescription() {
    return 'Open the apprtc url';
  }

  async step() {
    await TestUtils.open(this);
  }
}

module.exports = OpenAppUrlStep;