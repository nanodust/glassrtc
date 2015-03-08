#glasschat

the goal was a simple drag & drop 'distrbuted teleprompter' / simstim type app for google glass. 

succeeded.

http://newsroom.ucla.edu/stories/an-eye-on-the-future-of-storytelling


#approach

basic MVC

M) server holds overall state in a database
C) control pushes state change to database
V) clients draw view from polling state changes from database

This particular instance uses mongodb + mod python + apache

so to run, 
configure apache for mod-python
install & start mongodb
and drop this folder into the WebRoot. 


but could easily use mysql - just change 'database.py' to perform same methods & json conversion / return same results to data.py 

note each 'row/collection' is simply <deviceUID, HTML_Content>, that's all there is for a schema. 

###client


install android apk on glass

you can also 'fake' a glass device by loading url:

http://<server_url>/glass/index.html?uid=whatever1234

replace UID with anything, open as many windows as you like. 

convenient for debugging from desktop !

###server

open http://<server_url>/glass/control.html in a browser

enter text, select device, hit 'send'. 

also, upload image, copy text into 'text', then hit 'send'. 

optionally, hit 'refresh' to preview. 

also note, very hurried improvement:

open http://<server_url>/glass/control2.html - developed to make it easier to feed lines from a script in a timely manner. 

copy in any text, select & hit spacebar to send the text to the selected UUID/device



###notes

initial commit, the glass native apk is complete. 
test/image upload & per-client polling works. 

video won't work (yet) until html5 video works... (remember we're trying to do *live* streaming, not simply play content)

and actually, we ended up using webRTC as it was already working via google sample code.

tho note it seems some people had since gotten html5 video to work, after extensive hacking. maybe ? 
http://stackoverflow.com/questions/3815090/webview-and-html5-video
