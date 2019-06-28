const {TestUtils, WebDriverFactory, KiteBaseTest} = require('kite-common');
const {GoogleSearchStep} = require('./steps'); 
const {GoogleFirstResultCheck} = require('./checks');
const {GoogleResultPage, GoogleSearchPage} = require('./pages');

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
      this.page = new GoogleSearchPage(this.driver);

      let googleSearchStep = new GoogleSearchStep(this, "https://google.com");
      await googleSearchStep.execute(this);
      
      this.page = new GoogleResultPage(this.driver);
      let googleFirstResultCheck = new GoogleFirstResultCheck(this);
      await googleFirstResultCheck.execute(this);

    } catch (e) {
      console.log(e);
    } finally {
      this.driver.quit();
    }
  }
}

module.exports = Example;

let test = new Example('Example test', globalVariables, capabilities, payload); 
test.run();
