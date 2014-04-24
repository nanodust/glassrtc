//GPAdmin UI code by Zhehao on Apr 15th, 2014

//GPAdmin UI log quick fixes:
//quick fixes for automatically loading ampping sheet
//cheap hack for loading: expecting character not added or dismissed.
//only load the phrases the first time character's loaded.


//USEGLASSCHAT macro defines whether we use glass chat code and glass uids
var USEGLASSCHAT = true;

//key constants
var KEYUP = 38;
var KEYDOWN = 40;
var KEYENTER = 13;
var KEYSPACE = 32;

var KEYZERO = 48;
var KEYNINE = 57;

//global vars for num of players and player name array
var nameList = new Array();
var characterNum = 0;
var characterMap = new Array();

var activeList = new Array();
var scriptNum = 0;

var currentIdx = 0;

var mapLoaded = false;
var whenToPushEnabled = true;

//Send button click or similar action: calls send(uid, what, chatroomID) and logging using PHP code
function sendClick()
{
	for (var i = 0; i<characterNum; i++)
	{
		if (document.getElementById('send' + i).checked == true)
		{
			var pushMsg = document.getElementById('inputBox').value;
			send(i, pushMsg);
			//format for history and writing to spreadsheet
			
			var currentTime = new Date();
			var timeStamp = currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds() + "." + Math.round(currentTime.getMilliseconds() / 100);
			
			var sender = document.getElementById('adminName').value;
			var receiver = nameList[i].charactername;
			
			document.getElementById('historyBox').value = timeStamp + ": " + sender + " => " + receiver + " : \'" + pushMsg + "\'\n" + document.getElementById('historyBox').value;
			//moveCursorToEnd(document.getElementById('historyBox'));
		
			//it takes time to do the following. Tested with fast clicking of 'send', seems to work well.
			//one token should be good for several 'send's
			url = "php/TestCode.php?time="+timeStamp+"&sender="+sender+"&receiver="+receiver+"&line="+pushMsg;
			$.get(url);
		}
	}
	inputPreview();
}

//Generate input preview in previewDiv
function inputPreview()
{
	document.getElementById('previewDiv').innerHTML = document.getElementById('inputBox').value;
}

//separate onkeyup event from onkeydown event, 
//onkeyup happens after the letter appears in the inputbox, so we want to preview input after the modification's done
//onkeydown happens before the letter appears, so we want to check if it's enter, and if it is, block it.

//inputBox onkeyup event
function inputBoxKeyup()
{
	inputPreview();
}

//inputBox onkeydown event
function inputBoxKeydown(event)
{
	if (event.keyCode == KEYENTER)
	{
		//may need to check for illegal characters before sending
		sendClick();
		
		//ok, the actual admins don't really like this feature
		/*
		var txtarea = document.getElementById('txtarea' + currentIdx);
		if (txtarea!=null)
		{
			txtarea.focus();
			selectPhrase(currentIdx);
		}
		*/
		event.preventDefault();
	}
}

//playerList oncheck(onchange) event
//checkSend is called manually if the checkbox is checked/unchecked by code.
//span vs div...
function checkSend(obj)
{
	var id = "character" + obj.id[4];
	var ele = document.getElementById(id);
	if (document.getElementById(obj.id).checked)
	{
		ele.style.color = "#EDD400";
	}
	else
	{
		ele.style.color = "#FFFFFF";
	}
}

//lockList oncheck(onchange) event
function checkLock(obj)
{
	//console.log(obj.id);
	var sendCheck = "send" + obj.id[4];
	document.getElementById(sendCheck).checked = false;
	document.getElementById(sendCheck).disabled = !(obj.checked);
	checkSend(document.getElementById(sendCheck));
}

//Send pushes to specific URL(character name and glass uid), called by sendclick
//Uses Python server code by Alex
function send(uid, what)
{
	if (nameList[uid].charactername!=null)
	{
		content = encodeURI(what);
		url = "http://ether.remap.ucla.edu/glass/data.py/insertContentFor?uid="+nameList[uid].charactername+"&content="+content;
		var xobj = new XMLHttpRequest();
    	xobj.overrideMimeType("application/json");
    	xobj.open('GET', url, false);
    	xobj.onreadystatechange = function () {
        	if (xobj.readyState == 4) {
            	//console.log("wrote new content "+xobj.responseText)
        	}
    	}
    	xobj.send(null);
    	
    	if (USEGLASSCHAT)
    	{
    		url = "http://ether.remap.ucla.edu/glass/graceplains/backup/web/data.py/insertContentFor?uid=" + nameList[uid].uid + "&content=" + content + "&chatroomID=" + nameList[uid].chatroomid;
    		//url = "http://ether.remap.ucla.edu/glass/data.py/insertContentFor?uid="+nameList[uid].uid+"&content="+content;
			xobj = new XMLHttpRequest();
    		xobj.overrideMimeType("application/json");
    		xobj.open('GET', url, false);
    		xobj.onreadystatechange = function () {
        		if (xobj.readyState == 4) {
            		//console.log("wrote new content "+xobj.responseText)
        		}
    		}
    		xobj.send(null);
    	}
    }
}

