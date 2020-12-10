/**
 *  Copyright 2017 Phil Mead
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  This is the arrival app that will turn on a bunch of lights when you tell it to or the "VT Arrival" routine is executed 
 *      (or in the future are in proximity to the house)
 *
 *  Author: Phil Mead
 */

definition (
    name: "VT Arrival ON",
    namespace: "meadforssell",
    author: "Phil Mead",
    description: "Turn  lights on when the SmartApp is tapped or the VT Arrival routine is executed and turn some of them off later.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section ("When any of these people arrive home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("When I touch the app, turn on ...") {
		input "InteriorSwitches", "capability.switch", multiple: true, required: false
        }
    section("When I touch the app, turn on and in 30 minutes turn off...") {
		input "ExteriorSwitches", "capability.switch", multiple: true, required: false
        }
	section( "Notifications" ) {
		input("recipients", "contact", title: "Send notifications to", required: false) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
	}
}

def routineChanged(evt) {
    // execute when routine detected
    if (evt.displayName == "VT Arrival") { 
         log.debug "VT arrival routine triggered the execution of VT Arrival On SmartApp"
         def message = "VT arrival routine triggered the execution of VT Arrival On SmartApp"
         send(message)
         TurnOnLights()
    } else {
        log.debug "We didnt find the VT Arrival routine"
    }
}

def installed()
{
	// subscribe(to app touch
	subscribe(app, appTouch)
	// subscribe to location changes
	subscribe(people, "presence", presence)
    // Subscribe to routines
    subscribe(location, "routineExecuted", routineChanged)	
}

def updated()
{
	unsubscribe()
	// subscribe(to app touch)
	subscribe(app, appTouch)
	// subscribe to location changes
	subscribe(people, "presence", presence)
    // Subscribe to routines
    subscribe(location, "routineExecuted", routineChanged)
}

def changedLocationMode(evt) {
	log.debug "changedLocationMode: $evt"
	// TurnOnLights()
}

def appTouch(evt) {
	log.debug "appTouch form: $evt"
    TurnOnLights()
}

def TurnOnLights() {
    log.debug "Turned on lights" 
	// Turn on all switches
	InteriorSwitches?.on()
	ExteriorSwitches?.on()
	// setup timer to call function to turn off lights 30 minutes later
	runIn(60*30, TurnOffExteriorLights)
}

def TurnOffExteriorLights() {
    log.debug "VT arrival timed turn off of exterior lights" 
    def message = "VT arrival timed turn off of exterior lights"
    send(message)
    ExteriorSwitches?.off()
    //InteriorSwitches?.off()
}

private send(msg) {
	if (location.contactBookEnabled) {
        log.debug("Sending notifications to: ${recipients?.size()}")
		sendNotificationToContacts(msg, recipients)
	}
	else  {
		if (sendPushMessage != "No") {
			log.debug("Sending push message")
			sendPush(msg)
		}
		if (phone) {
			log.debug("Sending text message")
			sendSms(phone, msg)
		}
	}
	log.debug msg
}

def presence(evt)
{
     // Get the sunset and sunrise times for weston VT 
	def weston = getSunriseAndSunset(zipCode: "05161")
    def setTime = weston.sunset
    def now = new Date()
	log.debug "sunset time: $setTime"
	
    for (person in people) {
		if (person.currentPresence == "present") {
			// Check if the sunset time is before the current time
            if(setTime.before(now)) {
			    log.debug "$person has arrived at VT home after sunset turning lights on"
			    def message = "$person has arrived at VT home after sunset turning lights on"
			    send(message)
			    // Run the lights on because they arrived after sunset
                TurnOnLights()			
			} else {
			    log.debug "$person has arrived at VT home before sunset no lights turned on"
			    def message = "$person has arrived at VT home before sunset no lights turned on"
			    send(message)
			}
		} else {
            // Check if the sunset time is before the current time
			if(setTime.before(now)) {
			    log.debug "$person has departed VT home after sunset"
			    def message = "$person has departed VT home after sunset"
			    send(message)
			} else {
			    log.debug "$person has departed VT home before sunset"
			    def message = "$person has departed VT home before sunset"
			    send(message)
			}
		}
	}
}