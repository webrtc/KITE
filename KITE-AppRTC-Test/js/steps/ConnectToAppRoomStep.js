const {TestStep} = require('kite-common');

/**
 * Class: ConnectToAppRoomStep
 * Extends: TestStep
 * Description:
 */
class ConnectToAppRoomStep extends TestStep {
  constructor(kiteBaseTest) {
    super();
    this.driver = kiteBaseTest.driver;
    this.timeout = kiteBaseTest.timeout;
    this.uuid =  kiteBaseTest.uuid;
    this.page = kiteBaseTest.page;
  }

  stepDescription() {
    return 'Join a room';
  }

  async step() {
    await this.page.enterRoomId('apprtc' + this.uuid);
    // Join the room and wait until the page is ready
    await this.page.joinRoom(this);
  }
}

module.exports = ConnectToAppRoomStep;
