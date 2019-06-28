const {By, Key} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');

const elements = {
  searchBar: By.className('gLFyf'),
}

class GoogleSearchPage {
  constructor(driver) {
    this.driver = driver;
  }

  async open(stepInfo) {
    await TestUtils.open(stepInfo);
  }

  async searchFor(target) {
    let searchBar = await this.driver.findElement(elements.searchBar);
    await searchBar.sendKeys(target);
    await searchBar.sendKeys(Key.ENTER);
    await TestUtils.waitAround(1000);
  }
}

module.exports = GoogleSearchPage;