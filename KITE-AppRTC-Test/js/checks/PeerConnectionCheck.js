const {TestUtils, TestStep, Status} = require('kite-common');
const {apprtcMeetingPage} = require('../pages');


/**
 * Class: PeerConnectionCheck
 * Extends: TestStep
 * Description:
 */
class PeerConnectionCheck extends TestStep {
  constructor(driver, timeout) {
    super();
    this.driver = driver;
    this.timeout = timeout;
  }

  stepDescription() {
    return "Verify that the ICE connection state is 'connected'";
  }

  async step(allureTestReport, reporter) {
    let state;
    let time = 0;
    let notFinished = true;
    while(time < this.timeout && notFinished) {

      state = await apprtcMeetingPage.getIceConnectionState(this.driver);

      if (state === "failed") {
        console.log("The ICE connection failed");
        allureTestReport.status = Status.FAILED;
        notFinished = false;
      }
      if (state === "connected" || state === "completed") {
        console.log('Success ! ' + 'State: ' + state);
        notFinished = false;
      }

      // Every 1 sec
      await TestUtils.waitAround(1000)
      time++;
    }

    if (time == this.timeout) {
      result = 'failed';
      console.log("Time out !");
      allureTestReport.status = Status.FAILED;
    }
    else {
      console.log("Peer connection ckecked !");
    }
  }
}

module.exports = PeerConnectionCheck;