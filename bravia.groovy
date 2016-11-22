/**
 *  Sony TV Smartthings Integration, Currently testing on: KDL-55W829B
 *
 *  Copyright 2016 Ed Anuff
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
 * Based on Ed Anuff and Jamie Yates Code
 *
 *  Based on Jamie Yates's example:
 *   https://gist.github.com/jamieyates79/fd49d23c1dac1add951ec8ba5f0ff8ae
 *
 *  Note: Device Network ID for Device instance must be hex of IP address and port
 *  in the form of 00000000:0000 (i.e. 10.0.1.220:80 is 0A0001DC:0050)
 *
 *  JSON-RPC Methods From:
 *
 *  curl -X "POST" "http://10.0.1.220/sony/system" \
 *  -H "X-Auth-PSK: 1111" \
 *  -H "Content-Type: application/json" \
 *  -d $'{
 *  "id": 4649,
 *  "method": "getMethodTypes",
 *  "version": "1.0",
 *  "params": [
 *    "1.0"
 *  ]
 *  }'
 *
 */
 
metadata {
  definition (name: "Sony Bravia TV", namespace: "steveAbratt", author: "Steve Bratt") {
    capability "Switch"
    capability "Polling"
    capability "Refresh"
    
    command "input"
    command "WOLC"
  }

  simulator {
    status "on": "on/off: 1"
    status "off": "on/off: 0"
  }

  tiles {
    standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
      state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
      state "on", label: 'ON', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
    }

    standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }
   
   standardTile("input", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Input", action:"input", icon:"st.secondary.refresh"
    }

   standardTile("WOLC", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Wake", action:"WOLC", icon:"st.secondary.refresh"
    }

    main "switch"
    details(["switch", "refresh", "input", "WOLC"])
  }
}


preferences {

input name: "ipAdd1", type: "number", range: "0..254", defaultValue: "192", required: false, title: "Ip address part 1"
input name: "ipAdd2", type: "number", range: "0..254", defaultValue: "168", required: false, title: "Ip address part 2"
input name: "ipAdd3", type: "number", range: "0..254", defaultValue: "0", required: false, title: "Ip address part 3"
input name: "ipAdd4", type: "number", range: "0..254", defaultValue: "12", required: false, title: "Ip address part 4"
input name: "tv_psk", type: "text", defaultValue: "1111", title: "Passphrase", description: "Enter passphrase", required: false
			
}

def ipAddr1 = ("192")
def ipAddr2 = ("168")
def ipAddr3 = ("0")
def ipAddr4 = ("12")
def tv_psk = ("1111")

log.debug("${ipAddr1} ${ipAddr2} ${ipAddr3} ${ipAddr4}") 

def tv_ip = (ipAddr1 + "." + ipAddr2 + "." + ipAddr3 + "." + ipAddr4)
def port = "80"

String ip_hex = tv_ip.tokenize( '.' ).collect {
  String.format( '%02x', it.toInteger() )
}.join()

String port_hex = port.tokenize( '.' ).collect {
  String.format( '%04x', it.toInteger() )
}.join()

log.debug( "Device IP:Port = ${tv_ip}:${port}" )
log.debug( "Device IP:Port Hex = ${ip_hex}:${port_hex}" )
log.debug( "Passphrase ${tv_psk}")


def parse(description) {
  log.debug "Parsing '${description}'"
  def msg = parseLanMessage(description)
	log.debug "msg '${msg}'"
    log.debug "msg.json '${msg.json?.id}'"
    log.debug "MAC Address '${msg.mac}'"
    //log.debug "msg.json.status '${msg.json.result[0]?.status}'"
  if (msg.json?.id == 2) {
    def status = (msg.json.result[0]?.status == "active") ? "on" : "off"
    sendEvent(name: "switch", value: status)
    log.debug "TV is '${status}'"
  }
}

private sendJsonRpcCommand(json) {

  // TV IP and Pre-Shared Key
  //def tv_ip = "192.168.0.12"
  //def tv_psk = "1111"

  def headers = [:]
  headers.put("HOST", "${tv_ip}:80")
  headers.put("Content-Type", "application/json")
  headers.put("X-Auth-PSK", "${tv_psk}")

  def result = new physicalgraph.device.HubAction(
    method: 'POST',
    path: '/sony/system',
    body: json,
    headers: headers
  )

  result
}

def installed() {
  log.debug "Executing 'installed'"

  poll()
}

def on() {
  log.debug "Executing 'on'"

  def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.0\",\"params\":[{\"status\":true}],\"id\":102}"
  def result = sendJsonRpcCommand(json)
}

def off() {
  log.debug "Executing 'off'"

  def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.0\",\"params\":[{\"status\":false}],\"id\":102}"
  def result = sendJsonRpcCommand(json)
}

def refresh() {
  log.debug "Executing 'refresh'"

  poll()
}

def input() {

	log.debug "Executing input"

    //def rawcmd = "AAAAAQAAAAEAAAAvAw=="  //Wake On LAN
    //def rawcmd = "AAAAAgAAABoAAAB8Aw=="  //netflix
    def rawcmd = "AAAAAQAAAAEAAAAlAw=="  //input
    //def rawcmd = "AAAAAgAAABoAAABbAw=="  //HDMi2
    //def rawcmd = "AAAAAQAAAAEAAAAVAw=="  //TV Power
    //def ip = "192.168.0.12" //TV IP
    //def port = "80"          //TV's Port
        //setDeviceNetworkId(ip,port)
        log.debug( "Device IP:Port = ${tv_ip}:${port}" )

        def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${tv_ip}:${port}", 'X-Auth-PSK':"${tv_psk}"]
        )
        sendHubCommand(sonycmd)

        log.debug( "hubAction = ${sonycmd}" )
}

def WOLC() {
    
	def result = new physicalgraph.device.HubAction (
  	  	"wake on lan ACD1B83DDA7B", 
   		physicalgraph.device.Protocol.LAN,
   		null,
    	[secureCode: "111122223333"]
	)
	return result
    
}


def poll() {
  log.debug "Executing 'poll'"
  def json = "{\"id\":2,\"method\":\"getPowerStatus\",\"version\":\"1.0\",\"params\":[]}"
  def result = sendJsonRpcCommand(json)
}


