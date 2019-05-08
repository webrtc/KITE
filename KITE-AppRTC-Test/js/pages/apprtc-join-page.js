const {By} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');

const elements = {
  roomInput: By.id('room-id-input'),
  joinButton: By.id('join-button')
};

module.exports = {

  // Kite common ?
  /*
  open: async function(driver, url, timeout) {
    await driver.get(url);
    await TestUtils.waitForPage(driver, timeout);
  },*/

  enterRoomId: async function(stepInfo) {
    let roomInput = await stepInfo.driver.findElement(elements.roomInput);
    await roomInput.clear();
    await roomInput.sendKeys(stepInfo.roomId);
  },

  joinRoom: async function(stepInfo) {
    let joinButton = await stepInfo.driver.findElement(elements.joinButton);
    joinButton.click();
    await TestUtils.waitForPage(stepInfo.driver, stepInfo.timeout);
  },
}