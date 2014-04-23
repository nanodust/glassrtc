var UID = String(document.location).split('=')[1]

var oldContent = "null";

 
$(document).ready(function(){
	console.log('document ready');
   setInterval ( "loadData()", 200 );
 });



function loadData() {   
    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', 'http://ether.remap.ucla.edu/glass/data.py/getContentFor?uid='+UID, true);
    	//console.log('loading data');
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            var jsonData = xobj.responseText;
            processData(jsonData);
        }
    }
    xobj.send(null);
}


function processData(data){
	console.log("prpcessog datsa");
	var status = eval('(' + data + ')');
	if(status.content != oldContent){
		$(content).html(status.content);
		oldContent = status.content;
		//playVideo();
		}
}


function playVideo(){
console.log("playing video ? ")
var video = document.getElementById('video');

  video.play();
}