var UID = String(document.location).split('=')[1]

var oldContent = "null";
var oldChatroomID = 0;

$(document).ready(function(){
	console.log('document ready');
   setInterval ( "loadData()", 200 );
 });



function loadData() {   
    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', 'http://ether.remap.ucla.edu/glass/graceplains/backup/web/data.py/getContentFor?uid='+UID, true);
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
	if (status.chatroomID != oldChatroomID)
	{
		var s = document.getElementsByTagName("body")[0];
		s.innerHTML += "<script type=\"text/javascript\">var chatRoomId = " + status.chatroomID + ";</script>";
		document.getElementById('chatroomid').innerHTML = status.chatroomID;
		console.log(s);
		oldChatroomID = status.chatroomID;
	}
}


function playVideo(){
console.log("playing video ? ")
var video = document.getElementById('video');

  video.play();
}
