
var selectedText = "";

$(document).ready(function(){
	console.log('document ready');
    loadUIDs();
    //loadMedia();
    //$("#chatbox").change(function(){
    //$(glassview).html($(chatbox).val());
    $("#chatbox" ).bind('keypress', function(e){
    	console.log(e.keyCode);
    	   e.preventDefault();
    	   ShowSelection();
	 });
    //console.log("changed ! ");
    //})
    console.log('triggering..');
    //document.onmouseup = function (e) {  }
    
    var isChrome = /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor);
	var isSafari = /Safari/.test(navigator.userAgent) && /Apple Computer/.test(navigator.vendor);

	if((!isChrome)&&(!isSafari)){
		alert("please use either chrome or safari");
	}
	
	clearUIDs();
    
 });

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
    
    
function insertContent(what){	
console.log("inserting content.." + what);
	// for each selected UID, send contents of text box
	$('.glasses option:selected').each(function(idx, item){
		sendContent(item.value, what);
		})
		}
		
function sendContent(uid, what){
	//content = jQuery('textarea#chatbox').val();
	content = encodeURI(what)
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

function ShowSelection()
{
  var textComponent = document.getElementById('chatbox');
  var selectedText;
  // IE version
  if (document.selection != undefined)
  {
    textComponent.focus();
    var sel = document.selection.createRange();
    selectedText = sel.text;
  }
  // Mozilla version
  else if (textComponent.selectionStart != undefined)
  {
    var startPos = textComponent.selectionStart;
    var endPos = textComponent.selectionEnd;
    selectedText = textComponent.value.substring(startPos, endPos)
  }
  console.log("You selected: " + selectedText);
  
  selectedText = selectedText;
  
    
  insertContent(selectedText);
  $(glassview).html(selectedText);
  //clearUIDs();
}