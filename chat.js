var server = require('http').Server(app);
var io = require('socket.io')(server);
var path = require('path');
var express = require('express');
var app = express();
server.listen(4000);
var mongoose = require('mongoose');
var uuid = require('node-uuid');
var markdown = require('markdown').markdown;

mongoose.connect('mongodb://localhost:27017/test');

var Chat = require('../database/chat.js');



var clients = 0;
var sender;
var rec;
var roomName;

function getRoomName(user, reciever, callback) {
    Chat.Room.findOne({ s_id: user, r_id: reciever }, function(err, room) {
        if (room != null) {

            sender = user;
            rec = reciever;
            roomName = room.room_id;
            console.log(reciever);
            return callback(roomName, sender, rec);
        } else {
            Chat.Room.findOne({ s_id: reciever, r_id: user }, function(err, room) {
                if (room != null) {
                    sender = user;
                    rec = reciever;
                    roomName = room.room_id;
                    console.log(reciever);
                    return callback(roomName, sender, rec);
                }
            });
        }
    });
}

function getMessages(sender,callback){
    Chat.ChatL.find({s_id: sender}, function(err,messages){
        if(messages != null)
        {
            console.log(messages);
            return callback(sender,messages);
        }        
    });
}

io.on('connection', function(socket) {



    console.log('User Connected');

    socket.on('new user', function(user, reciever, callback) {
        // if(data in usernames){
        //  callback(false)
        // }

        callback(true);


        getRoomName(user, reciever, function(roomN) {

            console.log(roomN);
            socket.username = user
            socket.join(roomN);

            // io.sockets.emit('username', socket.username);

        });
    });


    var message = new Chat.Message();

    socket.on('chat message', function(data) {

        Chat.Message.find({ room_id: roomName }, function(err, data) {
            // console.log(data);
            for (var x = 0; x < data.length; x++) {
                // console.log("sender: " + data[x].s_id + " reciever: " + data[x].r_id + " message: " + data[x].message_content + " time: " + data[x].created_date);
            }
        });
        io.in(roomName).emit('message', { msg: markdown.toHTML(data).slice(3,-4), user: socket.username });

        message.s_id = socket.username;
        message.room_id = roomName;
        message.message_id = uuid.v4();
        message.message_content = data;
        console.log(sender+" "+rec);
        // FIX THE RECIEVER
        message.r_id = rec;

        message.save(function(err) {
            if (err)
                console.log(err);
            else
                console.log("data added");
        });

    });



    socket.on('disconnect', function() {
        console.log('User disconnected');
        //     return;
        // // delete usernames[socket.username];
        // // usernames.splice(usernames.indexOf(socket.username), 1);
        // // clients--;
        // io.sockets.emit('username', usernames);
    });

});

module.exports = app;
