/**
 *  Sony TV Smartthings Integration, Currently testing on: KDL-55W829B

Working on KDL-55W829B,

 *
 *  
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
 *  Note: Within the Device on the SmartThings IDE the Device Network ID for Device instance must be hex of IP address and port
 *  in the form of 00000000:0000 (i.e. 10.0.1.220:80 is 0A0001DC:0050) - if you check the logs the smart device will log the 
 *  correct address that you can copy paste, once the IP address has been set. If this is not set you wont get updated on/off status
 *
 *  Please make sure the TV is on when you set up the device. 
 *
 *
 *  Wake on Lan button works when the TV is in ECO mode and goes to sleep even on wifi. however it takes a slight bit longer to boot
 *  wake on lan wont yet update the status to on very quickly and status polls happen about every 5 mins so it maybe 5 mins before
 *  the TV shows as on.
 *
 */
 
metadata {
  definition (name: "Sony Bravia TV", namespace: "steveAbratt", author: "Steve Bratt") {
    capability "Switch"
    capability "Polling"
    capability "Refresh"
    
    command "sendremotecommand"
    command "digital"
    command "picoff"
    
    command "tv_source"
    command "hdmi1"
    command "hdmi2"
    command "hdmi3"
    command "hdmi4"
    command "mute"
    command "netflix"
    command "WOLC"
    command "ipaddress"
    command "iphex"
    command "macaddress"
    command "home"
   
  }

  simulator {
    status "on": "on/off: 1"
    status "off": "on/off: 0"
  }

  tiles(scale:2) {
    standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
      state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
      state "on", label: 'ON', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
    }

    standardTile("digital", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Digital", action:"digital", icon:""
    }
    
    standardTile("picoff", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"PicOff", action:"picoff", icon:""
    }
    
    standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }
   
   standardTile("tv_source", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Source", action:"tv_source", icon:""
    }

   standardTile("WOLC", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Wake on Lan", action:"WOLC", icon:""
    }

   standardTile("hdmi1", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HDMI 1", action:"hdmi1", icon:""
    }

   standardTile("hdmi2", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HDMI 2", action:"hdmi2", icon:""
    }
    
   standardTile("hdmi3", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HDMI 3", action:"hdmi3", icon:""
    }
    
   standardTile("hdmi4", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HDMI 4", action:"hdmi4", icon:""
    }    

   standardTile("netflix", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Netflix", action:"netflix", icon:""
    }
    
   standardTile("home", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"HOME", action:"home", icon:""
    }

   standardTile("mute", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
      state "default", label:"Mute", action:"mute", icon:""
    }    
    
    main "switch"
    details(["switch", "digital", "picoff", "tv_source", "hdmi1", "hdmi2", "hdmi3", "hdmi4", "netflix", "home", "mute", "refresh", "WOLC"])
  }



  preferences {

		input name: "ipadd1", type: "number", range: "0..254", required: true, title: "Ip address part 1", displayDuringSetup: true
		input name: "ipadd2", type: "number", range: "0..254", required: true, title: "Ip address part 2", displayDuringSetup: true
		input name: "ipadd3", type: "number", range: "0..254", required: true, title: "Ip address part 3", displayDuringSetup: true
		input name: "ipadd4", type: "number", range: "0..254", required: true, title: "Ip address part 4", displayDuringSetup: true
		input name: "tv_port", type: "number", range: "0..9999", required: true, title: "Port Usually: 80", displayDuringSetup: true
		input name: "tv_psk", type: "text", title: "PSK Passphrase Set on your TV", description: "Enter passphrase", required: true, displayDuringSetup: true
	
	}
}



def updated(){
	log.debug( "Preferences Updated rebuilding IP Address, MAC address and Hex Network ID")
	state.tv_poll_count = 0
	ipaddress()
	iphex()
	refresh()
	
}

def ipaddress(){
	//Build an IP Address from the 4 input preferences
	log.debug( "building IP address from Preferences")
	state.tv_ip = "${ipadd1}" + "." + "${ipadd2}" + "." + "${ipadd3}" + "." + "${ipadd4}"
	log.debug( "IP Address State Value Set to = ${state.tv_ip}:${tv_port}" )
}
	
