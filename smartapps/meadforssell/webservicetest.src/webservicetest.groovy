/**
 *  Webservicetest
 *
 *  Copyright 2019 Phil Mead
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
 */
/**
examples:

this from deployment:
https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/f0524d51-cd4e-47f5-a27f-ba7423f538ef
9b728d3d-8e54-4164-a633-26be3aac126c

curl -H "Authorization: Bearer 9b728d3d-8e54-4164-a633-26be3aac126c" "https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/f0524d51-cd4e-47f5-a27f-ba7423f538ef/switches"
curl -H "Authorization: Bearer 9b728d3d-8e54-4164-a633-26be3aac126c" -X PUT "https://graph-na04-useast2.api.smartthings.com/api/smartapps/installations/f0524d51-cd4e-47f5-a27f-ba7423f538ef/switches/on"

*/
definition(
    name: "Webservicetest",
    namespace: "meadforssell",
    author: "Phil Mead",
    description: "web services tutorial",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "web services tutorial ", displayLink: "http://localhost:4567"])


preferences {
  section ("Allow external service to control these things...") {
    input "switches", "capability.switch", multiple: true, required: true
  }
}

mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/switches/:command") {
    action: [
      PUT: "updateSwitches"
    ]
  }
}

// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSwitches() {

    def resp = []
    switches.each {
        resp << [name: it.displayName, value: it.currentValue("switch")]
    }
    log.debug "Request for switch state recieved"
    return resp
}

void updateSwitches() {
    // use the built-in request object to get the command parameter
    def command = params.command

    // all switches have the comand
    // execute the command on all switches
    // (note we can do this on the array - the command will be invoked on every element
    switch(command) {
        case "on":
            switches.on()
            log.debug "we are turning on the switches"
            break
        case "off":
            switches.off()
            log.debug "we are turning off the switches"
            break
        default:
            log.debug "we are not sure what we are doing on the switches"
            httpError(400, "$command is not a valid command for all switches specified")
    }

}
def installed() {}

def updated() {}