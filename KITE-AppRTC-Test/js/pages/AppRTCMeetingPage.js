const {By} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');

// Elements of the page
const elements = {
  muteAudioButton: By.id('mute-audio'),
  muteVideoButton: By.id('mute-video'),
  hangUpButton: By.id('hangup'),
  fullscreenButton: By.id('fullscreen')
}
const videos = By.className('video');

class AppRTCMeetingPage {
  constructor(driver) {
    this.driver = driver;
  }

  // Click on the mute audio button
  async muteAudio() {
    let muteAudioButton = await this.driver.findElement(elements.fullscreenButton.muteAudioButton);
    muteAudioButton.click();
  }

  // Click on the mute video button
  async muteVideo() {
    let muteVideoButton = await this.driver.findElement(elements.muteVideoButton);
    muteVideoButton.click();
  }

  // Click on the hangup button
  async hangup() {
    let hangUpButton = await this.driver.findElement(elements.hangUpButton);
    hangUpButton.click();
  }

  // Click on the 'full screen' button
  async goFullScreen() {
    let fullscreenButton = await this.driver.findElement(elements.fullscreenButton);
    fullscreenButton.click();
  }

  async getIceConnectionState() {
    let state = await this.driver.executeScript("var retValue;"
    + "try {retValue = appController.call_.pcClient_.pc_.iceConnectionState;} catch (exception) {} "
    + "if (retValue) {return retValue;} else {return 'unknown';}"); 
    return state;
  }

  async videoCheck(stepInfo, index) {
    let checked; // Result of the verification
    let i
    let timeout = stepInfo.timeout / 1000;

    // Waiting for all the videos
    await TestUtils.waitVideos(stepInfo, videos);

    // Check the status of the video
    // checked.result = 'blank' || 'still' || 'video'
    checked = await verifyVideoDisplayByIndex(stepInfo.driver, index);
    i = 0;
    while(checked.result === 'blank' && i < timeout) {
      checked = await verifyVideoDisplayByIndex(stepInfo.driver, index);
      i++;
      await waitAround(1000);
    }

    i = 0;
    while(i < 3 && checked.result != 'video') {
      checked = await verifyVideoDisplayByIndex(stepInfo.driver, index);
      i++;
      await waitAround(3 * 1000); // waiting 3s after each iteration
    }
    return checked.result;
  }

}

module.exports = AppRTCMeetingPage;