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
 *  based on :Medicine Reminder
 *
 *  Author: SmartThings
 */

definition(
    name: "Raspberry pi On Check",
    namespace: "meadforssell",
    author: "meadforssell",
    description: "Set up a schedule to check if rapsberry pis switches are on and if not turn them on",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text_contact@2x.png"
)

preferences {

	section("Check Pi's at:"){
		input "time1", "time", title: "Time 1"
		input "time2", "time", title: "Time 2", required: false
		input "time3", "time", title: "Time 3", required: false
		input "time4", "time", title: "Time 4", required: false
	}
	section("text when one is found to be off..."){
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPush", "enum", title: "Push Notification", required: false, options: ["Yes", "No"]
            input "phone", "phone", title: "Phone Number", required: false
        }
	}
	section("Switches") {
		input name: "switches", type: "capability.switch", multiple: true
	}
}

def installed()
{
	initialize()
}

def updated()
{
	unschedule()
	initialize()
}

def initialize() {
	[time1, time2, time3, time4].eachWithIndex {time, index ->
		if (time != null) {
			def endTime = new Date(timeToday(time, location?.timeZone).time)
			log.debug "Scheduling check at $endTime"
			//runDaily(endTime, "scheduleCheck${index}")
			switch (index) {
				case 0:
					schedule(endTime, scheduleCheck0)
					break
				case 1:
					schedule(endTime, scheduleCheck1)
					break
				case 2:
					schedule(endTime, scheduleCheck2)
					break
				case 3:
					schedule(endTime, scheduleCheck3)
					break
			}
		}
	}
}

def scheduleCheck0() { scheduleCheck() }
def scheduleCheck1() { scheduleCheck() }
def scheduleCheck2() { scheduleCheck() }
def scheduleCheck3() { scheduleCheck() }

def scheduleCheck()
{
	log.debug "scheduleCheck"
    def logmessage = 'Switch check: '
    def pushmessage = 'Switch check: '
    def oneswitchoff = 0
    switches.each {aswitch ->
        def aswitchname = aswitch.label
		def onstate = aswitch.currentValue("switch")
        log.info "values $aswitchname , $onstate "
        if (onstate == 'on') {
            def message1 =  "$logmessage  $aswitchname was on..... "
            logmessage = "$message1"
            //log.info "We found switch $aswitchname on. We will leave it that way"
            //aswitch.on()
        } else {
		    def message1 =  "$pushmessage  $aswitchname was OFF..... "
            pushmessage = "$message1"
            message1 =  "$logmessage  $aswitchname was OFF..... "
            logmessage = "$message1"
            oneswitchoff = 1
            //log.info "We found switch $aswitchname  off, We will turn it on"
            //aswitch.on()
		}
    }
    log.info " message - $logmessage"
    if (oneswitchoff == 1) {
        send(pushmessage)
    }
}

private send(msg) {
	if (location.contactBookEnabled) {
        log.debug("Sending notifications to: ${recipients?.size()}")
		sendNotificationToContacts(msg, recipients)
	}
	else  {
		if (sendPush != "No") {
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

