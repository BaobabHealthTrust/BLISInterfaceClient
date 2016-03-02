
var path = require("path");
var exec = require('child_process').exec;
var fs = require('fs');

setTimeout(function() {
    var files = fs.readdirSync(path.resolve("machines", "active"));

    for (var i in files) {

				// java -cp lib/JSSC/jssc-2.8.0.jar:lib/jackson/jackson-annotations-2.2.3.jar:lib/jackson/jackson-core-2.2.3.jar:lib/jackson/jackson-databind-2.2.3.jar:lib/AbsoluteLayout.jar:BLISInterfaceClient.jar ui.MainForm

        var child = exec('cd ' + path.resolve('machines', 'active', files[i]) + '; java -cp lib/JSSC/jssc-2.8.0.jar:lib/jackson/jackson-annotations-2.2.3.jar:lib/jackson/jackson-core-2.2.3.jar:lib/jackson/jackson-databind-2.2.3.jar:lib/AbsoluteLayout.jar:BLISInterfaceClient.jar ui.MainForm',
            function (error, stdout, stderr) {
                console.log('Output -> ' + stdout);
                if (error !== null) {
                    console.log("Error -> " + error);
                }
            });

    }

}, 1000);
