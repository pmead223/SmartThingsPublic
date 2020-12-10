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
/**
 Example URL calls
 From the deplotyment
 9728f88f-c2f6-46be-8fc8-8e5dc0433b4c
 https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/301d169b-4c30-4572-8e62-02dbd8424eaa/
 Calls:
curl -H "Authorization: Bearer 9728f88f-c2f6-46be-8fc8-8e5dc0433b4c" "https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/301d169b-4c30-4572-8e62-02dbd8424eaa/GetTemps"
curl -H "Authorization: Bearer 9728f88f-c2f6-46be-8fc8-8e5dc0433b4c" "https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/301d169b-4c30-4572-8e62-02dbd8424eaa/SetTemp/56"
{"error":true,"type":"SmartAppException","message":"Method Not Allowed"}
 */
 

definition (
    name: "Thermostat_control",
    namespace: "meadforssell",
    author: "Phil Mead",
    description: "Control thermostat - on and off setback to fixed temps, also contol for weather.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section ("Thermostats to control") {
		input (name:"Cthermostat", type: "capability.thermostat", title: "Select thermostat", required: true, multiple: true)
	}
	section("When I touch the app, set heat to 69 on ...") {
		input (name:"thermoup", type: "capability.thermostat", title: "Select thermostat", required: false, multiple: true)
        }
	section( "Notifications" ) {
		input("recipients", "contact", title: "Send notifications to", required: false) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
	}
}

def installed()
{
	// subscribe(to app touch)
	subscribe(app, appTouch)
}

def updated()
{
	unsubscribe()
	// subscribe(to app touch)
	subscribe(app, appTouch)
}


mappings {
  path("/GetTemps") {
    action: [
      GET: "CurrentTemp"
    ]
  }
  path("/SetTemp/:temp") {
    action: [
      GET: "SetTempTo"
    ]
  }
  path("/TurnOffSetBack") {
    action: [
      GET: "OffSetBack"
    ]
  }
    path("/TurnOnSetBack") {
    action: [
      GET: "OnSetBack"
    ]
  }
}

def appTouch(evt) {
	log.debug "appTouch form: $evt"
     OffSetBack()
}


def CurrentTemp() {
    // returns a list like
    // [[name: "Basement thermo", currenttemp: 72, setpoint: 52], [name: "Hall thermo", currenttemp: 72, setpoint: 52]]
    def resp = []
    Cthermostat.each {
        def currentTemp     = it.currentValue("temperature")
        def coolingSetpoint = it.currentValue("coolingSetpoint")
        def heatingSetpoint = it.currentValue("heatingSetpoint")
        def thermostatMode  = it.currentValue("thermostatMode")
        log.debug "thermo mode: " + thermostatMode
        resp << [name: it.displayName, currenttemp: it.currentValue("temperature"), setpoint: it.currentValue("heatingSetpoint"), ThermostatMode: $thermostatMode ]
    }
    log.debug "Request for Thermostat state recieved"
    return resp
}

def SetTempTo() {
    // use the built-in request object to get the command parameter
    log.debug "in SetTempTo" 
    def temptoset = params.temp
    log.debug temptoset 
    Cthermostat.setHeatingSetpoint(temptoset)
    log.debug "SetTempTo called, we are setting the temperature to " + temptoset
}


def OffSetBack() {
    // Set the temp to 69 to warm up the house
    Cthermostat.setHeatingSetpoint(69)
    log.debug "OffSetBack called, we are setting the temperature to 69" 
}


def appTouch() {
    // Set the temp to 69 to warm up the house
    thermoup.setHeatingSetpoint(69)
    log.debug "appTouch called, we are setting the temperature to 69" 
}

def OnSetBack() {
    // Set the temps back to 52 universally....
    Cthermostat.setHeatingSetpoint(52)
    log.debug "OnSetBack called, we are setting the temperature to 52 for ALL thermostats" 

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