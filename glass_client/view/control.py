import os
import database as db
import json
from bson import Binary, Code
from bson.json_util import dumps
import image
import config as cfg
import sys
sys.path.append("/var/www/glass/view/")

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
	
	
### new for view:


def getAllMedia():
	return dumps(db.getAllMedia())
	
def upload(req):

	fileitem = req.form['file']
	accepted = ['mp4','mpg','mpeg','mov'] 
	
	if fileitem.filename:
		extension = os.path.splitext(fileitem.filename)[1][1:]
		
	# ensure it's an image
	if (extension.lower() in accepted):
		# strip leading path from file name to avoid directory traversal attacks
		# also prepend DB ID so filenames are unique
		fname = os.path.basename(fileitem.filename)
		
		# build absolute path to files directory
		dir_path = os.path.join(os.path.dirname(req.filename), 'files')
		open(os.path.join(dir_path, fname), 'wb').write(fileitem.file.read())
		#obj = image.transcode(fname)
		message = '<html><meta http-equiv="refresh" content="14"; url=https://remapglass2013.appspot.com/">'
		message += 'The file "%s" was uploaded and converted successfully, returning . . .' % fname
		#essage += str(obj);
		message += '</html>'
		
		
		
		# add file to database
		
		db.insertMedia(fname, fileitem.filename,cfg.imageWebPath+fname)

		
	else:
		message = 'No file was uploaded - ensure its less than 8MB.'

	return message
	
	
# def getAllMediaFromFilesystem()


# def 