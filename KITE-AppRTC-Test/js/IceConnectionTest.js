const {TestUtils, WebDriverFactory, KiteBaseTest} = require('kite-common');
// Steps & checks
const {OpenAppUrlStep, ConnectToAppRoomStep, GetStatsStep} = require('./steps');
const {PeerConnectionCheck, RemoteVideoDisplayCheck} = require('./checks');
// Pages
const {AppRTCJoinPage, AppRTCMeetingPage} = require('./pages');

class IceConnectionTest extends KiteBaseTest {
  constructor(name, kiteConfig) {
    super(name, kiteConfig);
  }

  async testScript() {
    try {
      this.driver = await WebDriverFactory.getDriver(this.capabilities, this.remoteUrl);
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

      await this.waitAllSteps();
    } catch (error) {
      console.log(error);
    } finally {
      if (typeof this.driver !== 'undefined') {
        await this.driver.quit();
      }
    }
  }
}

module.exports = IceConnectionTest;

(async () => {
  const kiteConfig = await TestUtils.getKiteConfig(__dirname);
  let test = new IceConnectionTest('IceConnection Test', kiteConfig);
  await test.run();
})();
