package com.acmeair.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class Util {

	private static Gson gson = new GsonBuilder().create();

	public static void registerService(){
		String PORT = System.getenv("VCAP_APP_PORT");
		String NAME = System.getenv("SERVICE_NAME");
		String BEARER_TOKEN = System.getenv("SD_TOKEN");
		String SD_URL = System.getenv("SD_URL");
		String space_id = System.getenv("space_id");

		int TIME_TO_LIVE = 300;
		int SLEEP_TIME= new Double(TIME_TO_LIVE*0.9*1000).intValue();

		String requestUrl = SD_URL + "/api/v1/instances";

		if (space_id != null){
			String SERVICE_IP = "";
			try {
				SERVICE_IP = Inet4Address.getLocalHost().getHostAddress();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			JsonObject jsonObj = new JsonObject();
			JsonObject endpoint = new JsonObject();
			JsonArray empty = new JsonArray();

			endpoint.add("type", new JsonPrimitive("http"));
			endpoint.add("value", new JsonPrimitive(SERVICE_IP +":"+ PORT));

			jsonObj.add("tags",empty);
			jsonObj.add("status",new JsonPrimitive("UP"));
			jsonObj.add("service_name", new JsonPrimitive(NAME));
			jsonObj.add("endpoint", endpoint);
			jsonObj.add("ttl", new JsonPrimitive(TIME_TO_LIVE));

			byte[] postData = gson.toJson(jsonObj).getBytes( StandardCharsets.UTF_8 );

			URL url;
			while (true){
				try {
					System.out.println("REGISTERING THIS SERVICE...");
					url = new URL( requestUrl );
					HttpURLConnection conn= (HttpURLConnection) url.openConnection();
					conn.setDoOutput( true );
					conn.setInstanceFollowRedirects( false );
					conn.setRequestMethod( "POST" );
					conn.setRequestProperty( "Content-Type", "application/json");
					conn.setRequestProperty( "authorization", "Bearer " + BEARER_TOKEN);
					conn.setRequestProperty( "X-Forwarded-Proto", "https");
					try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
					   wr.write( postData );
					   wr.flush();
					   wr.close();
					}

					String line;
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					StringBuffer response = new StringBuffer();
					while((line = br.readLine()) != null) {
						response.append(line);
					}
					br.close();

					System.out.println("Response Code : " + conn.getResponseCode()
							+ " Response : " + response.toString());

					JsonObject responseJson = gson.fromJson(response.toString(), JsonObject.class);
					JsonObject linkJson = gson.fromJson(responseJson.get("links").toString(), JsonObject.class);
					try{
						sendHeartbeat(linkJson.get("heartbeat").getAsString(), BEARER_TOKEN, SLEEP_TIME);
					}catch (Exception e){
						System.out.println("HEARTBEAT FAILED AT " + e.getClass() + " WITH ERROR : " + e.getMessage() + " RE-REGISTERING");
					}
				} catch (Exception e) {
					int sleepTime = 10000;
					System.out.println("REGISTRATION FAILED AT " + e.getClass() + " WITH ERROR : " + e.getMessage() + " RE-REGISTERING AFTER " + sleepTime/1000 + " sec  SLEEP");
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public static void sendHeartbeat(String heartbeatUrl, String BEARER_TOKEN, int SLEEP_TIME) throws Exception{
		URL url;
		while (true){
			Thread.sleep(SLEEP_TIME);
			System.out.print("Heartbeat Check ");
			url = new URL( heartbeatUrl );
			HttpURLConnection conn= (HttpURLConnection) url.openConnection();
			conn.setDoOutput( true );
			conn.setInstanceFollowRedirects( false );
			conn.setRequestMethod( "PUT" );
			conn.setRequestProperty( "Content-Type", "application/json");
			conn.setRequestProperty( "authorization", "Bearer " + BEARER_TOKEN);
			conn.setRequestProperty( "X-Forwarded-Proto", "https");
			conn.getOutputStream();
			int responseCode = conn.getResponseCode();
			System.out.println( "Response Code : " + responseCode);
			if (responseCode != 200){
				throw new Not200Exception(Integer.toString(conn.getResponseCode()));
			}
		}
	}

	static class Not200Exception extends Exception
	{
		private static final long serialVersionUID = 1L;

		public Not200Exception(String message)
		{
			super(message);
		}
	}

	public static String getServiceProxy() {

		String SD_URL = System.getenv("SD_URL");
		String BEARER_TOKEN = System.getenv("SD_TOKEN");

		String requestUrl = SD_URL + "/api/v1/services/ServiceProxy";

		while (true){
			try {

				System.out.println("GETTING SERVICE PROXY URL...");
				URL url = new URL( requestUrl );
				HttpURLConnection conn= (HttpURLConnection) url.openConnection();
				conn.setRequestProperty( "authorization", "Bearer " + BEARER_TOKEN);

				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer response = new StringBuffer();
				while((line = br.readLine()) != null) {
					response.append(line);
				}
				br.close();

				System.out.println("SP Response Code : " + conn.getResponseCode()
						+ " SP Response : " + response.toString());

				JsonObject responseJson = gson.fromJson(response.toString(), JsonObject.class);
				JsonArray instancesJson = gson.fromJson(responseJson.get("instances").getAsString(), JsonArray.class);
				JsonObject instanceJson = gson.fromJson(instancesJson.get(0).getAsString(), JsonObject.class);
				JsonObject endpointJson = gson.fromJson(instanceJson.get("endpoint").getAsString(), JsonObject.class);
				return endpointJson.get("value").getAsString();
			} catch (Exception e) {
				int sleepTime = 10000;
				System.out.println("FAILED TO GET THE SERVICE PROXY AT " + e.getClass() + " WITH ERROR : " + e.getMessage() + " RE-TRYING AFTER " + sleepTime/1000 + " sec  SLEEP");
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}
}
