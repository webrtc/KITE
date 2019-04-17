// Kite common
const {TestUtils} = require('kite-common');
// StepReport
const StepReport = TestUtils.StepReport;
// Pages
const {apprtcMeetingPage} = require('../pages');


module.exports = {
  execute: async function(driver, testReport, screenshotsFolderPath, screenshotFileName) {

    let details = {};
    let result = 'passed';
    // Report
    let stepReport = new StepReport('Verify that the remote video is actually playing');

    if (testReport.status === 'passed') {
      try {
        console.log('executing: '+ stepReport.report.name);
  
        let videoCheck = await apprtcMeetingPage.verifyRemoteVideoDisplay(driver, screenshotsFolderPath, screenshotFileName);
        details['videoCheck'] = videoCheck;
        stepReport.addAttachment('value', details);
  
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