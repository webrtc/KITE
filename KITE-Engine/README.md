# KITE-Engine
The core Engine, grid, test and dashboard agnostic.

# Command to Run
java -cp kite-jar-with-dependencies.jar:[Path and name of the test implementation].jar org.webrtc.kite.Engine [Path and name of config file].json

# Examples:
## AppRTC
java -cp kite-jar-with-dependencies.jar:apprtc-test-1.0.jar org.webrtc.kite.Engine local.config.json
  
    {
        "name": "local selenium example",
        "callback": "http://localhost:8080/kiteweb/datacenter",
        "remotes": [
            {
                "type": "local",
                "remoteAddress": "http://localhost:4444/wd/hub"
            }
        ],
        "tests": [
            {
                "name": "IceConnectionTest",
                "tupleSize": 2,
                "description": "This test check the ICEConnection state between two browsers communicating via appr.tc",
                "testImpl": "org.webrtc.kite.IceConnectionTest"
            }
        ],
        "browsers": [
            {
                "browserName": "chrome",
                "version": "57.0",
                "platform": "MAC"
            },
            {
                "browserName": "firefox",
                "version": "45.0",
                "platform": "LINUX"
            },
            {
                "browserName": "MicrosoftEdge",
                "version": "14.0",
                "platform": "WINDOWS"
            }
        ]
    }
