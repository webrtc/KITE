const {By} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');

const elements = {
  roomInput: By.id('room-id-input'),
  joinButton: By.id('join-button')
};

class AppRTCJoinPage {
  constructor(driver) {
    this.driver = driver;
  }
  
  async open(stepInfo) {
    await TestUtils.open(stepInfo);
  }

  async enterRoomId(roomId) {
    let roomInput = await this.driver.findElement(elements.roomInput);
    await roomInput.clear();
    await roomInput.sendKeys(roomId);
  }

  async joinRoom() {
    let joinButton = await this.driver.findElement(elements.joinButton);
    await joinButton.click();
  }
}

module.exports = AppRTCJoinPage;