// Kite common
const {TestUtils} = require('kite-common');
const waitAround = TestUtils.waitAround;
// StepReport
const StepReport = TestUtils.StepReport;
// Pages
const {apprtcMeetingPage} = require('../pages');


module.exports = {
  execute: async function(driver, testReport, timeout) {
    let state;
    let time = 0;
    let notFinished = true;
    let result = 'passed';

    // Report
    let stepReport = new StepReport("Verify that the ICE connection state is 'connected'");
    
    if (testReport.status === 'passed') {
      try {
        console.log('executing: '+ stepReport.report.name);
        
        // Loop to wait for the connection - timeMax delay
        while(time < timeout && notFinished) {
  
          state = await apprtcMeetingPage.getIceConnectionState(driver);
  
          if (state === "failed") {
            console.log("The ICE connection failed");
            notFinished = false;
          }
          if (state === "connected" || state === "completed") {
            console.log('Success ! ' + 'State: ' + state);
            notFinished = false;
          }
  
          // Every 1 sec
          await waitAround(1000);
          time++;
        }
  
        if (time == timeout) {
          result = 'failed';
          console.log("Time out !");
        }
        else {
          console.log("Peer connection ckecked !");
        }
  
      } catch(error) {
        console.log(error);
        result = 'failed';
      }
    } else {
      result = 'skipped';
      console.log('skipping: ' + stepReport.report.name);
    }
    // End of the report
    stepReport.end(result);
    testReport.addStep(stepReport.report, result);
  }
}