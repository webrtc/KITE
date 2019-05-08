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
  constructor(kiteBaseTest) {
    super();
    this.driver = kiteBaseTest.driver;
    this.timeout = kiteBaseTest.timeout;
    this.roomId = roomId;
  }

  stepDescription() {
    return 'Join a room';
  }

  async step() {
    await apprtcJoinPage.enterRoomId(this);
    // Join the room and wait until the page is ready
    await apprtcJoinPage.joinRoom(this);
  }
}

module.exports = ConnectToAppRoomStep;
