# Tutorial: Writing a simple KITE Test for Jitsi in Javascript
*****
### KITE Test Design Pattern

KITE Tests are implemented in a framework that follows the Page Object Model design pattern [(POM)](https://medium.com/tech-tajawal/page-object-model-pom-design-pattern-f9588630800b), and organises a Test in __Pages__,  __Steps__ and __Checks__.
Using __Page Object Model__, all element locators are being managed in separate directory and can be updated easily without any modification to the test scenarios. The best practice to this model is that no WebDriver API should be call in the test class, it should be manage in __Pages__, especially for exception/error handling.

A Test consists of a succession of __Steps__ and __Checks__:
- __Steps__ are actions, for example, navigate to a page or click a button. __Steps__ are not expected to fail, they either 'Pass' or 'Break' (any unexpected error).
- __Checks__ are assertations, for example, validate that a video is playing. __Checks__ can 'Pass' (video is playing), 'Fail' (video is blank/still) or 'Break' (e.g. page timeout).

__Pages__ are where we place the element locators and the functions to interact with them.

### Writing a test

This tutorial will guide you step-by-step in writing your first KITE Test. The sample test will open the https://meet.jit.si/ page on a web browser (Chrome and Firefox), check the sent and received videos, collect the WebRTC statistics and take a screenshot.

1. Create the Test
2. Adding the first Step: open https://meet.jit.si/ page
3. Adding the MainPage (Jitsi page)
4. Adding the __Checks__ :
    * Sent Video Check
    * Received Videos Check
5. Adding the more __Steps__: 
    * Collect WebRTC Stats
    * Taking a Screenshot


You can find the full source code of this tutorial [here](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test).

*****
### 1. Create the Test

To create the test, execute the following commands:

__On Windows:__
```
cd %KITE_HOME%
kite_init jitsiTutorial
c
```
__On Linux/Mac:__
```
cd $KITE_HOME
kite_init jitsiTutorial
c
```

This will create KITE-JitsiTutorial-Test folder containing all the basic files and sub-folders used in all KITE Tests, for both Java and Javascript.

This tutorial is about the Javascript KITE Test and all the javascript files are located in `KITE-JitsiTutorial-Test/js`.   
It will also compile the project, which will setup npm, nodejs and trigger `npm install` for the `js/` folder.
Let's have a closer look at the files and folders generated in  `KITE-JitsiTutorial-Test/js`.

#### Main js file
The file `js/JitsiTutorial.js` is the entry point of the test. It will be run by KITE by calling `node JitsiTutorial.js <params of the tests>`. 
At the top of this file, we will find three classes imported from kite-common:
```
const {TestUtils, WebDriverFactory, KiteBaseTest} = require('kite-common');
```
__TestUtils__ contains a set of common functions that are used by all KITE Tests.
__WebDriverFactory__ allows the creation of the WebDriver.
__KiteBaseTest__ this is the parent class of the test, it will enable the generation of the Allure Report.

The following codes gets the *capabilities* required to create the webdrivers and the *payload* which is the test specific configuration passed to the test by the KITE Engine. We can pass any kind of information to the test via this *payload* object in the config file.
```
const globalVariables = TestUtils.getGlobalVariables(process);
const capabilities = require(globalVariables.capabilitiesPath);
const payload = require(globalVariables.payloadPath);
```

#### Folders

The `kite_init` script generated 3 subfolders:
```
- pages/
- steps/
- checks/
```
This is where we will respectively implement the __Pages__, __Steps__ and __Checks__ of the test, following the __Page Object Model__ design pattern.

The `node_modules` should have already been created during the compilation process by `npm install`. 


#### Config file.

The KITE config file of our javascript test can be found at  `KITE-JitsiTutorial-Test/configs/js.jitsiTutorial.config.json`
This file contains all the information for the tests.

__name__: name of the configuration file.
__remotes__: the address of the selenium hub.
__tests__: a array containing all the tests to be run. Each test contains all the necessary information for its progress.
In each test we can find:
- __name__: name of the test
- __description__: description of the test
- __tupleSize__: number of browsers participating/launched at the same time during the test
- __testImpl__: name of the JS test file
- __payload__ contains _name:values_ pairs to be used in the test.

__browsers__: List of all the browsers that will be tested. This list will be used to create tuples (with provided tupleSize). Each tuple will be used to run in one test case.

#### Running the test
Once done, we can already run the test with:
```
r configs\js.jitsiTutorial.config.json
```
At this stage, the test only launches a webbrowser, opens https://google.com and does a random check.

#### Test report
KITE test is not based on any unit test framework, so Allure has no support for it. However, KITE test has its own report system, inspired by Allure reporting, and can be served by Allure.Open the Allure Report with:
```
a
```

The report should look like this:
    ![First Allure Report](./img/Screenshot1AllureReport.png)

*****
### 2. Adding a Step
#### Step: Open a the web page

The file `/steps/OpenUrlStep.js` was generated by the `kite_init` script. It already contains everything required for this Step.  

__TestStep__ is the parent class that enables the Allure Report generation.

The constructor of each Step must contain the WebDriver object. It is needed for the Step to control the web browser and execute what it needs to do.
Next, there is the attribute `this.page` which contains an instance of the __MainPage__.
The constructor can include other variables and objects, specifically needed for the Step.

⚠ To create a Step or a Check, we must implement two functions, __stepDescription()__ and __step()__, which are abstract functions of the parent class __TestStep__.

   * __stepDescription()__ returns a string which will be displayed in the report.
    
   * __step()__ is asynchronous because it executes WebDriver functions which are asynchronous. Therefore, we use `async/await` to make it synchronous.

Example:
```
    async step() {
        await this.page.open(this);
    }
```
__step()__ will call __open()__ in `/pages/MainPage.js` which use __TestUtils.open(url)__ to open the browser to the url.

But, you have to change the url in `/configs/js.jitsiTutorial.config.json` in the payload:
`"url": "https://meet.jit.si/"`

Now we're going to add the code that allows joining a meeting.

*****
### 3. Adding a Page
#### Page: Main page

Similarly to the __Steps__ which are located in the `steps/` folder, the __Pages__ are located in the `pages/` folder.
The `kite_init` script created two files:  `pages/MainPage.js` and `pages/index.js`.
We can now edit `pages/MainPage.js` and add the HTML locators and functions that allows interactions with the web page.
 
The HTML locators will be represented the class `By` from [Selenium-webdriver](https://seleniumhq.github.io/selenium/docs/api/javascript/module/selenium-webdriver/).
Here, we'll use the text input to choose our meeting room;

`const meetingRoom = By.id('enter_room_field')` where __enter_room_field__ is the id of the HTML object.

In __MainPage__ class, add:

    async enterRoom(roomId) {
        let meeting = await this.driver.findElement(meetingRoom);
        await meeting.sendKeys(roomId); // Fill out the field and add some random numbers
        await meeting.sendKeys(Key.ENTER); // Press ENTER to enter in the room
    }

The variable __stepInfo__ is a reference to the Step object (= this).
To interact with a HTML element, for example the button or the text input, we're using the selenium-webdriver API.
The full API doc can be found here: [Selenium-webdriver](https://seleniumhq.github.io/selenium/docs/api/javascript/module/selenium-webdriver/)
`Key.XXX` where XXX is an enumeration of keys like "ENTER" that can be used: [Key](https://seleniumhq.github.io/selenium/docs/api/javascript/module/selenium-webdriver/index_exports_Key.html).

Now, we are going to modify `/steps/OpenUrlStep.js` to join our metting room.

In __step()__, add:
`await this.page.enterRoom("I am a random room" + this.uuid);

To obtain:

    async step() {
        await TestUtils.open(this);
        await this.page.enterRoom("I am a random room" + this.uuid);
    }    

See [OpenUrlStep.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/steps/OpenUrlStep.js).


And the report should now look like:  
    ![First Step Allure Report](./img/Screenshot2AllureReport.png)
****
### 4. Adding the __Checks__
#### Check: Sent Video

Now, we're going to add a __Check__. A Check is a kind of Step that asserts a condition and can 'Fail'. All __Checks__ are places in the `checks/` folder.

We're goind to create a file named `checks/SentVideoCheck.js`.
Once the file has been created, we'll add a reference to `checks/index.js` so it's available to any code that requires the folder `checks/`:

    ____ checks/index.js ____
        ...
        exports.SentVideoCheck = require('./SentVideoCheck');
        ...  
The objective of this Check is to validate that the video is being sent.  We'll need some elements in the `pages/MainPage.js` to access the _\<video\>_ elements.

Open the file `pages/MainPage.js`.

To add the _\<video\>_ elements, we're simply add:
`const videos = By.css('video');` => This allows us to get all the HTML elements with tag _\<video\>_ into our `videos` array.
Next, we're going to add a synchronous function called __videoCheck()__ where we'll implement the video check logic.
```
    async videoCheck(stepInfo, index) {
        ...
    },
```
__stepInfo__ is a reference to the Check object.
__index__ is the index of the video to be checked (0 for the first video, 1 for the 2nd, etc...)

First, we'll declare our variables:
```
    async videoCheck(stepInfo, index) {
        let checked; // Result of the verification
        let i; // iteration indicator
        let timeout = stepInfo.timeout;
        stepInfo.numberOfParticipant = parseInt(stepInfo.numberOfParticipant) + 1; // To add the first video      
    }
```    
Then we'll wait for all the videos. So, we're going to use `TestUtils.waitVideos()` from kite-common.
```  
    async videoCheck(stepInfo, index) {
        let checked; // Result of the verification
        let i; // iteration indicator
        let timeout = stepInfo.timeout;
        stepInfo.numberOfParticipant = parseInt(stepInfo.numberOfParticipant) + 1; // To add the first video
        // Waiting for all the videos
        await TestUtils.waitVideos(stepInfo, videos);
        stepInfo.numberOfParticipant --; // To delete the first video
    }
```
We add `parseInt(stepInfo.numerOfParticipant)` __`+ 1`__ because there is an other video that does not need to be checked. But, we remove it right after.

Next we'll check that the video is actually playing, meaning that it isn't blank (all the pixels of the video frame are black/white) or still, which means that the same image is still displayed after a second interval. For that we'll use the utility function `TestUtils.verifyVideoDisplayByIndex()` from kite-common:
```
    async videoCheck(stepInfo, index) {
        let checked; // Result of the verification
        let i; // iteration indicator
        let timeout = stepInfo.timeout;
        stepInfo.numberOfParticipant = parseInt(stepInfo.numberOfParticipant) + 1; // To add the first video
        
        // Waiting for all the videos
        await TestUtils.waitVideos(stepInfo, videos);
        stepInfo.numberOfParticipant --; // To delete the first video

        // Check the status of the video
        // checked.result = 'blank' || 'still' || 'video'
        i = 0;
        checked = await TestUtils.verifyVideoDisplayByIndex(stepInfo.driver, index + 1);
            while(checked.result === 'blank' || checked.result === undefined && i < timeout) {
            checked = await TestUtils.verifyVideoDisplayByIndex(stepInfo.driver, index + 1);
            i++;
            await waitAround(1000);
        }

        i = 0;
        while(i < 3 && checked.result != 'video') {
            checked = await TestUtils.verifyVideoDisplayByIndex(stepInfo.driver, index + 1);
            i++;
            await waitAround(3 * 1000); // waiting 3s after each iteration
        }
        return checked.result;
    }
```

We need to add `index` __`+ 1`__ to skip the large video in our .
    
To make the check robust to poor connections, we decided to repeat it 3 times at 3 seconds interval. We could make the checks much stricter by doing it only once, which would cause it to fail more easily in case of low framerate.

Now that we completed the implementation in `pages/MainPage.js`, we're going to edit `checks/SentVideoCheck.js`.  

As previously mentioned, a Check is a kind of Step and inherits from the the framework's `TestStep` class.
At the top of the file, we add the following require:
`const {TestStep, KiteTestError, Status} = require('kite-common');`

Then we implement the SentVideoCheck Class and its constructor:
```
class SentVideoCheck extends TestStep {
    constructor(kiteBaseTest) {
        super();
        this.driver = kiteBaseTest.driver;
        this.timeout = kiteBaseTest.timeout;
        this.numberOfParticipant = kiteBaseTest.numberOfParticipant;
        this.page = kiteBaseTest.page;

        // Test reporter if you want to add attachment(s)
        this.testReporter = kiteBaseTest.reporter;
    }
}
```
Update the __stepDescription()__:
```
    stepDescription() {
        return "Check the first video is being sent OK";
    }
```
Then the __step()__, where we call `this.page.videoCheck()` for the first video in the page (the sender's video).
We compare the result and if it's not 'video' we throw a KiteTestError with the Status.FAILED, that will be reported accordingly in the Allure Report.
```
    async step() {
        try {
            let result = await this.page.videoCheck(this, 0);
            if (result != 'video') {
                this.testReporter.textAttachment(this.report, "Sent video", result, "plain");
                throw new KiteTestError(Status.FAILED, "The video sent is " + result);
            }
        } catch (error) {
            console.log(error);
            if (error instanceof KiteTestError) {
                throw error;
            } else {
                throw new KiteTestError(Status.BROKEN, "Error looking for the video");
            }
        }
    }
```

To finish with this file, at the end, add:
`module.exports = SentVideoCheck;`
See [SentVideoCheck.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/checks/SentVideoCheck.js).

Lastly, we return to the main Test class file: `JitsiTutorial.js` and add our check there:
At the top of the file, we import the class
`const {SentVideoCheck} = require('./checks');`

And add the check to the __testScript()__ function:
```
    this.driver = await WebDriverFactory.getDriver(capabilities, capabilities.remoteAddress);
    this.page = new MainPage(this.driver);
    let openUrlStep = new OpenUrlStep(this);
    await openUrlStep.execute(this);
    // New check
    let sentVideoCheck = new SentVideoCheck(this);
    await sentVideoCheck.execute(this);
```   
Once here, you can remove `checks/MyFirstCheck.js`, remove it from `JitsiTutorial.js` and `checks/index.js`.
See [JitsiTutorial.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/JitsiTutorial.js).

Now, our test is able to check the sentVideo.

We can run the test again and see the report like mentioned above.
Our report should now look like this:
    ![First Check Allure Report](./img/Screenshot3AllureReport.png)
*****
#### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 2. Received Videos Check

Now, we are going to add an other Check. It will check that we received all the remote videos.
First, we have to change the value of `tupleSize` in `/configs/js.jitsiTutorial.config.json` and set it at 2 because we'll need 2 browsers.

We're goind to create a file named `checks/ReceivedVideoCheck.js`.
Once the file has been created, we will add a reference to `checks/index.js`.

____ checks/index.js ____
`exports.VideoSentCheck = require('./VideoSentCheck');`

The objective of this Check is to validate that all the remode videos are being received.
Fortunately, we already have everything to do it.

Open the file `checks/ReceivedVideoCheck.js`.

Like the last check, at the top of the file, we add the following require:
`const {TestStep, KiteTestError, Status} = require('kite-common');`

Then we implement the ReceivedVideoCheck Class and its constructor:
```
class ReceivedVideoCheck extends TestStep {
    constructor(kiteBaseTest) {
        super();
        this.driver = kiteBaseTest.driver;
        this.timeout = kiteBaseTest.timeout;
        this.numberOfParticipant = kiteBaseTest.numberOfParticipant;
        this.page = kiteBaseTest.page;

        // Test reporter if you want to add attachment(s)
        this.testReporter = kiteBaseTest.reporter;
    }
}
```
    
Update the __stepDescription()__:
```
    stepDescription() {
        return "Check the other videos are being received OK";
    }
```
Then the __step()__, where we call `this.page.videoCheck()` for each remote video.
We compare every result and if one is not 'video', then we throw a KiteTestError with the Status.FAILED, that will be reported accordingly in the Allure Report.
```
    async step() {
        let result = "";
        let tmp;
        let error = false;
        try {
            for(let i = 1; i < this.numberOfParticipant; i++) {
                tmp = await this.page.videoCheck(this, i);
                result += tmp;
                if (i < this.numberOfParticipant) {
                    result += ' | ';
                }
                if (tmp != 'video') {
                    error = true;
                }
            }
            if (error) {
                this.testReporter.textAttachment(this.report, "Received videos", result, "plain");
                throw new KiteTestError(Status.FAILED, "Some videos are still or blank: " + result);
            }
        } catch (error) {
            console.log(error);
            if (error instanceof KiteTestError) {
                throw error;
            } else {
                throw new KiteTestError(Status.BROKEN, "Error looking for the video");
            }
        }
    }
```
The for loop, starting at index 1, allows to check every received video. Indeed, the one of index 0 is the video sent.

At the end of the file, we shall add the following line to export this module and make it available to `JitsiTutorial.js`:
`module.exports = ReceivedVideoCheck;`
See [ReceivedVideoCheck.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/checks/ReceivedVideoCheck.js).

Finally, we add this check as the previous one in ```JitsiTutorial.js```.
We modify:
`const {SentVideoCheck} = require('./checks');`
by
`const {SentVideoCheck, ReceivedVideoCheck} = require('./checks');`

and add in __testScript()__:
```
    this.driver = await WebDriverFactory.getDriver(capabilities, capabilities.remoteAddress);
    this.page = new MainPage(this.driver);
    let openUrlStep = new OpenUrlStep(this);
    await openUrlStep.execute(this);
    let sentVideoCheck = new SentVideoCheck(this);
    await sentVideoCheck.execute(this);
    // New check
    let receivedVideoCheck = new ReceivedVideoCheck(this);
    await receivedVideoCheck.execute(this);
```
See [JitsiTutorial.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/JitsiTutorial.js).

Now, our test is able to every videos (sent and received). 

We can run the test again like mentioned above and the report will look like this:
    ![Second Step Allure Report](./img/Screenshot4AllureReport.png)
*****
### 5. Step: get stats

#CONFIG FILE
Before we start, we have to modify our config file, __configs/js.jitsiTutorial.config.json__.
Indeed, this step requires some information to work properly.
So, in `payload`, we add :
```
    "getStats" : {
        "enabled": true,
        "statsCollectionTime": 2,
        "statsCollectionInterval": 1,
        "peerConnections": ["window.pc[0]"],
        "selectedStats" : ["inbound-rtp", "outbound-rtp", "candidate-pair"]
    }
```
To obtain:
```
    "payload":{
        "url": "https://meet.jit.si/",
        "getStats" : {
            "enabled": true,
            "statsCollectionTime": 2,
            "statsCollectionInterval": 1,
            "peerConnections": ["window.pc[0]"],
            "selectedStats" : ["inbound-rtp", "outbound-rtp", "candidate-pair"]`
        }
    }
```

Now, we are going to create a step from scratch. Remember, all __Steps__ are places in the `steps/` folder.

We're going to create a file named `steps/GetStatsStep.js`.
Once the file has been created, we'll also add a reference to `steps/index.js`.

____ steps/index.js ____
`exports.GetStatsStep = require('./GetStatsStep');`

Open the file `checks/GetStatsStep.js`..

At the top of the file, we add the following require:
`const {TestUtils, TestStep, KiteTestError, Status} = require('kite-common');`

Then, in this file, we implement the GetStatsStep class and its constructor:

```
class GetStatsStep extends TestStep {
    constructor(kiteBaseTest) {
        super();
        this.driver = kiteBaseTest.driver;
        this.statsCollectionTime = kiteBaseTest.statsCollectionTime;
        this.statsCollectionInterval = kiteBaseTest.statsCollectionInterval;
        this.selectedStats = kiteBaseTest.selectedStats;
        this.peerConnections = kiteBaseTest.peerConnections;
        this.page = kiteBaseTest.page;

        // Test reporter if you want to add attachment(s)
        this.testReporter = kiteBaseTest.reporter;
    }
}
```
`this.statsCollectionTime`: duration of data collection
`this.statsCollectionTime`: interval of data collection
`this.selectedStats`: the data we want to collect
`this.peerConnections`: array of peer connections used to get stats;
   
Update the __stepDescription()__:
```
    stepDescription() {
        return 'Getting WebRTC stats via getStats';
    }
```
Then the __step()__:
```
    async step() {
        try {
            let rawStats = await this.page.getStats(this);
            let summaryStats = TestUtils.extractJson(rawStats, 'both');
            // // Data
            this.testReporter.textAttachment(this.report, 'GetStatsRaw', JSON.stringify(rawStats), "json");
            this.testReporter.textAttachment(this.report, 'GetStatsSummary', JSON.stringify(summaryStats), "json");
        } catch (error) {
            console.log(error);
            throw new KiteTestError(Status.BROKEN, "Failed to getStats");
        }
    }
```
Do not forget, at the end of this file, add:
`module.exports = GetStatsStep;`
See [GetStatsStep.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/steps/GetStatsStep.js).

The __this.page.getStats()__ allows to get stats. We are going to create it right after in MainPage();
The __TestUtils.extractJson()__ allows to make a summary of the data collected.

Now, we are going to create __getSats()__.
Open the file __/pages/MainPage.js__.

First, we need a script to get the peer connection.
Here is the script:
```
    window.peerConnections = [];
    map = APP.conference._room.rtc.peerConnections;
    for(var key of map.keys()){
        window.peerConnections.push(map.get(key).peerconnection);
    }
```
So, we create a function, outside our class, to get this script:
```
    const getPeerConnectionScript = function() {
        return "window.peerConnections = [];"
        + "map = APP.conference._room.rtc.peerConnections;"
        + "for(var key of map.keys()){"
        + "  window.peerConnections.push(map.get(key).peerconnection);"
        + "}";
    }
``` 
Then, in __MainPage__ class:
```
    async getStats(stepInfo) {
        await stepInfo.driver.executeScript(getPeerConnectionScript());
        let stats = await TestUtils.getStats(stepInfo, 'kite', stepInfo.peerConnections[0]);
        return stats;
    }
```
See [MainPage.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/pages/MainPage.js).

The __stepInfo.driver.executeScript(getPeerConnectionScript())__ will execute our script to get the peer connection more easily.
The __TestUtils.getStats()__ get stats from the peer connection.

Finally, we add this step in __JitsiTutorial.js__.
We modify:
`const {OpenUrlStep} = require('./steps');`
by
`const {OpenUrlStep, GetStatsStep} = require('./steps');`

and add in __testScript()__:
```
    this.driver = await WebDriverFactory.getDriver(capabilities, capabilities.remoteAddress);
    this.page = new MainPage(this.driver);
    let openUrlStep = new OpenUrlStep(this);
    await openUrlStep.execute(this);
    let sentVideoCheck = new SentVideoCheck(this);
    await sentVideoCheck.execute(this);
    let receivedVideoCheck = new ReceivedVideoCheck(this);
    await receivedVideoCheck.execute(this);
    // New step
    if (this.getStats) {
        let getStatsStep = new GetStatsStep(this);
        await getStatsStep.execute(this);
    }
```
See [JitsiTutorial.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/JitsiTutorial.js).

Like this, it's easier to enable or disable this step using the config file.

Now, we can get stats from the peer connection.

We can run the test again like mentioned above and the updated report should be now:
    ![GetStats Step Allure Report](./img/Screenshot5AllureReport.png) 
*****
### 6. Step: take a screenshot 

Now, we're going to create a new step (__Steps__ in `steps/` folder).

We're goind to create a file named `steps/ScreenshotStep.js`.
Once the file has been created, we'll again add a reference to `steps/index.js`.

____ steps/index.js ____
`exports.ScreenshotStep = require('./ScreenshotStep');`

Open the file `steps/ScreenshotStep.js`.

At the top of the file, we add the following require:
`const {TestUtils, TestStep} = require('kite-common');`

Then, in this file, we implement the ScreenshotStep class and its constructor:
```
class ScreenshotStep extends TestStep {
    constructor(kiteBaseTest) {
        super();
        this.driver = kiteBaseTest.driver;

        // Test reporter if you want to add attachment(s)
        this.testReporter = kiteBaseTest.reporter;
    }
}
```  
Update the __stepDescription()__:

    stepDescription() {
        return 'Get a screenshot';
    }

Then the __step()__:

    async step() {
        let screenshot = await TestUtils.takeScreenshot(this.driver);
        this.testReporter.screenshotAttachment(this.report, "Screenshot step", screenshot);
    }

Do not forget, at the end of this file, add:
`module.exports = ScreenshotStep;`
See [ScreenshotStep.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/steps/ScreenshotStep.js).

The __TestUtils.takeScreenshot()__ allows to take a screenthot.

Finally, we add this step to __JitsiTutorial.js__.
We modify:
`const {OpenUrlStep, GetStatsStep} = require('./steps');`
by
`const {OpenUrlStep, GetStatsStep, ScreenshotStep} = require('./steps');`

and add in __testScript()__:

    this.driver = await WebDriverFactory.getDriver(capabilities, capabilities.remoteAddress);
    this.page = new MainPage(this.driver);
    let openUrlStep = new OpenUrlStep(this);
    await openUrlStep.execute(this);
    let sentVideoCheck = new SentVideoCheck(this);
    await sentVideoCheck.execute(this);
    let receivedVideoCheck = new ReceivedVideoCheck(this);
    await receivedVideoCheck.execute(this);
    if (this.getStats) {
        let getStatsStep = new new GetStatsStep(this);
        await getStatsStep.execute(this);
    }
    // New step
    let screenshotStep = new ScreenshotStep(this);
    await screenshotStep.execute(this);

See:
[JitsiTutorial.js](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/js/JitsiTutorial.js).
[js.jitsitutorial.config.json](https://github.com/CoSMoSoftware/KITE-JitsiTutorial-Test/blob/master/configs/js.jitsitutorial.config.json).

We can also do the same as getStats to enable or disable this step more easily. We just have to add "takeScreenshotForEachTest": true, in __configs/js.jitsiTutorial.config.json__ in the payload.

We can run the test like mentioned above.
Finally, the report should be now:
    ![Screenshot Step Allure Report](./img/Screenshot6AllureReport.png)
*****

Congratulations, you've implemented your first KITE Test in javascript! 
