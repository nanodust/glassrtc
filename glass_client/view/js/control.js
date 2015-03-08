
$(document).ready(function(){
	console.log('document ready');
    //loadUIDs();
    loadMedia();
    $("#chatbox").change(function(){
    $(glassview).html($(chatbox).val());
    $("#chatbox" ).bind('keypress', function(e){
   if ( e.keyCode == 13 && e.ctrlKey) {
     insertContent()
     console.log("ctrl enter pressed ")
   }
 });
    //console.log("changed ! ");
    })
 });


function loadMedia() {   
    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', 'http://ether.remap.ucla.edu/glass/view/database.py/getAllMedia');
   	console.log('loading data');
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            var jsonData = xobj.responseText;
            processMedia(jsonData);
        }
    }
    xobj.send(null);
    // $(glassview).html($(chatbox).val());
}


function processMedia(data){
	//console.log('data loaded' + data);
	var vids = eval('(' + data + ')');
	
	$('#glasses').html("");
	
	$.each(vids, function(key, value) {   
     $('#glasses')
      .append($("<option></option>")
      .attr("value",vids[key].src)
      .text(vids[key].name)); 
      });
}



function loadUIDs() {   
    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', 'http://ether.remap.ucla.edu/glass/data.py/getAllUID');
    	//console.log('loading data');
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            var jsonData = xobj.responseText;
            processUID(jsonData);
        }
    }
    xobj.send(null);
     $(glassview).html($(chatbox).val());
}


function processUID(data){
	var uids = eval('(' + data + ')');
	
	$('#glasses').html("");
	
	$.each(uids, function(key, value) {   
     $('#glasses')
      .append($("<option></option>")
      .attr("value",uids[key].uid)
      .text(uids[key].uid)); 
      });
}


function clearUIDs() {   
console.log("clearing out data")
    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', 'http://ether.remap.ucla.edu/glass/data.py/clearAll', false);
        xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            console.log("cleared db "+xobj.responseText)
            } else {
            
            }
            }
            xobj.send(null);
            }
    
    
function insertContent(){	
console.log("inserting content..");
	// for each selected UID, send contents of text box
	$('.glasses option:selected').each(function(idx, item){
		sendContent(item.value);
		})
		}
		
function sendContent(uid){
	content = jQuery('textarea#chatbox').val();
	content = encodeURI(content)
	url = "http://ether.remap.ucla.edu/glass/data.py/insertContentFor?uid="+uid+"&content="+content
	var xobj = new XMLHttpRequest();
    xobj.overrideMimeType("application/json");
    xobj.open('GET', url, false);
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            console.log("wrote new content "+xobj.responseText)
            }
            }
            xobj.send(null);
}