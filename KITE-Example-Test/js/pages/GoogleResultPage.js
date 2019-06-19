const {By, until} = require('selenium-webdriver');

const elements = {
  result: By.className('LC20lb'),
}

class GoogleResultPage {
  constructor(driver) {
    this.driver = driver;
  }

  async openFirstResult() {
    let result = await this.driver.wait(until.elementLocated(elements.result));
    await result.click();
  }

  async getTitle() {
    return await this.driver.getTitle();
  }

}
module.exports = GoogleResultPage