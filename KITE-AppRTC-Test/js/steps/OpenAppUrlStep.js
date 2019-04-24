const {TestStep} = require('kite-common');
const {apprtcJoinPage} = require('../pages');


/**
 * Class: OpenAppUrlStep
 * Extends: TestStep
 * Description:
 */
class OpenAppUrlStep extends TestStep {
  constructor(driver, url, timeout) {
    super();
    this.driver = driver
    this.url = url;
    this.timeout = timeout;
  }

  stepDescription() {
    return 'Open the apprtc url';
  }

  async step() {
    await apprtcJoinPage.open(this.driver, this.url, this.timeout);
  }
}

module.exports = OpenAppUrlStep;