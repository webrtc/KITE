const {By, Key} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');



const elements = {
  searchBar: By.className('gLFyf'),
}


module.exports = {
  searchFor: async function(stepInfo) {
    let searchBar = await stepInfo.driver.findElement(elements.searchBar);
    await searchBar.sendKeys(stepInfo.target);
    await searchBar.sendKeys(Key.ENTER);
    await TestUtils.waitAround(1000);
  }
}