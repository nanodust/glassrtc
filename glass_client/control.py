import os
import database as db
import json
from bson import Binary, Code
from bson.json_util import dumps
import image
import config as cfg
import sys
sys.path.append("/var/www/glass/")

def getContent(uid):
     return 'data = { "content": "this is dynamic content '+uid+' <br> <img src=\'http://sociorocketnewsen.files.wordpress.com/2013/07/nyan-gif-original.gif\' />"}'
     
def getContentFor(uid):
	obj = db.getMediaFor(uid)
	return dumps(obj)
	
def insertContentFor(uid, content):
	db.insertMediaFor(uid, content)
	return 'data = { "content": "ok"}'
	

def testInsertMediaFor(uid):
	content = "this is db content 0 "+uid+" <br> <img src=\'http://sociorocketnewsen.files.wordpress.com/2013/07/nyan-gif-original.gif\' />"
	db.insertMediaFor(uid, content)
	return "ok"
	
def clearAll():
	db.clearAll()
	return "ok"
	
def getAllUID():
	return dumps(db.getAllUID())
	
def upload(req):

	fileitem = req.form['file']
	accepted = ['jpg', 'png', 'gif'] 
	
	if fileitem.filename:
		extension = os.path.splitext(fileitem.filename)[1][1:]
		
	# ensure it's an image… 
	if (extension.lower() in accepted):
		# strip leading path from file name to avoid directory traversal attacks
		# also prepend DB ID so filenames are unique
		fname = os.path.basename(fileitem.filename)
		
		# build absolute path to files directory
		dir_path = os.path.join(os.path.dirname(req.filename), 'files')
		open(os.path.join(dir_path, fname), 'wb').write(fileitem.file.read())
		obj = image.resize(fname)
		message = '<html>The file "%s" was uploaded successfully' % fname
		message += '<br/>Please copy the following, and paste in the text box after hitting <back> button'
		message += '<textarea rows="4" cols="50"><img src="'+cfg.imageWebPath+fname+'"/></textarea>'
		message += '</html>'
	else:
		message = 'No file was uploaded - only jpeg, gif, or png accepted'
	
	return message