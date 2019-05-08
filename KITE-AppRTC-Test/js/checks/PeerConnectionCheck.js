const {TestUtils, TestStep, Status, KiteTestError} = require('kite-common');
const {apprtcMeetingPage} = require('../pages');


/**
 * Class: PeerConnectionCheck
 * Extends: TestStep
 * Description:
 */
class PeerConnectionCheck extends TestStep {
  constructor(kiteBaseTest) {
    super();
    this.driver = kiteBaseTest.driver;
    this.timeout = kiteBaseTest.timeout;
  }

  stepDescription() {
    return "Verify that the ICE connection state is 'connected'";
  }

  async step() {
    let state;
    let time = 0;

    while(time < this.timeout) {

      state = await apprtcMeetingPage.getIceConnectionState(this.driver);

      if (state === "failed") {
        console.log("The ICE connection failed");
        throw new KiteTestError(Status.FAILED, "The ICE connection failed");
      }
      if (state === "connected" || state === "completed") {
        console.log('Success ! ' + 'State: ' + state);
        break;
      }

      // Every 1 sec
      await TestUtils.waitAround(1000)
      time++;
    }

    if (time === this.timeout) {
      throw new KiteTestError(Status.FAILED, "The ICE connection failed (Time out)");
    }
    else {
      console.log("Peer connection ckecked !");
    }
  }
}

module.exports = PeerConnectionCheck;