//lookup the index of certain name if it's existent in nameList global array
function indexOfNameList(name)
{
	for (var i = 0; i < characterNum; i++)
	{
		if (nameList[i].charactername == name)
		{
			return i;
		}
	}
	return -1;
}

function sortNameList()
{
	var i = 0;
	var j = 0;
	var temp;
	for (i = characterNum - 1; i > 0; i--)
	{
		for (j = 0; j < i; j++)
		{
			if (nameList[j].charactername > nameList[j + 1].charactername)
			{
				temp = nameList[j];
				nameList[j] = nameList[j + 1];
				nameList[j + 1] = temp;
			}
		}
	}
}

//Select(focus, change inputBox and previewDiv) the idx-th phrase in phraseBox
function selectPhrase(idx)
{
	var txtarea = document.getElementById('txtarea' + idx);
	if (txtarea != null)
	{
		//remember to change this line if the ' => ' that comes before user name is changed.
		var strSplit = txtarea.value.substring(4).split(" : ");
		var name = strSplit[0];
		var line = strSplit[1];
		
		var lineEscapeWhenToPush = (strSplit[1].split("\n"))[0];
		
		var num = indexOfNameList(name);
		
		if (num != -1)
		{
			document.getElementById('inputBox').value = lineEscapeWhenToPush;
			for (var i = 0; i < characterNum; i++)
			{
				if (i!=num)
				{
					document.getElementById('send' + i).checked = false;
					checkSend(document.getElementById('send' + i));
				}
			}
			if (document.getElementById('send' + num).disabled == false)
			{
				document.getElementById('send' + num).checked = true;
				checkSend(document.getElementById('send' + num));
			}
		}
		else
		{
			document.getElementById('inputBox').value = lineEscapeWhenToPush;
		}
		currentIdx = idx;
		inputPreview();
	}
}

//Move cursor to the end of inputBox
function moveCursorToEnd(el) {
    if (typeof el.selectionStart == "number") {
        el.selectionStart = el.selectionEnd = el.value.length;
    } else if (typeof el.createTextRange != "undefined") {
        el.focus();
        var range = el.createTextRange();
        range.collapse(false);
        range.select();
    }
}

function toggleWhenToPush()
{
	whenToPushEnabled = !whenToPushEnabled;
	loadPhrase();
}

//Phrase section onkeydown
function scriptKeydown(event, num)
{
	if (event.keyCode == KEYUP)
	{
		if (num - 1 < 0)
		{
			num = scriptNum;
		}
		txtarea = document.getElementById('txtarea' + (num - 1));
		if (txtarea!=null)
		{
			txtarea.focus();
			//selectScript function does not work well for now, 
			//should redesign the interface to these functions
			selectPhrase(num - 1);
		}
	}
	else if (event.keyCode == KEYDOWN)
	{
		if (num + 1 >= scriptNum)
		{
			num = -1;
		}
		txtarea = document.getElementById('txtarea' + (num + 1));
		if (txtarea!=null)
		{
			txtarea.focus();
			selectPhrase(num + 1);
		}
	}
	else if (event.keyCode == KEYENTER)
	{
		sendClick();
	}
	//may want to change how's operating
	else if (event.keyCode == KEYSPACE)
	{
		moveCursorToEnd(document.getElementById('inputBox'));
		//.focus();
		
		currentIdx = num;
		inputPreview();
	}
}

//Generate phrase section using callback from tabletop code
function showPhrase(data, tabletop) {
	var div = document.getElementById('scriptBoxDiv');
    var html = '';
    scriptNum = data.length;
    var height = 40;
    for(var i = 0; i < data.length; i++) {
    	height = 40 + data[i].pushedline.length / 8;
    	if (whenToPushEnabled)
    	{
    		html = html + "<textarea class=\"scriptTextarea boxClass\" id=\"txtarea" + i + "\" readonly \
    		onkeydown=\"scriptKeydown(event, " + i + ")\" style=\"height:" + height + "px\" ondblclick=\"selectPhrase(" + i + ")\"> => " 
    		+ data[i].character + " : " + data[i].pushedline + "\n[" + data[i].whentopush + "]</textarea>";
    	}
    	else
    	{
    		html = html + "<textarea class=\"scriptTextarea boxClass\" id=\"txtarea" + i + "\" readonly \
    		onkeydown=\"scriptKeydown(event, " + i + ")\" style=\"height:" + height + "px\" ondblclick=\"selectPhrase(" + i + ")\"> => " 
    		+ data[i].character + " : " + data[i].pushedline + "</textarea>";
    	}
	}
    div.innerHTML = html;
    
    //focus on previously selected phrase if there is one.
	var txtarea = document.getElementById('txtarea' + currentIdx);
	if (txtarea != null)
	{
		//console.log(currentIdx);
		txtarea.focus();
		selectPhrase(currentIdx);
	}
	
    //console.log(html);
}

