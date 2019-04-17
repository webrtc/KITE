const {By} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');


const remoteVideo = 'remoteVideo';

const elements = {
  miniVideo: By.id('mini-video'),
  localVideo: By.id('local_video'),
  remoteVideo: By.id('remote-video'),
  muteAudioButton: By.id('mute-audio'),
  muteVideoButton: By.id('mute-video'),
  hangUpButton: By.id('hangup'),
  fullscreenButton: By.id('fullscreen')
}


module.exports = {

  muteAudio: async function(driver) {
    let muteAudioButton = await driver.findElement(elements.fullscreenButton.muteAudioButton);
    muteAudioButton.click();
  },

  muteVideo: async function(driver) {
    let muteVideoButton = await driver.findElement(elements.muteVideoButton);
    muteVideoButton.click();
  },

  hangup: async function(driver) {
    let hangUpButton = await driver.findElement(elements.hangUpButton);
    hangUpButton.click();
  },

  goFullScreen: async function(driver) {
    let fullscreenButton = await driver.findElement(elements.fullscreenButton);
    fullscreenButton.click();
  },

  getIceConnectionState: async function(driver) {
    let state = await driver.executeScript("var retValue;"
    + "try {retValue = appController.call_.pcClient_.pc_.iceConnectionState;} catch (exception) {} "
    + "if (retValue) {return retValue;} else {return 'unknown';}"); 
    return state;
  },

  verifyRemoteVideoDisplay: async function(driver, screenshotsFolderPath, screenshotFileName) {
    let result = await TestUtils.verifyVideoDisplayById(driver, remoteVideo);
    await TestUtils.takeScreenshot(driver, screenshotsFolderPath, screenshotFileName);
    return result;
  }

}