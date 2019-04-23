const {TestStep} = require('kite-common');
const {apprtcJoinPage} = require('../pages');


// Todo: change it
// Random room id with Date
var today = new Date()
const roomId = (today.getDate()) * (today.getHours()+100) * (today.getMinutes()+100) 


/**
 * Class: ConnectToAppRoomStep
 * Extends: TestStep
 * Description:
 */
class ConnectToAppRoomStep extends TestStep {
  constructor(driver, timeout) {
    super();
    this.driver = driver;
    this.timeout = timeout;
  }

  stepDescription() {
    return 'Join a room';
  }

  async step() {
    await apprtcJoinPage.enterRoomId(this.driver, roomId);
    // Join the room and wait until the page is ready
    await apprtcJoinPage.joinRoom(this.driver, this.timeout);
  }
}

module.exports = ConnectToAppRoomStep;
