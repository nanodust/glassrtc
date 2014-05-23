Glass RTC - minor tweaks to the glass-ready code at http://www.webrtc.org/demo



we've gotten it to work 30 minutes before overheating, just by limiting screen brightness & resolution (lousy 320x180)  

far from ideal - but it worked well for our needs. 

regarding codecs - the codecs of the TI OMAP CPU (page 3 (http://www.ti.com/lit/ml/swpt034b/swpt034b.pdf), lower left) don't match webRTC's VP8... (but if they did match, android would handle acceleration automatically). 

and while the OMAP 4 bulletin still gives hope for glass optimization / VP8 -  '...with the industry’s broadest support for multimedia codecs available today as well as programmability to add support for future codecs' 

beyond what we were able to do in the time we had - but certainly a great target for someone with more resources... 
