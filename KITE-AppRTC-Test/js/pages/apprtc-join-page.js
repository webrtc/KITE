const {By} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');

const elements = {
  roomInput: By.id('room-id-input'),
  joinButton: By.id('join-button')
};

module.exports = {

  open: async function(driver, url, timeout) {
    await driver.get(url);
    await TestUtils.waitForPage(driver, timeout);
  },

  enterRoomId: async function(driver, roomId) {
    let roomInput = await driver.findElement(elements.roomInput);
    await roomInput.clear();
    await roomInput.sendKeys(roomId);
  },

  joinRoom: async function(driver, timeout) {
    let joinButton = await driver.findElement(elements.joinButton);
    joinButton.click();
    await TestUtils.waitForPage(driver, timeout);
  },
}