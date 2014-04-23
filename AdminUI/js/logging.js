var clientId = '324206727795';
var apiKey = 'AIzaSyAdjHPT5Pb7Nu56WJ_nlrMGOAgUAtKjiPM';
var scopes = 'https://www.googleapis.com/auth/plus.me';

function handleClientLoad() {
    // Step 2: Reference the API key
    gapi.client.setApiKey(apiKey);
    window.setTimeout(checkAuth,1);
}

function checkAuth() {
    gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: true}, handleAuthResult);
}

function handleAuthResult(authResult) {
	var authorizeButton = document.getElementById('authorize-button');
    if (authResult && !authResult.error) {
        authorizeButton.style.visibility = 'hidden';
        makeApiCall();
    } else {
        authorizeButton.style.visibility = '';
        authorizeButton.onclick = handleAuthClick;
    }
}

function handleAuthClick(event) {
	// Step 3: get authorization to use private data
    gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: false}, handleAuthResult);
    return false;
}

// Load the API and make an API call.  Display the results on the screen.
function makeApiCall() {
    // Step 4: Load the Google+ API
    gapi.client.load('plus', 'v1', function() {
	    // Step 5: Assemble the API request
    	var request = gapi.client.plus.people.get({
            'userId': 'me'
    	});
    	// Step 6: Execute the API request
    	request.execute(function(resp) {
        	var heading = document.createElement('h4');
        	var image = document.createElement('img');
        	image.src = resp.image.url;
        	heading.appendChild(image);
        	heading.appendChild(document.createTextNode(resp.displayName));

        	document.getElementById('content').appendChild(heading);
    	});
	});
}

function onLoad()
{
	console.log("Trying spreadsheet auth");
}

function log()
{
	
}