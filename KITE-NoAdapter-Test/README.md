# Basic interoperability test without adapter.js:

* node.js based minimal web app and signalling server, in KITE-NoAdapter-Test/server. See below for instructions.
* KITE Test relying on the web app and minimal signalling server, execute as any other KITE test once server side is ready.

# Simple signalling server for NoAdapter test

Web app and minimalistic signalling service based on node, using socket.io, in server directory

To start the server, change to server directory and run

```sh
npm install && npm start
```

To stop the server, just interrupt the starting command or change to the server directory and run

```sh
npm stop
```

By default server listens to port 8083. To change this, launch the server as follow instead:

```sh
node server.js PORT
```

The port argument are not mandatory.

By default server listens in all interfaces, to change this, edit server.js. If you want to test on lots of different browser configurations. We recommend using a server with a CA to avoid any problem with safari and edge.

If the test is to be run on a Selenium Grid, the IP address for the server should be the IP address of the machine on which it is being run, instead of http(s)://localhost:8082(3). You can change this by adding a payload object in the config file, as follow:

    {
        "name": "NoAdapterTest",
        "tupleSize": 2,
        "noOfThreads": 3,
        "description": "This test checks the ICEConnection state between two browser peer connections without adapter.js",
        "testImpl": "org.webrtc.kite.NoAdapterTest",
        "payload": {
            "ip": "SERVER_IP",
            "port": PORT
        }
    }

## How to generate server's SSL certificate

The files server.cert and server.key, which will be used to create the https server 
are initially empty. You should create your own sefl-signed certificate and 
replace them in the config folder.


```
openssl req -new -newkey rsa:2048 -nodes -out server.cert -keyout server.key
```