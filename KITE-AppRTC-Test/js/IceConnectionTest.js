const {TestUtils, WebDriverFactory, KiteBaseTest, Status} = require('kite-common');
const globalVariables = TestUtils.getGlobalVariables(process);

// Steps & checks
const {OpenAppUrlStep, ConnectToAppRoomStep, GetStatsStep} = require('./steps');
const {PeerConnectionCheck, RemoteVideoDisplayCheck} = require('./checks');

// KiteBaseTest config
const capabilities = require(globalVariables.capabilitiesPath);
const payload = require(globalVariables.payloadPath);



class IceConnectionTest extends KiteBaseTest {
  constructor(name, globalVariables, capabilities, payload) {
    super(name, globalVariables, capabilities, payload);
  }

  async testScript() {
    try {
      this.driver = await WebDriverFactory.getDriver(this.capabilities, this.capabilities.remoteAddress);
      let openAppUrlStep = new OpenAppUrlStep(this);
      await openAppUrlStep.execute(this);
      let connectToAppRoomStep = new ConnectToAppRoomStep(this);
      await connectToAppRoomStep.execute(this);
      let peerConnectionCheck = new PeerConnectionCheck(this);
      await peerConnectionCheck.execute(this);
      let remoteVideoDisplayCheck = new RemoteVideoDisplayCheck(this);
      await remoteVideoDisplayCheck.execute(this);
      let getStatsStep = new GetStatsStep(this);
      await getStatsStep.execute(this);

      // End of Test report
      this.report.setStopTimestamp();
    } catch (error) {
      console.log(error);
    } finally {
      await this.driver.quit();
    }

    this.reporter.generateReportFiles();
    let value = this.report.getJsonBuilder();
    TestUtils.writeToFile(this.reportPath + '/result.json', JSON.stringify(value));
  }
}

module.exports = IceConnectionTest;

var test = new IceConnectionTest('IceConnection Test', globalVariables, capabilities, payload);
test.testScript();
