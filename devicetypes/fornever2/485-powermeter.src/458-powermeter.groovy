/**
 *  485-light
 *
 *  Copyright 2020 fornever2@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
metadata {
	definition (name: "485-power", namespace: "fornever2", author: "rbitts@gmail.com", cstHandler: true, ocfDeviceType: "x.com.st.d.energymeter") {
        capability "Power Meter"
      	capability "Sensor"
        capability "Health Check"
        capability "Refresh"

        fingerprint deviceId: "0x2101", inClusters: " 0x70,0x31,0x72,0x86,0x32,0x80,0x85,0x60"	
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

def installed() {
	log.debug "installed()"
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute

}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"
    
    log.debug "apiServerUrl: ${apiServerUrl("/my/path")}"
    
    // TODO: handle 'refresh' command
    try{
		log.debug "Try to get data from ${state.address}"
        def options = [
            "method": "GET",
            "path": state.path,
            "headers": [
                "HOST": state.address,
                "Content-Type": "application/json"
            ]
        ]
        log.debug "options : ${options}"
        def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: refreshCallback])
        sendHubCommand(myhubAction)
    }catch(e){
    	log.error "Error!!! ${e}"
    }
    
}

def refreshCallback(physicalgraph.device.HubResponse hubResponse) {
	log.debug "refreshCallback() - hubResponse : ${hubResponse}"
	def msg, status, json
    try {
        msg = parseLanMessage(hubResponse.description)
        log.debug msg.json
        updateProperty("switch", msg.json.property.switch)
	} catch (e) {
        log.error("Exception caught while parsing data: "+e)
    }
}
////////////////////////////////////////////////////////////////

def init(data) {
	log.debug "init >> ${data}"
}

def setUrl(String url){
	log.debug "URL >> ${url}"
    state.address = url
}

def setPath(String path){
	log.debug "path >> ${path}"
    state.path = path
}

def updateProperty(propertyName, propertyValue) {
	log.debug "updateProperty - ${propertyName} : ${propertyValue}"
    sendEvent(name:propertyName, value:propertyValue)
}

////////////////////////////////////////////////////////////////

def setProperty(String name, String value) {
	try{    
        def options = [
            "method": "PUT",
            "path": state.path + "/" + name + "/" + value,
            "headers": [
                "HOST": state.address,
                "Content-Type": "application/json"
            ]
        ]
        log.debug "setProperty - send to 485server : ${options}"
        def myhubAction = new physicalgraph.device.HubAction(options, null, [callback: setPropertyCallback])
        sendHubCommand(myhubAction)
    }catch(e){
    	log.error "Error!!! ${e}"
    }
}

def setPropertyCallback(physicalgraph.device.HubResponse hubResponse) {
	log.debug "setPropertyCallback() - hubResponse : ${hubResponse}"
	def msg, status, json
    try {
        msg = parseLanMessage(hubResponse.description)
        log.debug msg.json
	} catch (e) {
        log.error("Exception caught while parsing data: "+e);
    }
}




/////////////////////////
// Not used below

// gets the address of the Hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}