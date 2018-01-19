//
//	Copyright 2017 Google Inc.
//	
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//	
//	    https://www.apache.org/licenses/LICENSE-2.0
//	
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//
window.onload = function() {

    // Get references to elements on the page.
    var configuration = document.getElementById('configuration');
    var configurationName = document.getElementById('configuration-name');
    var callback = document.getElementById('callback');
    var gridList = document.getElementById('grid-list');
    var testList = document.getElementById('test-list');
    var browserList = document.getElementById('browser-list');
    var configText;
    var grids =[];
    grids[0] = {name:"", add:"", user:"", key:""};
    var tests =[];
    tests[0] = {name:"", size:"", impl:""};
    var browsers =[];
    browsers[0] = {name:"", version:"", platform:""};
    var gridCount = 0;
    var testCount = 0;
    var browserCount = 0;
    var socketStatus = document.getElementById('status');
    var closeBtn = document.getElementById('close');


    // Create a new WebSocket.
    var socket = new WebSocket('ws://localhost:8080/kiteweb/configurator');


    // Handle any errors that occur.
    socket.onerror = function(error) {
    };


    // Show a connected message when the WebSocket is opened.
    socket.onopen = function(event) {
       /* socketStatus.innerHTML = 'Create a new configuration file';
        socketStatus.className = 'open';*/
    };


    // Handle messages sent by the server.
    socket.onmessage = function(event) {
        var message = event.data;
        configuration.innerHTML =message;
    };


    // Show a disconnected message when the WebSocket is closed.
    socket.onclose = function(event) {
        /*socketStatus.innerHTML = 'Disconnected from configurator. Please try refreshing the page.';
        socketStatus.className = 'closed';*/
    };


    $(document).ready(function(){
        $("input").focusout(function() {
        if (this.value !='' && this.value != null ){
            var message = this.id+"|"+this.value;
            //socket.send(message);
        }
        });
    });


    $(document).on("click", ".local", function(e) {
        var tmp = 'grid-'+gridCount;
        if (gridCount>0){
            for(i=0; i<gridCount; i++){
                var name = document.getElementById('grid-'+(i)+'-name');
                var add = document.getElementById('grid-'+(i)+'-add');
                var user = document.getElementById('grid-'+(i)+'-user');
                var key = document.getElementById('grid-'+(i)+'-key');
                var grid = {name:name.value, add:add.value, user:user.value, key:key.value};
                grids[i] = grid;
            }
            if (grids[gridCount-1].name!=""&&grids[gridCount-1].add!=""&&grids[gridCount-1].user!=""&&grids[gridCount-1].key!=""){
                gridList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-grid" name="'+gridCount+'"><i class="fa fa-minus"></i></button>'
                        +'<input type="text" id="'+tmp+'-name" class="field-small" value="Local" disabled />'
                        +'&nbsp;<input type="text" id="'+tmp+'-add" class="field-divided" placeholder="Address" />'
                        +'&nbsp;<input type="text" id="'+tmp+'-user" class="field-small" value="User Name" disabled/>'
                        +'&nbsp;<input type="text" id="'+tmp+'-key" class="field-small" value="Access Key" disabled/></li>';
                for(i=0; i<gridCount; i++){
                    var name = document.getElementById('grid-'+(i)+'-name');
                    var add = document.getElementById('grid-'+(i)+'-add');
                    var user = document.getElementById('grid-'+(i)+'-user');
                    var key = document.getElementById('grid-'+(i)+'-key');
                    name.value = grids[i].name;
                    add.value = grids[i].add;
                    user.value = grids[i].user;
                    key.value = grids[i].key;
                }
                gridCount+=1;
            }
        }else{
            gridList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-grid" name="'+gridCount+'"><i class="fa fa-minus"></i></button>'
                    +'<input type="text" id="'+tmp+'-name" class="field-small" value="Local" disabled />'
                    +'&nbsp;<input type="text" id="'+tmp+'-add" class="field-divided" placeholder="Address" />'
                    +'&nbsp;<input type="text" id="'+tmp+'-user" class="field-small" value="User Name" disabled/>'
                    +'&nbsp;<input type="text" id="'+tmp+'-key" class="field-small" value="Access Key" disabled/></li>';
            gridCount+=1;
        }


    });
    $(document).on("click", ".remote", function(e) {
        var tmp = 'grid-'+gridCount;
        var remoteId = $(this).attr('name');
        if (gridCount>0){
            for(i=0; i<gridCount; i++){
                var name = document.getElementById('grid-'+(i)+'-name');
                var add = document.getElementById('grid-'+(i)+'-add');
                var user = document.getElementById('grid-'+(i)+'-user');
                var key = document.getElementById('grid-'+(i)+'-key');
                var grid = {name:name.value, add:add.value, user:user.value, key:key.value};
                grids[i] = grid;
            }
            if (grids[gridCount-1].name!=""&&grids[gridCount-1].add!=""&&grids[gridCount-1].user!=""&&grids[gridCount-1].key!=""){
                gridList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-grid" name="'+gridCount+'"><i class="fa fa-minus"></i></button>'
                        +'<input type="text" id="'+tmp+'-name" class="field-small" value="'+remoteId+'" disabled />'
                        +'&nbsp;<input type="text" id="'+tmp+'-add" class="field-small" value="Address" disabled/>'
                        +'&nbsp;<input type="text" id="'+tmp+'-user" class="field-divided" placeholder="User Name" />'
                        +'&nbsp;<input type="text" id="'+tmp+'-key" class="field-divided" placeholder="Access Key" /></li>';
                for(i=0; i<gridCount; i++){
                    var name = document.getElementById('grid-'+(i)+'-name');
                    var add = document.getElementById('grid-'+(i)+'-add');
                    var user = document.getElementById('grid-'+(i)+'-user');
                    var key = document.getElementById('grid-'+(i)+'-key');
                    name.value = grids[i].name;
                    add.value = grids[i].add;
                    user.value = grids[i].user;
                    key.value = grids[i].key;
                }
                gridCount+=1;
            }
        }else{
            gridList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-grid" name="'+gridCount+'"><i class="fa fa-minus"></i></button>'
                    +'<input type="text" id="'+tmp+'-name" class="field-small" value="'+remoteId+'" disabled />'
                    +'&nbsp;<input type="text" id="'+tmp+'-add" class="field-small" value="Address" disabled/>'
                    +'&nbsp;<input type="text" id="'+tmp+'-user" class="field-divided" placeholder="User Name" />'
                    +'&nbsp;<input type="text" id="'+tmp+'-key" class="field-divided" placeholder="Access Key" /></li>';
            gridCount+=1;
        }

    });

    $(document).on("click", ".add-test", function(e) {
        var tmp = 'test-'+testCount;
        if (testCount>0){
            for(i=0; i<testCount; i++){
                var name = document.getElementById('test-'+(i)+'-name');
                var size = document.getElementById('test-'+(i)+'-size');
                var impl = document.getElementById('test-'+(i)+'-impl');
                var test = {name:name.value, size:size.value, impl:impl.value};
                tests[i] = test;
            }
            if (tests[testCount-1].name!=""&&tests[testCount-1].size!=""&&tests[testCount-1].impl!=""){
                testList.innerHTML += '<li> <button type="button" class="btn btn-default btn-circle minus-test" name="'+testCount+'"><i class="fa fa-minus"></i></button>'
                        +'<input type="text" id="'+tmp+'-name" class="field-divided" placeholder="Test name" />'
                        +'&nbsp;<input type="text" id="'+tmp+'-size" class="field-small" placeholder="Tuple size" />'
                        +'&nbsp;<input type="text" id="'+tmp+'-impl" class="field-divided" placeholder="Test implementation class" /></li>';
                for(i=0; i<testCount; i++){
                    var name = document.getElementById('test-'+(i)+'-name');
                    var size = document.getElementById('test-'+(i)+'-size');
                    var impl = document.getElementById('test-'+(i)+'-impl');
                    name.value = tests[i].name;
                    size.value = tests[i].size;
                    impl.value = tests[i].impl;
                }
                tests[testCount] = {name:"", size:"", impl:""};
                testCount+=1;
            }
        }else{
            testList.innerHTML += '<li> <button type="button" class="btn btn-default btn-circle minus-test" name='+testCount+'><i class="fa fa-minus"></i></button>'
                    +'<input type="text" id="'+tmp+'-name" class="field-divided" placeholder="Test name" />'
                    +'&nbsp;<input type="text" id="'+tmp+'-size" class="field-small" placeholder="Tuple size" />'
                    +'&nbsp;<input type="text" id="'+tmp+'-impl" class="field-divided" placeholder="Test implementation class" /></li>';
            tests[testCount] = {name:"", size:"", impl:""};
            testCount+=1;
        }

    });


    $(document).on("click", ".add-browser", function(e) {
        var tmp = 'browser-'+browserCount;
        if (browserCount>0){
            for(i=0; i<browserCount; i++){
                var name = document.getElementById('browser-'+(i)+'-name');
                var version = document.getElementById('browser-'+(i)+'-version');
                var platform = document.getElementById('browser-'+(i)+'-platform');
                var browser = {name:name.value, version:version.value, platform:platform.value};
                browsers[i] = browser;
            }
            if (browsers[browserCount-1].name!=""&&browsers[browserCount-1].version!=""&&browsers[browserCount-1].platform!=""){
                browserList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-browser" name='+browserCount+'><i class="fa fa-minus"></i></button>'
                +'<input type="text" id="'+tmp+'-name" class="field-divided" placeholder="Browser name" />'
                +'&nbsp;<input type="text" id="'+tmp+'-version" class="field-small" placeholder="Version" />'
                +'&nbsp;<input type="text" id="'+tmp+'-platform" class="field-divided" placeholder="Platform" /></li>';
                for(i=0; i<browserCount; i++){
                    var name = document.getElementById('browser-'+(i)+'-name');
                    var version = document.getElementById('browser-'+(i)+'-version');
                    var platform = document.getElementById('browser-'+(i)+'-platform');
                    name.value = browsers[i].name;
                    version.value = browsers[i].version;
                    platform.value = browsers[i].platform;
                }
                browsers[browserCount] = {name:"", version:"", platform:""};
                browserCount+=1;
            }
        }else{
            browserList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-browser" name='+browserCount+'><i class="fa fa-minus"></i></button>'
            +'<input type="text" id="'+tmp+'-name" class="field-divided" placeholder="Browser name" />'
            +'&nbsp;<input type="text" id="'+tmp+'-version" class="field-small" placeholder="Version" />'
            +'&nbsp;<input type="text" id="'+tmp+'-platform" class="field-divided" placeholder="Platform" /></li>';
            browserCount+=1;
        }
    });

    $(document).on("click", ".minus-test", function(e) {
        var name = document.getElementById('test-'+(testCount-1)+'-name');
        var size = document.getElementById('test-'+(testCount-1)+'-size');
        var impl = document.getElementById('test-'+(testCount-1)+'-impl');
        var test = {name:name.value, size:size.value, impl:impl.value};
        tests[testCount-1] = test;
        var id = parseInt($(this).attr('name'));
        tests.splice(id,1);
        testCount-=1;
        testList.innerHTML='<li> <label>Test(s)</label>'
            +'<button type="button" class="btn btn-default btn-circle add-test" name="test"><i class="fa fa-plus"></i></button></li>';
        for(i=0; i<testCount; i++){
        var tmp = 'test'+i;
        testList.innerHTML += '<li> <button type="button" class="btn btn-default btn-circle minus-test" name="'+i+'"><i class="fa fa-minus"></i></button>'
            +'<input type="text" id="'+tmp+'-name" class="field-divided" value="'+tests[i].name+'" />'
            +'&nbsp;<input type="text" id="'+tmp+'-size" class="field-small" value="'+tests[i].size+'" />'
            +'&nbsp;<input type="text" id="'+tmp+'-impl" class="field-divided" value="'+tests[i].impl+'" /></li>';
        }
    });

    $(document).on("click", ".minus-browser", function(e) {
        var name = document.getElementById('browser-'+(browserCount-1)+'-name');
        var version = document.getElementById('browser-'+(browserCount-1)+'-version');
        var platform = document.getElementById('browser-'+(browserCount-1)+'-platform');
        var browser = {name:name.value, version:version.value, platform:platform.value};
        browsers[browserCount-1] = browser;
        var id = parseInt($(this).attr('name'));
        browsers.splice(id,1);
        browserCount-=1;
        browserList.innerHTML='<li> <label>Browser(s)</label>'
            +'<button type="button" class="btn btn-default btn-circle add-browser" name="browser"><i class="fa fa-plus"></i></button></li>';
        for(i=0; i<browserCount; i++){
        var tmp = 'browser'+i;
            browserList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-browser" name='+i+'><i class="fa fa-minus"></i></button>'
            +'<input type="text" id="'+tmp+'-name" class="field-divided" value="'+browsers[i].name+'" />'
            +'&nbsp;<input type="text" id="'+tmp+'-version" class="field-small" value="'+browsers[i].version+'" />'
            +'&nbsp;<input type="text" id="'+tmp+'-platform" class="field-divided" value="'+browsers[i].platform+'" /></li>';
        }
    });

    $(document).on("click", ".minus-grid", function(e) {
        var name = document.getElementById('grid-'+(gridCount-1)+'-name');
        var add = document.getElementById('grid-'+(gridCount-1)+'-add');
        var user = document.getElementById('grid-'+(gridCount-1)+'-user');
        var key = document.getElementById('grid-'+(gridCount-1)+'-key');
        var grid = {name:name.value, add:add.value, user:user.value, key:key.value};
        grids[gridCount-1] = grid;
        var id = parseInt($(this).attr('name'));
        grids.splice(id,1);
        gridCount-=1;
        gridList.innerHTML='<li>'
                +'<label>Grid(s)</label>'
                +'<div class="dropdown">'
                    +'<button type="button" class="btn btn-default btn-circle dropdown-toggle" data-toggle="dropdown"><i class="fa fa-plus"></i></button>'
                    +'<ul class="dropdown-menu">'
                    +'<li><a name="remote" class="local">Local</a></li>'
                    +'<li><a name="SauceLabs" class="remote">SauceLabs</a></li>'
                    +'<li><a name="BrowserStack" class="remote">BrowserStack</a></li>'
                    +'<li><a name="TestingBot" class="remote">TestingBot</a></li>'
                    +'</ul>'
                +'</div>'
            +'</li>';
        for(i=0; i<gridCount; i++){
        var tmp = 'grid'+i;
        if (grids[i].name=='Local')
            gridList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-grid" name="'+i+'"><i class="fa fa-minus"></i></button>'
                    +'<input type="text" id="'+tmp+'-name" class="field-small" value="'+grids[i].name+'" disabled />'
                    +'&nbsp;<input type="text" id="'+tmp+'-add" class="field-small" value="'+grids[i].add+'" />'
                    +'&nbsp;<input type="text" id="'+tmp+'-user" class="field-divided" value="'+grids[i].user+'" disabled/>'
                    +'&nbsp;<input type="text" id="'+tmp+'-key" class="field-divided" value="'+grids[i].key+'" disabled/></li>';
        else
            gridList.innerHTML += '<li><button type="button" class="btn btn-default btn-circle minus-grid" name="'+i+'"><i class="fa fa-minus"></i></button>'
                    +'<input type="text" id="'+tmp+'-name" class="field-small" value="'+grids[i].name+'" disabled />'
                    +'&nbsp;<input type="text" id="'+tmp+'-add" class="field-small" value="'+grids[i].add+'" disabled/>'
                    +'&nbsp;<input type="text" id="'+tmp+'-user" class="field-divided" value="'+grids[i].user+'" />'
                    +'&nbsp;<input type="text" id="'+tmp+'-key" class="field-divided" value="'+grids[i].key+'" /></li>';
        }
    });

    $(document).on("click", ".create", function(e) {
        configText = '';
        configText += '<p>{</p>';
        configText+='<p>\"name\": \"'+configurationName.value +'\",</p>';
        if(callback.value!="")
            configText+='<p>\"callback\": \"'+callback.value +'\",</p>';
        else
            configText+='<p>\"callback\": null,</p>';
        configText+= '<p>\"remotes\": [</p>';
        for (i=0; i<gridCount; i++){
            configText+='<p>{</p>';
            var name = document.getElementById('grid-'+(i)+'-name');
            var add = document.getElementById('grid-'+(i)+'-add');
            var user = document.getElementById('grid-'+(i)+'-user');
            var key = document.getElementById('grid-'+(i)+'-key');
            configText+='<p>\"type\": \"'+name.value +'\",</p>';
            if(name.value =='Local')
                configText+='<p>\"remoteAddress\": \"'+add.value +'\",</p>';
            else{
                configText+='<p>\"username\": \"'+user.value +'\",</p>';
                configText+='<p>\"accesskey\": \"'+key.value +'\",</p>';
            }
            if(i==gridCount-1)
                configText+='<p>}</p>';
            else
                configText+='<p>},</p>';
        }
        configText+= '<p>],</p>';
        configText+= '<p>\"tests\": [</p>';
        for (i=0; i<browserCount; i++){
            configText+='<p>{</p>';
                var name = document.getElementById('test-'+(i)+'-name');
                var size = document.getElementById('test-'+(i)+'-size');
                var impl = document.getElementById('test-'+(i)+'-impl');
            configText+='<p>\"name\": \"'+name.value +'\",</p>';
            configText+='<p>\"vtupleSizen\": \"'+size.value +'\",</p>';
            configText+='<p>\"ptestImplm\": \"'+impl.value +'\",</p>';
            if(i==browserCount-1)
                configText+='<p>}</p>';
            else
                configText+='<p>},</p>';
        }
        configText+= '<p>],</p>';
        configText+= '<p>\"browsers\": [</p>';
        for (i=0; i<browserCount; i++){
            configText+='<p>{</p>';
            var name = document.getElementById('browser-'+(i)+'-name');
            var version = document.getElementById('browser-'+(i)+'-version');
            var platform = document.getElementById('browser-'+(i)+'-platform');
            configText+='<p>\"name\": \"'+name.value +'\",</p>';
            configText+='<p>\"version\": \"'+version.value +'\",</p>';
            configText+='<p>\"platform\": \"'+platform.value +'\",</p>';
            if(i==browserCount-1)
                configText+='<p>}</p>';
            else
                configText+='<p>},</p>';
        }
        configText+= '<p>]</p>';
        configText+= '<p>}</p>';
        configuration.innerHTML=configText;
    });
};
