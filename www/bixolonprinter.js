var argscheck = require('cordova/argscheck'),
    channel = require('cordova/channel'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec'),
    cordova = require('cordova');

function BixolonPrinter () {
}

BixolonPrinter.prototype.connect = function (str, callback) {
    exec(callback, function(err) {
        callback('Nothing to echo.');
    }, "BixolonPrinter", "connect", [str]);
}

BixolonPrinter.prototype.print = function (str, callback) {
    exec(callback, function(err) {
        callback('Nothing to echo.');
    }, "BixolonPrinter", "print", [str]);
}

module.exports = new BixolonPrinter();