package bankers.fisa.controller;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import bankers.fisa.connecter.HttpsConnecter;

@RestController
public class MainController {

	private static final String ACCOUNT = "administrator@vsphere.local:VMware1!";
	private static final String baseURL = "https://192.168.0.150";
	
	private static String token = new String();
	private static Date tokenDate = null;
	
	private static String getToken() {
		
		if(tokenDate == null) {
			tokenDate = new Date();
		}else {
			Date current = new Date();
			if((tokenDate.getTime() / 1000) < (current.getTime() / 1000) + (3600)) {
				return token;
			}
		}
			
		
		ArrayList<String> header = new ArrayList<String>();
		
	    byte[] authEncBytes = Base64.encodeBase64(ACCOUNT.getBytes(), true);
        String authStringEnc = new String(authEncBytes);
        authStringEnc = authStringEnc.substring(0, authStringEnc.length() - 2);
        header.add("Authorization:Basic " + authStringEnc);
		try {
			token = HttpsConnecter.connect(baseURL + "/api/session", "POST", header);
			token = token.substring(1, token.length() - 1);
		}catch (Exception e) {

		}
		System.out.println(token);
		return token;
	}
	
	@PostMapping("getvmaddress")
	public String getvmaddress(@RequestParam("vmId") String vmId) {
		ArrayList<String> header = new ArrayList<String>();
        header.add("vmware-api-session-id:" + getToken());

        String result = new String();
        try {
        	result = HttpsConnecter.connect(baseURL + "/api/vcenter/vm/vm-" + vmId + "/guest/networking/interfaces", "GET", header);
        	result = result.split(":")[9].split(",")[0];
        	result = result.substring(1, result.length()-1);
            return result;
        }catch (Exception e) {
        	return "preparing";
        }
	}
	
	@PostMapping("vmdelete")
	public void vmdelete(@RequestParam("vmId") String vmId) {
		ArrayList<String> header = new ArrayList<String>();
        header.add("vmware-api-session-id:" + getToken());
        
        vmpower(vmId, "OFF");
        try {
        	HttpsConnecter.connect(baseURL + "/api/vcenter/vm/vm-" + vmId, "DELETE", header);
        }catch (Exception e) {

		}
	}
	
	@PostMapping("vmpower")
	public void vmpower(
			@RequestParam("vmId") String vmId,
			@RequestParam("state") String state) {
		
		ArrayList<String> header = new ArrayList<String>();
        header.add("vmware-api-session-id:" + getToken());
		
        String uri = new String();
        
        if(state.equals("ON")) {
        	uri = baseURL + "/api/vcenter/vm/vm-" + vmId + "/power?action=start";
        }else if(state.equals("OFF")) {
        	uri = baseURL + "/api/vcenter/vm/vm-" + vmId + "/power?action=stop";
        }else {
        	return;
        }
        try {
        	HttpsConnecter.connect(uri, "POST", header);
		}catch (Exception e) {
	
		}
	}
	
	@PostMapping("createVM")
	public String createVM(
			@RequestParam("vmname") String vmname,
			@RequestParam("catalType") String catalType) {
		
		ArrayList<String> header = new ArrayList<String>();
        header.add("vmware-api-session-id:" + getToken());
        header.add("Content-Type:application/json");
        
        JSONObject placement = new JSONObject();
        placement.put("datastore", "datastore-28");
        placement.put("host", "host-25");
        
        JSONObject spec = new JSONObject();
        spec.put("power_on", false);
        spec.put("name", vmname);
        
        if(catalType.equals("A")) {
            spec.put("source", "vm-51");
        }else if(catalType.equals("B")){
            spec.put("source", "vm-53");
        }else {
        	return "false";
        }
        
        spec.put("placement", placement);
        
        String result = HttpsConnecter.connect(baseURL + "/api/vcenter/vm?action=clone", "POST", spec, header);
		
		return result;
	}
}