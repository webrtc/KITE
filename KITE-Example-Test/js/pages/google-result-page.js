const {By, until} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');

const elements = {
  result: By.className('LC20lb'),
}

module.exports = {

  openFirstResult: async function(driver) {
    let result = await driver.wait(until.elementLocated(elements.result));
    result.click();
  },

  getTitle: async function(driver) {
    return await driver.getTitle();
  }
}