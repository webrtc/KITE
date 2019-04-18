// Kite common
const {TestUtils} = require('kite-common');
// StepReport
const StepReport = TestUtils.StepReport;

const pc = 'appController.call_.pcClient_.pc_';

module.exports = {
  execute: async function(driver, testReport, statsCollectionDuration, statsCollectionInterval) {
    
    let result = 'passed';
    let stats = {};

    // Report
    let stepReport = new StepReport("Get the peer connection's stats");

    if (testReport.status === 'passed') {
      try {
        console.log('executing: ' + stepReport.report.name);
        // getStats
        let getStats = await TestUtils.getStats(driver, pc, statsCollectionDuration, statsCollectionInterval);
        stats['Peer connection\'s stats'] = getStats;
  
        console.log("Get stats step done !");
        
      } catch (error) {
        console.log(error);
        result = 'failed';
      }
    } else {
      result = 'skipped';
      console.log('skipping: ' + stepReport.report.name);
    }
    stepReport.addAttachment('value', stats);
    stepReport.end(result);
    testReport.addStep(stepReport.report, result);
  }
}