def iphex(){
	//create a Hex of the IP this will need to be set as the Network ID
	//TO DO Set the Network IP automatically or Show the user the Value to set manually
	log.debug( "Creating Hex of IP: ${state.tv_ip}")

	
	String tvipstring = state.tv_ip
	String tv_ip_hex = tvipstring.tokenize( '.' ).collect {
		String.format( '%02x', it.toInteger() )
	}.join()

	//set the global value of state.ip_hex
	state.ip_hex = tv_ip_hex
	log.debug ("IP Hex stored Globaly as '${state.ip_hex}'")

	log.debug( "Creating Hex of Port: ${tv_port}")


    String tvportstring = tv_port
    String tv_port_hex = tvportstring.tokenize( '.' ).collect {
    	String.format( '%04x', it.toInteger() )
    }.join()

    //Set the Global Value of state.port_hex
    state.port_hex = tv_port_hex
    log.debug ("Port Hex stored Globaly as '${state.port_hex}'")

    log.debug( "Please set your Device Network ID to the following to allow the TV state to be captured: ${state.ip_hex}:${state.port_hex}" )
    String netid = ("${state.ip_hex}:${state.port_hex}")
    log.debug( "Netid ${netid}" )
    //device.deviceNetworkId = ("${netid}")
}

def parse(description) {
  //log.debug ("Parsing '${description}'")
  def msg = parseLanMessage(description)
	//Set the Global Value of state.tv_mac
	//log.debug "${msg}"
    state.tv_mac = msg.mac
    log.debug ("MAC Address stored Globally as '${state.tv_mac}'")
    //log.debug "msg '${msg}'"
    //log.debug "msg.json '${msg.json?.id}'"
    
  
  if (msg.json?.id == 2) {
  	//Set the Global value of state.tv on or off
  	state.tv_poll_count = 0
    state.tv = (msg.json.result[0]?.status == "active") ? "on" : "off"
    sendEvent(name: "switch", value: state.tv)
    log.debug "TV is '${state.tv}'"
  }
}

