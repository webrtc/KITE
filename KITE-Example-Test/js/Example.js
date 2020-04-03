const {TestUtils, WebDriverFactory, KiteBaseTest} = require('kite-common');
const {GoogleSearchStep} = require('./steps'); 
const {GoogleFirstResultCheck} = require('./checks');
const {GoogleResultPage, GoogleSearchPage} = require('./pages');

class Example extends KiteBaseTest{
  constructor(name, kiteConfig) {
    super(name, kiteConfig);
  }

  async testScript() {
    try {
      this.driver = await WebDriverFactory.getDriver(this.capabilities);
      this.page = new GoogleSearchPage(this.driver);

      let googleSearchStep = new GoogleSearchStep(this, "https://google.com");
      await googleSearchStep.execute(this);
      
      this.page = new GoogleResultPage(this.driver);
      let googleFirstResultCheck = new GoogleFirstResultCheck(this);
      await googleFirstResultCheck.execute(this);

    } catch (e) {
      console.log(e);
    } finally {
      if (typeof this.driver !== 'undefined') {
        await this.driver.quit();
      }
    }
  }
}

module.exports = Example;

(async () => {
  const kiteConfig = await TestUtils.getKiteConfig(__dirname);
  let test = new Example('Example test', kiteConfig); 
  await test.run();
})();
