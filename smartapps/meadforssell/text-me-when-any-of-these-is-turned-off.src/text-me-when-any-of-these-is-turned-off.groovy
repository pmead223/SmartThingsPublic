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
 *  based on Text Me When It Opens
 *
 *  Author: SmartThings
 */
definition(
    name: "Text Me When Any of these is turned off",
    namespace: "Meadforssell",
    author: "Meadforssell",
    description: "Get a text message sent to your phone when a switch that should normally be on goes off",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("When the switch turns off...") {
		input name: "switches", type: "capability.switch", multiple: true
	}
	section("Text when one turned off..."){
        input("recipients", "contact", title: "Send notifications to") {
            input "Pushsend", "enum", title: "Push Notification", required: false, options: ["Yes", "No"]
            input "phone", "phone", title: "Phone Number", required: false
        }
	}
}

def installed()
{
	subscribe(switches, "switch", switchOffHandler)
}

def updated()
{
	unsubscribe()
	subscribe(switches, "switch", switchOffHandler)
}

def switchOffHandler(evt) {
    //log.debug "$contact1 was opened, sending text"
    // get the event name, e.g., "switch"
    //log.debug "This event name is $evt.name"
    // get the value of this event, e.g., "on" or "off"
    // log.debug "The value of this event is $evt.value"
    // get the Date this event happened at
    //log.debug "This event happened at $evt.date"
    // did the value of this event change from its previous state?
    //log.debug "The value of this event is different from its previous value: $evt.isStateChange()"   
    // def SwitchName = "$evt.description"
    //                        "{$evt.getDevice()}"    "$evt.description"      "$evt.descriptionText"
    // def message = "Switch -  $eventdisplayname -> $SwitchName >  $eventnameat "
    //               message - Switch - {getDevice()} -> switch:on > null
    //                        "$evt.deviceId"    "$evt.name"      "$evt.displayName"
    // def message = "Switch -  $eventdisplayname -> $SwitchName >  $eventnameat "
    //               message - Switch - 3ffe4a20-b24c-4b58-9831-1f6ba190a13f -> switch > null    def CurrentSwitchState = "$evt.value"
    // log.trace "value $evt.value : $evt : Setting $settings displayname $evt.displayName"
    def CurrentSwitchdate = "$evt.date"
    def EventDisplayName = "$evt.displayName"
    def message = 'wtf dude'
    log.info "$CurrentSwitchState"
    if (CurrentSwitchState == "off") {
         message = "Problem - switch $EventDisplayName has been turned $CurrentSwitchState at $CurrentSwitchdate."
         send(message)
    } else {
         message = "Normal - switch $EventDisplayName has been turned $CurrentSwitchState at $CurrentSwitchdate."
    }
    log.info "$message"
}

private send(msg) {
	if (location.contactBookEnabled) {
        log.debug("Sending notifications to: ${recipients?.size()}")
		sendNotificationToContacts(msg, recipients)
	}
	else  {
		if (Pushsend != "No") {
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
