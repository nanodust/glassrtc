import re
import sys
import pymongo
from pymongo import Connection
from pymongo.errors import CollectionInvalid
import datetime
import time
from bson import Binary, Code
from bson.json_util import dumps

import config as cfg

connection = Connection('localhost', cfg.dbPort)
#db = connection.test
#collection = db.test

db = connection[cfg.colName]
collection = db[cfg.colName]

def insertMediaFor(uid, media):
	collection.update({"uid":uid},{'$set':{"content":media}}, upsert=True, multi=True)
	
def getMediaFor(uid):
	result = collection.find_one({"uid":uid})
	if not result:
		collection.update({"uid":uid},{'$set':{"content":". . ."}}, upsert=True, multi=True)
	return collection.find_one({"uid":uid})
	
	
	
def clearAll():
	collection.remove()
	
	
def getAllDown():
	#docs = collection.find({},{"uid": 1})
	docs = collection.find({},{"uid": 1},exhaust=True)
	#total = myCursor.forEach(printjson)
	total = []
	for doc in docs:
		total += doc
	return total
	
def getAllUID():

	return list(collection.find({},{"uid": 1, "_id":False},exhaust=True))