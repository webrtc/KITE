const {TestUtils, WebDriverFactory, KiteBaseTest} = require('kite-common');
const {GoogleSearchStep} = require('./steps'); 
const {GoogleFirstResultCheck} = require('./checks');

const globalVariables = TestUtils.getGlobalVariables(process);

// KiteBaseTest config
const capabilities = require(globalVariables.capabilitiesPath);
const payload = require(globalVariables.payloadPath);



class Example extends KiteBaseTest{
  constructor(name, globalVariables, capabilities, payload) {
    super(name, globalVariables, capabilities, payload);
  }

  async testScript() {
    try {
      this.driver = await WebDriverFactory.getDriver(this.capabilities, this.capabilities.remoteAddress);
      
      let googleSearchStep = new GoogleSearchStep(this,  "https://google.com");
      await googleSearchStep.execute(this);
      
      let googleFirstResultCheck = new GoogleFirstResultCheck(this);
      await googleFirstResultCheck.execute(this);

      this.report.setStopTimestamp();
    } catch (e) {
      console.log(e);
    } finally {
      this.driver.quit();
    }

    this.reporter.generateReportFiles();
    let value = this.report.getJsonBuilder();
    TestUtils.writeToFile(this.reportPath + "/result.json", JSON.stringify(value));
  }
}

module.exports = Example;

let test = new Example('Example test', globalVariables, capabilities, payload); 
test.testScript();
