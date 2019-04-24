const {TestUtils, TestStep} = require('kite-common');
const {apprtcMeetingPage} = require('../pages');

/**
 * Class: RemoteVideoDisplayCheck
 * Extends: TestStep
 * Description:
 */
class RemoteVideoDisplayCheck extends TestStep {
  constructor(driver) {
    super();
    this.driver = driver;
  }

  stepDescription() {
    return 'Verify that the remote video is actually playing';
  }

  async step(allureTestReport, reporter) {
    let videoCheck = await apprtcMeetingPage.verifyRemoteVideoDisplay(this.driver);
    let screenshot = await TestUtils.takeScreenshot(this.driver);
    reporter.jsonAttachment(this.report, 'videoCheck', videoCheck);
    reporter.screenshotAttachment(this.report, 'remote-video-display-check', screenshot);
  }

}

module.exports = RemoteVideoDisplayCheck;