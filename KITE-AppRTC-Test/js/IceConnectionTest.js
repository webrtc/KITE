// const KiteBaseTest = require('./classes/KiteBaseTest');
const {TestUtils, WebDriverFactory, KiteBaseTest, Status} = require('kite-common');
const globalVariables = TestUtils.getGlobalVariables(process);

// Steps & checks
const OpenAppUrlStep = require('./steps/OpenAppUrlStep');
const ConnectToAppRoomStep = require('./steps/ConnectToAppRoomStep');
const PeerConnectionCheck = require('./checks/PeerConnectionCheck');
const RemoteVideoDisplayCheck = require('./checks/RemoteVideoDisplayCheck');
const GetStatsStep = require('./steps/GetStatsStep');

const capabilities = require(globalVariables.capabilitiesPath);
const payload = require(globalVariables.payloadPath);
const reportPath  = globalVariables.reportPath;

class IceConnectionTest extends KiteBaseTest {
  constructor(name, payload, reportPath) {
    super(name, payload, reportPath);
  }

  async testScript() {
    try {
      var driver = await WebDriverFactory.getDriver(capabilities, capabilities.remoteAddress);
      let openAppUrlStep = new OpenAppUrlStep(driver, this.url, this.timeout);
      await openAppUrlStep.execute(this.report, this.reporter);
      let connectToAppRoomStep = new ConnectToAppRoomStep(driver, this.timeout);
      await connectToAppRoomStep.execute(this.report, this.reporter);
      let peerConnectionCheck = new PeerConnectionCheck(driver, this.timeout);
      await peerConnectionCheck.execute(this.report, this.reporter);
      let remoteVideoDisplayCheck = new RemoteVideoDisplayCheck(driver);
      await remoteVideoDisplayCheck.execute(this.report, this.reporter);
      let getStatsStep = new GetStatsStep(driver,this.statsCollectionDuration, this.statsCollectionInterval);
      await getStatsStep.execute(this.report, this.reporter);

      // End of Test report
      this.report.setStopTimestamp();
    } catch (error) {
      console.log(error);
    } finally {
      driver.quit();
    }
    this.reporter.generateReportFiles();
    let value = this.report.getJsonBuilder();
    TestUtils.writeToFile(this.reportPath + '/result.json', JSON.stringify(value));
  }
}

module.exports = IceConnectionTest;

var test = new IceConnectionTest('IceConnection Test', payload, reportPath);
test.testScript();
