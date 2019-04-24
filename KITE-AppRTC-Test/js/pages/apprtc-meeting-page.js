const {By} = require('selenium-webdriver');
const {TestUtils} = require('kite-common');


const remoteVideo = 'remoteVideo';

// Elements of the page
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

  // Click on the mute audio button
  muteAudio: async function(driver) {
    let muteAudioButton = await driver.findElement(elements.fullscreenButton.muteAudioButton);
    muteAudioButton.click();
  },

  // Click on the mute video button
  muteVideo: async function(driver) {
    let muteVideoButton = await driver.findElement(elements.muteVideoButton);
    muteVideoButton.click();
  },

  // Click on the hangup button
  hangup: async function(driver) {
    let hangUpButton = await driver.findElement(elements.hangUpButton);
    hangUpButton.click();
  },

  // Click on the 'full screen' button
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

  verifyRemoteVideoDisplay: async function(driver) {
    let result = await TestUtils.verifyVideoDisplayById(driver, remoteVideo);
    return result;
  }

}