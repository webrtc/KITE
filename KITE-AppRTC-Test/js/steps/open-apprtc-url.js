// Kite common
const {TestUtils} = require('kite-common');
// StepReport
const StepReport = TestUtils.StepReport;
// Pages
const {apprtcJoinPage} = require('../pages');


module.exports = {
  execute: async function(driver, testReport, url, timeout) {

    let result = 'passed';

    // Report
    let stepReport = new StepReport('Open the apprtc url');

    if (testReport.status === 'passed') {
      try{
        console.log('executing: ' + stepReport.report.name);
        // Open the url and wait until the page is ready
        await apprtcJoinPage.open(driver, url, timeout);

      } catch (error) {
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