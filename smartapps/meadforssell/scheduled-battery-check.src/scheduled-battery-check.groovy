/**
 *  Copyright 2015 SmartThings
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
 *  Scheduled Mode Change - Presence Optional
 *
 *  Author: SmartThings

 *
 */

definition(
    name: "Scheduled battery check",
    namespace: "meadforssell",
    author: "Phil Mead",
    description: "Check smoke alarm battery once a day and message when it gets below a set threshold",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {
	section("At this time every day") {
		input "time", "time", title: "Time of Day"
	}
	section("Check these smokes to this mode") {
		input name: "SmokeDectors", capabilities:"Smoke Detector", multiple: true
	}
	section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phoneNumber", "phone", title: "Send a text message?", required: false
        }
	}
}

def installed() {
	initialize()
}

def updated() {
	unschedule()
	initialize()
}

def initialize() {
	schedule(time, CheckBatt)
}

def CheckBatt() {
	log.debug "Checking Battery Level on Smokes"
    SmokeDectors.each {asmoke ->
        def smokename = asmoke.label
		def battlevel = asmoke.currentValue("battery")
        log.info "values $smokename , $battlevel "
        if (battlevel >= '.75' ) {
            def message1 =  "$logmessage  $smokename battery above 75%.. "
            logmessage = "$message1"
            //log.info "We found switch $smokename on. We will leave it that way"
            //aswitch.on()
        } else {
		    def message1 =  "$pushmessage  $smokename battery below 75%..... "
            pushmessage = "$message1"
            message1 =  "$logmessage  $smokename battery below 75%..... "
            logmessage = "$message1"
            oneswitchoff = 1
            //log.info "We found switch $aswitchname  off, We will turn it on"
            //aswitch.on()
		}}
}
private send(msg) {

    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage == "Yes") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phoneNumber) {
            log.debug("sending text message")
            sendSms(phoneNumber, msg)
        }
    }

	log.debug msg
}

private getLabel() {
	app.label ?: "SmartThings"
}