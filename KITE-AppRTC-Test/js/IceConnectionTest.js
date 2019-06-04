const {TestUtils, WebDriverFactory, KiteBaseTest, Status} = require('kite-common');

// Steps & checks
const {OpenAppUrlStep, ConnectToAppRoomStep, GetStatsStep} = require('./steps');
const {PeerConnectionCheck, RemoteVideoDisplayCheck} = require('./checks');

// Pages
const {AppRTCJoinPage, AppRTCMeetingPage} = require('./pages');

// KiteBaseTest config
const globalVariables = TestUtils.getGlobalVariables(process);
const capabilities = require(globalVariables.capabilitiesPath);
const payload = require(globalVariables.payloadPath);

class IceConnectionTest extends KiteBaseTest {
  constructor(name, globalVariables, capabilities, payload) {
    super(name, globalVariables, capabilities, payload);
  }

  async testScript() {
    try {
      this.driver = await WebDriverFactory.getDriver(capabilities, capabilities.remoteAddress);
      this.page = new AppRTCJoinPage(this.driver);

      let openAppUrlStep = new OpenAppUrlStep(this);
      await openAppUrlStep.execute(this);
      let connectToAppRoomStep = new ConnectToAppRoomStep(this);
      await connectToAppRoomStep.execute(this);

      this.page = new AppRTCMeetingPage(this.driver);

      let peerConnectionCheck = new PeerConnectionCheck(this);
      await peerConnectionCheck.execute(this);
      let remoteVideoDisplayCheck = new RemoteVideoDisplayCheck(this);
      await remoteVideoDisplayCheck.execute(this);

      if (this.getStats) {
        let getStatsStep = new GetStatsStep(this);
        await getStatsStep.execute(this);
      }

    } catch (error) {
      console.log(error);
    } finally {
      await this.driver.quit();
    }
  }
}

module.exports = IceConnectionTest;

var test = new IceConnectionTest('IceConnection Test', globalVariables, capabilities, payload);
test.run();