private sendJsonRpcCommand(json) {

  def headers = [:]
  headers.put("HOST", "${state.tv_ip}:${tv_port}")
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
  
  if (state.tv == "polling"){
  	  WOLC()
  } else {
  	  def json = "{\"method\":\"setPowerStatus\",\"version\":\"1.0\",\"params\":[{\"status\":true}],\"id\":102}"
  	  def result = sendJsonRpcCommand(json)
  }
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

def poll() {
  //set state.tv to 0ff
  log.debug "poll count ${state.tv_poll_count}"
  state.tv = "polling"
  state.tv_poll_count = state.tv_poll_count++
  if (state.tv_poll_count > 1 ) {
  	  sendEvent(name: "switch", value: "off")
  }
  log.debug "Executing 'poll'"
  def json = "{\"id\":2,\"method\":\"getPowerStatus\",\"version\":\"1.0\",\"params\":[]}"
  def result = sendJsonRpcCommand(json)
}

def digital(){
	//Set Remote command to send
	state.remotecommand = "AAAAAgAAAJcAAAAyAw=="
	state.button = "Digital"
	sendremotecommand()
}

def picoff(){
	//Set Remote command to send
	state.remotecommand = "AAAAAQAAAAEAAAA+Aw=="
	state.button = "Pic Off"
	sendremotecommand()
}




def sendremotecommand(){
	log.debug "Sending Button: ${state.button} ${state.remotecommand}"
    def rawcmd = "${state.remotecommand}"
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     log.debug( "hubAction = ${sonycmd}" )
}


def tv_source() {
	log.debug "Executing Source"
    def rawcmd = "AAAAAQAAAAEAAAAlAw=="  //tv source
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}


def hdmi1() {
	log.debug "Selecting HDMI 1 as input"
    def rawcmd = "AAAAAgAAABoAAABaAw=="  //HDMI 1
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def hdmi2() {
	log.debug "Selecting HDMI 2 as input"
    def rawcmd = "AAAAAgAAABoAAABbAw=="  //HDMI 2
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def hdmi3() {
	log.debug "Selecting HDMI 3 as input"
    def rawcmd = "AAAAAgAAABoAAABcAw=="  //HDMI 3
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def hdmi4() {
	log.debug "Selecting HDMI 4 as input"
    def rawcmd = "AAAAAgAAABoAAABdAw=="  //HDMI 4
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def netflix() {
	log.debug "Launching Netflix"
    def rawcmd = "AAAAAgAAABoAAAB8Aw=="  //netflix
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def mute() {
	log.debug "mute"
    def rawcmd = "AAAAAQAAAAEAAAAUAw=="  //mute
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def home() {
	log.debug "home"
    def rawcmd = "AAAAAQAAAAEAAABgAw=="  //home
    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    ["IRCCCode":rawcmd],
            headers: [Host:"${state.tv_ip}:${tv_port}", 'X-Auth-PSK':"${tv_psk}"]
     )
     sendHubCommand(sonycmd)
     //log.debug( "hubAction = ${sonycmd}" )
}

def WOLC() {
    log.debug "Executing Wake on Lan"
	def result = new physicalgraph.device.HubAction (
  	  	"wake on lan ${state.tv_mac}", 
   		physicalgraph.device.Protocol.LAN,
   		null,
    	[secureCode: "111122223333"]
	)
	return result
}

/**{"name":"PowerOff","value":"AAAAAQAAAAEAAAAvAw=="},
{"name":"GGuide","value":"AAAAAQAAAAEAAAAOAw=="},
{"name":"EPG","value":"AAAAAgAAAKQAAABbAw=="},
{"name":"Favorites","value":"AAAAAgAAAHcAAAB2Aw=="},
{"name":"Display","value":"AAAAAQAAAAEAAAA6Aw=="},
{"name":"Home","value":"AAAAAQAAAAEAAABgAw=="},
{"name":"Options","value":"AAAAAgAAAJcAAAA2Aw=="},
{"name":"Return","value":"AAAAAgAAAJcAAAAjAw=="},
{"name":"Up","value":"AAAAAQAAAAEAAAB0Aw=="},
{"name":"Down","value":"AAAAAQAAAAEAAAB1Aw=="},
{"name":"Right","value":"AAAAAQAAAAEAAAAzAw=="},
{"name":"Left","value":"AAAAAQAAAAEAAAA0Aw=="},
{"name":"Confirm","value":"AAAAAQAAAAEAAABlAw=="},
{"name":"Red","value":"AAAAAgAAAJcAAAAlAw=="},
{"name":"Green","value":"AAAAAgAAAJcAAAAmAw=="},
{"name":"Yellow","value":"AAAAAgAAAJcAAAAnAw=="},
{"name":"Blue","value":"AAAAAgAAAJcAAAAkAw=="},
{"name":"Num1","value":"AAAAAQAAAAEAAAAAAw=="},
{"name":"Num2","value":"AAAAAQAAAAEAAAABAw=="},
{"name":"Num3","value":"AAAAAQAAAAEAAAACAw=="},
{"name":"Num4","value":"AAAAAQAAAAEAAAADAw=="},
{"name":"Num5","value":"AAAAAQAAAAEAAAAEAw=="},
{"name":"Num6","value":"AAAAAQAAAAEAAAAFAw=="},
{"name":"Num7","value":"AAAAAQAAAAEAAAAGAw=="},
{"name":"Num8","value":"AAAAAQAAAAEAAAAHAw=="},
{"name":"Num9","value":"AAAAAQAAAAEAAAAIAw=="},
{"name":"Num0","value":"AAAAAQAAAAEAAAAJAw=="},
{"name":"Num11","value":"AAAAAQAAAAEAAAAKAw=="},
{"name":"Num12","value":"AAAAAQAAAAEAAAALAw=="},
{"name":"VolumeUp","value":"AAAAAQAAAAEAAAASAw=="},
{"name":"VolumeDown","value":"AAAAAQAAAAEAAAATAw=="},
{"name":"Mute","value":"AAAAAQAAAAEAAAAUAw=="},
{"name":"ChannelUp","value":"AAAAAQAAAAEAAAAQAw=="},
{"name":"ChannelDown","value":"AAAAAQAAAAEAAAARAw=="},
{"name":"SubTitle","value":"AAAAAgAAAJcAAAAoAw=="},
{"name":"ClosedCaption","value":"AAAAAgAAAKQAAAAQAw=="},
{"name":"Enter","value":"AAAAAQAAAAEAAAALAw=="},
{"name":"DOT","value":"AAAAAgAAAJcAAAAdAw=="},
{"name":"Analog","value":"AAAAAgAAAHcAAAANAw=="},
{"name":"Teletext","value":"AAAAAQAAAAEAAAA/Aw=="},
{"name":"Exit","value":"AAAAAQAAAAEAAABjAw=="},
{"name":"Analog2","value":"AAAAAQAAAAEAAAA4Aw=="},
{"name":"*AD","value":"AAAAAgAAABoAAAA7Aw=="},
{"name":"Digital","value":"AAAAAgAAAJcAAAAyAw=="},
{"name":"Analog?","value":"AAAAAgAAAJcAAAAuAw=="},
{"name":"BS","value":"AAAAAgAAAJcAAAAsAw=="},
{"name":"CS","value":"AAAAAgAAAJcAAAArAw=="},
{"name":"BSCS","value":"AAAAAgAAAJcAAAAQAw=="},
{"name":"Ddata","value":"AAAAAgAAAJcAAAAVAw=="},
{"name":"PicOff","value":"AAAAAQAAAAEAAAA+Aw=="},
{"name":"Tv_Radio","value":"AAAAAgAAABoAAABXAw=="},
{"name":"Theater","value":"AAAAAgAAAHcAAABgAw=="},
{"name":"SEN","value":"AAAAAgAAABoAAAB9Aw=="},
{"name":"InternetWidgets","value":"AAAAAgAAABoAAAB6Aw=="},
{"name":"InternetVideo","value":"AAAAAgAAABoAAAB5Aw=="},
{"name":"Netflix","value":"AAAAAgAAABoAAAB8Aw=="},
{"name":"SceneSelect","value":"AAAAAgAAABoAAAB4Aw=="},
{"name":"Mode3D","value":"AAAAAgAAAHcAAABNAw=="},
{"name":"iManual","value":"AAAAAgAAABoAAAB7Aw=="},
{"name":"Audio","value":"AAAAAQAAAAEAAAAXAw=="},
{"name":"Wide","value":"AAAAAgAAAKQAAAA9Aw=="},
{"name":"Jump","value":"AAAAAQAAAAEAAAA7Aw=="},
{"name":"PAP","value":"AAAAAgAAAKQAAAB3Aw=="},
{"name":"MyEPG","value":"AAAAAgAAAHcAAABrAw=="},
{"name":"ProgramDescription","value":"AAAAAgAAAJcAAAAWAw=="},
{"name":"WriteChapter","value":"AAAAAgAAAHcAAABsAw=="},
{"name":"TrackID","value":"AAAAAgAAABoAAAB+Aw=="},
{"name":"TenKey","value":"AAAAAgAAAJcAAAAMAw=="},
{"name":"AppliCast","value":"AAAAAgAAABoAAABvAw=="},
{"name":"acTVila","value":"AAAAAgAAABoAAAByAw=="},
{"name":"DeleteVideo","value":"AAAAAgAAAHcAAAAfAw=="},
{"name":"PhotoFrame","value":"AAAAAgAAABoAAABVAw=="},
{"name":"TvPause","value":"AAAAAgAAABoAAABnAw=="},
{"name":"KeyPad","value":"AAAAAgAAABoAAAB1Aw=="},
{"name":"Media","value":"AAAAAgAAAJcAAAA4Aw=="},
{"name":"SyncMenu","value":"AAAAAgAAABoAAABYAw=="},
{"name":"Forward","value":"AAAAAgAAAJcAAAAcAw=="},
{"name":"Play","value":"AAAAAgAAAJcAAAAaAw=="},
{"name":"Rewind","value":"AAAAAgAAAJcAAAAbAw=="},
{"name":"Prev","value":"AAAAAgAAAJcAAAA8Aw=="},
{"name":"Stop","value":"AAAAAgAAAJcAAAAYAw=="},
{"name":"Next","value":"AAAAAgAAAJcAAAA9Aw=="},
{"name":"Rec","value":"AAAAAgAAAJcAAAAgAw=="},
{"name":"Pause","value":"AAAAAgAAAJcAAAAZAw=="},
{"name":"Eject","value":"AAAAAgAAAJcAAABIAw=="},
{"name":"FlashPlus","value":"AAAAAgAAAJcAAAB4Aw=="},
{"name":"FlashMinus","value":"AAAAAgAAAJcAAAB5Aw=="},
{"name":"TopMenu","value":"AAAAAgAAABoAAABgAw=="},
{"name":"PopUpMenu","value":"AAAAAgAAABoAAABhAw=="},
{"name":"RakurakuStart","value":"AAAAAgAAAHcAAABqAw=="},
{"name":"OneTouchTimeRec","value":"AAAAAgAAABoAAABkAw=="},
{"name":"OneTouchView","value":"AAAAAgAAABoAAABlAw=="},
{"name":"OneTouchRec","value":"AAAAAgAAABoAAABiAw=="},
{"name":"OneTouchStop","value":"AAAAAgAAABoAAABjAw=="},
{"name":"DUX","value":"AAAAAgAAABoAAABzAw=="},
{"name":"FootballMode","value":"AAAAAgAAABoAAAB2Aw=="},
{"name":"Social","value":"AAAAAgAAABoAAAB0Aw=="}]]}
*/


