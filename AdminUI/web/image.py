import subprocess
import os
import commands
try:
    import json
except ImportError:
    import simplejson as json

import config as cfg

# this is a utility module for processing image (at moment system calls to imagemagick)

def histogram(id):
	imageFileName = cfg.imageFilePath+data.getFilenameFrom(id);
	result = commands.getoutput("convert "+imageFileName+" histogram:- | identify -format %c -")
	aName = "histogram"
	data.insertAnalysisWithID(id, aName, json.dumps(result));
	return data.getEntryFromID(id)
	
	#note for pattern generation:
	#json.loads(result) to reconsistute newline (or just explode on \\n)
	
def resize(imgName):
	imageFileName = cfg.imageFilePath+imgName;
	result = commands.getoutput("convert "+imageFileName+" -resize 640x "+imageFileName)
	return True
	
	#note for pattern generation:
	#json.loads(result) to reconsistute newline (or just explode on \\n)
 

