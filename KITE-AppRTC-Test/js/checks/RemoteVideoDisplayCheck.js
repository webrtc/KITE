const {TestUtils, TestStep} = require('kite-common');
const {apprtcMeetingPage} = require('../pages');

/**
 * Class: RemoteVideoDisplayCheck
 * Extends: TestStep
 * Description:
 */
class RemoteVideoDisplayCheck extends TestStep {
  constructor(kiteBaseTest) {
    super();
    this.driver = kiteBaseTest.driver;

    // Test reporter if you want to add attachment(s)
    this.testReporter = kiteBaseTest.reporter;
  }

  stepDescription() {
    return 'Verify that the remote video is actually playing';
  }

  async step() {
    let videoCheck = await apprtcMeetingPage.verifyRemoteVideoDisplay(this.driver);
    let screenshot = await TestUtils.takeScreenshot(this.driver);
    this.testReporter.jsonAttachment(this.report, 'videoCheck', videoCheck);
    this.testReporter.screenshotAttachment(this.report, 'remote-video-display-check', screenshot);
  }

}

module.exports = RemoteVideoDisplayCheck;