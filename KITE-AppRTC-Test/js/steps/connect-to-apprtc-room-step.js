// Kite common
const {TestUtils} = require('kite-common');
// StepReport
const StepReport = TestUtils.StepReport;
// Pages
const {apprtcJoinPage} = require('../pages');



// Todo: change it
// Rndom room id with Date
var today = new Date()
const roomId = (today.getDate()) * (today.getHours()+100) * (today.getMinutes()+100) 

// Todo: add some logs to understand
module.exports = {
  execute: async function(driver, testReport, timeout) {
    let result = 'passed';

    // Report
    let stepReport = new StepReport('Join a room');
    
    if (testReport.status === 'passed') {
      try {
        console.log('executing: ' + stepReport.report.name);
        // Room input
        await apprtcJoinPage.enterRoomId(driver, roomId);
        // Join the room and wait until the page is ready
        await apprtcJoinPage.joinRoom(driver, timeout);  
  
      } catch(error) {
        console.log(error);
        result = 'failed';
      }
    } else {
      result = 'skipped';
      console.log('skipping: ' + stepReport.report.name);
    }
    stepReport.end(result);
    testReport.addStep(stepReport.report, result);
  }
}