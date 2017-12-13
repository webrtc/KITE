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

var express = require('express');
var http = require('http');

var app = express();
var helmet = require('helmet')
app.use(helmet())

app.use(express.static('./test'));

// FIXME: use an argument
var port = 8082;

// Create an HTTP service.
var server = http.Server(app);
var io = require('socket.io')(server);
server.listen(port);

io.on('connection', function (socket) {
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

console.log('serving on http://*:' + port );

