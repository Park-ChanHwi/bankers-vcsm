package bankers.fisa.connecter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.json.JSONObject;

public class HttpsConnecter {
	
	public static String connect(String strURL, String requestMethod, JSONObject json, ArrayList<String> header){
		URL url = null;
		HttpsURLConnection conn = null;
		String result = new String();
		try {
			url = new URL(strURL);
			HttpsConnecter.ignoreSSL();
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod(requestMethod);
			conn.setDoOutput(true);
			
			for(String str : header) {
				String[] strArr = str.split(":");
				conn.setRequestProperty(strArr[0], strArr[1]);
			}
			
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = json.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}catch (Exception e){
				System.out.println(e);
			}
			
			BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			
			String input = null;
			while ((input = br.readLine()) != null) {
				result += input;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
		return result;
	}
	
	public static String connect(String strURL, String requestMethod, ArrayList<String> header) throws Exception{
		URL url = null;
		HttpsURLConnection conn = null;
		String result = new String();
		
		url = new URL(strURL);
		HttpsConnecter.ignoreSSL();
		conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestMethod(requestMethod);
		
		for(String str : header) {
			String[] strArr = str.split(":");
			conn.setRequestProperty(strArr[0], strArr[1]);
		}
			
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		
		String input = null;
		while ((input = br.readLine()) != null) {
			result += input;
		}
		br.close();
		
		return result;
	}
	
	public static void ignoreSSL() throws Exception {
		HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return false;
			}
		};
		trustAllHttpsCertificates();
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
	
	private static void trustAllHttpsCertificates() throws Exception{
		TrustManager[] trustAllCerts = new TrustManager[1];
		TrustManager tm = new MyTrustManager();
		trustAllCerts[0] = tm;
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}
}
