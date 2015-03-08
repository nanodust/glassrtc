import os
import database as db
import json
from bson import Binary, Code
from bson.json_util import dumps

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
	
