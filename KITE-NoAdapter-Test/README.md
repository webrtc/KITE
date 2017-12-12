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

By default server listens in 8080 port, to change this, edit server.http.js

By default server listens in all interfaces, to change this, edit server.http.js

If the test is to be run on a Selenium Grid, the IP address for the server should be the IP address of the machine on which it is being run, instead of http://localhost:8080.
