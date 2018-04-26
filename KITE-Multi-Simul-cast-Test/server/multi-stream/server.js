//
// Copyright 2017 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
// 
// https://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and limitations under
// the License.
//

'use strict';

var https_port = 8085;

var express = require('express');
var http = require('http');
var https = require('https');
var fs = require('fs');
var helmet = require('helmet');
var io = require('socket.io');

var app = express()
  .use(helmet())
  .use(express.static('./test'));

var ioSocket = new io();

if (process.argv.length > 2){
  https_port = process.argv[2];
}

var options = {
  key: fs.readFileSync('config/server.key'),
  cert: fs.readFileSync('config/server.cert')
};
// Create HTTPS server and attaches it to socket.io channel
ioSocket.attach(https.createServer(options, app).listen(https_port));
console.log('serving on https://'+ '*' + ':' + https_port );

ioSocket.on('connection', function (socket) {
  socket.emit('serverNews', { hello: 'world' });
  socket.on('hello', function (data) {
    var receivedId = data.id;
    socket.broadcast.emit('hello', data);
  });
  socket.on('goodbye', function (data) {
  });
  socket.on('webrtc', function (data) {
    var srcId = data.from;
    var destId = data.to;
    socket.broadcast.emit('webrtc', { channel: data.channel, from: data.from, to: data.to, msg: data.msg });
  });
});