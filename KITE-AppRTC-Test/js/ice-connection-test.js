// Kite common
const {TestUtils, WebDriverFactory} = require('kite-common')
const waitAround = TestUtils.waitAround;
const globalVariables = TestUtils.getGlobalVariables(process)
// TestReport
const TestReport = TestUtils.TestReport;
// Steps & Checks
const {openApprtcUrl,connectToApprtcRoomStep, getStatsStep} = require('./steps');
const {peerConnectionCheck, remoteVideoDisplayCheck} = require('./checks');

const numberOfParticipant = globalVariables.numberOfParticipant;
const id = globalVariables.id;
const capabilities = require(globalVariables.capabilitiesPath);
const payload = require(globalVariables.payloadPath);
const resultFilePath = globalVariables.resultFilePath;
const screenshotFolderPath = globalVariables.screenshotFolderPath;


const url = payload.url;
const timeout = payload.testTimeout * 1000;
const statsCollectionDuration = payload.statsCollectionDuration * 1000;
const statsCollectionInterval = payload.statsCollectionInterval * 1000;

// To take a screenshot during remoteVideoDisplayCheck
const screenshotFileName = 'Screenshot_'+ 'remoteVideoDisplayCheck' + '.png';

(async function testScript() {
  let driver;
  let status = 'passed';

  // Test report
  let testReport = new TestReport('ICE Connection test', status);
  
  // Start of tests
  try {
    driver = await WebDriverFactory.getDriver(capabilities, capabilities.remoteAddress)
    
    // Openning the url
    await openApprtcUrl.execute(driver, testReport, url, timeout);

    // Joinning room
    // Step
    await connectToApprtcRoomStep.execute(driver, testReport, timeout);

    // Meeting room 
    // Checks
    // Check the peer connection
    await peerConnectionCheck.execute(driver, testReport, timeout);
    
    // Check the remote video display
    await remoteVideoDisplayCheck.execute(driver, testReport, screenshotFolderPath, screenshotFileName);
    
    // Step
    await getStatsStep.execute(driver, testReport, statsCollectionDuration, statsCollectionInterval);

    await waitAround(1000);

  } catch(error) {
    console.log(error);

  } finally {
    await driver.quit();
  }

  // End of test report
  testReport.end(resultFilePath);
})();