function loadCharacterMap()
{
	loadSpreadSheetFromUri("https://docs.google.com/spreadsheet/pub?key=0AlW1a4Iy0WNvdEtaVFFPVkRWeTJmYzRHVkFzYkExSGc&output=html", storeCharacterMap);
}

function loadPhrasePlayerList()
{
	loadSpreadSheetFromUri("https://docs.google.com/spreadsheet/pub?key=0AlW1a4Iy0WNvdFRHLVpiQjdyTTVzemdhT3ZYdmdGbWc&output=html", generatePlayerList);
}

function storeCharacterMap(data, tabletop)
{
	//characterNum = 0;
	//nameList = new Array();
	var idx = -1;
	for (var i = 0; i < data.length; i++)
	{
		//jquery.inarray works when using nameList.charactername as input
		idx = indexOfNameList(data[i].charactername);
		if (idx == -1 && data[i].charactername != "Anyone")
		{
			nameList.push({uid: data[i].uid, charactername: data[i].charactername, chatroomid: data[i].chatroomid});
			characterNum ++;
		}
		else if (idx != -1)
		{
			nameList[idx].uid = data[i].uid;
			nameList[idx].chatroomid = data[i].chatroomid;
		}
	}
	sortNameList();
	//console.log(nameList);
	loadPhrasePlayerList();
}

//Loads phrase from published google spreadsheet, using code from tabletop
function loadPhrase()
{
	loadSpreadSheetFromUri("https://docs.google.com/spreadsheet/pub?key=0AlW1a4Iy0WNvdFRHLVpiQjdyTTVzemdhT3ZYdmdGbWc&output=html", showPhrase);
}

//Wrapper for tabletop init code
function loadSpreadSheetFromUri(public_spreadsheet_url, callbackFunc)
{
    Tabletop.init( { key: public_spreadsheet_url,
                     callback: callbackFunc,
                     simpleSheet: true } )
}

//callback for finishing loading character names in onLoad function
function generatePlayerList(data, tabletop)
{
	var html = 'Enabled: <br>';
	var sendHtml = 'Send to: <br>';
	
	if (!USEGLASSCHAT)
	{
		for (var i = 0; i < data.length; i++)
		{
			if (indexOfNameList(data[i].charactername) == -1 && data[i].charactername != "Anyone")
			{
				nameList.push({charactername: data[i].character, uid: 0});
				characterNum ++;
			}
		}
		sortNameList();
	}
	
	var checked = "";
	
	for (var i = 0; i < characterNum; i++)
	{
		html = html + "<input type=\"checkbox\" id=lock" + i + " onchange=\"checkLock(this)\" checked>" + nameList[i].charactername + "</input><br>";
		if (document.getElementById('send' + i) != null)
		{
			if (document.getElementById('send' + i).checked == true)
			{
				checked = "checked";
			}
			else
			{
				checked = "";
			}
		}
		else
		{
			checked = "";
		}
		sendHtml = sendHtml + "<input type=\"checkbox\" id=send" + i + " onchange=\"checkSend(this)\" " + checked + "></input><span id=\"character" + i + "\">" + (i + 1) + " : " + nameList[i].charactername + "</span><br>";
	}
	
	var div = document.getElementById('lockDiv');
	div.innerHTML = html;
	div = document.getElementById('recipientDiv');
	div.innerHTML = sendHtml;
	
	for (var i = 0; i < characterNum; i++)
	{
		if (document.getElementById('send' + i) != null)
		{
			checkSend(document.getElementById('send' + i));
		}
	}
	
	if (!mapLoaded)
	{
		loadPhrase();
	}
	mapLoaded = true;
}

function documentKeydown(event)
{
	if (event.keyCode > KEYZERO && event.keyCode <= KEYNINE && event.ctrlKey)
	{
		var objId = 'send' + (event.keyCode - KEYZERO - 1).toString();
		var obj = document.getElementById(objId);
		if (obj != null)
		{
			if (obj.disabled == false)
			{
				obj.checked = !obj.checked;
				checkSend(obj);
			}
		}
	}
}

//Page onload event, load character names from published google spreadsheet
function onLoad()
{
	if (USEGLASSCHAT)
	{
		//loadCharacterMap();
		setInterval ( "loadCharacterMap()", 1000 );
	}
	else
	{
		loadPhrasePlayerList();
	}
	
}

function loadHistory()
{

}