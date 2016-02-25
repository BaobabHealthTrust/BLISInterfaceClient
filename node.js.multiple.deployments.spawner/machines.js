
var path = require("path");
var exec = require('child_process').exec;
var fs = require('fs');

var files = fs.readdirSync(path.resolve("machines", "active"));

for(var i in files) {

    var child = exec('cd ' + path.resolve('machines', 'active', files[i]) + '; java -jar ' + path.resolve('machines',
            'active', files[i], 'BLISInterfaceClient.jar'),
        function (error, stdout, stderr) {
            console.log('Output -> ' + stdout);
            if (error !== null) {
                console.log("Error -> " + error);
            }
        });

}