/*******************************************************************************
* Copyright (c) 2013-2015 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.acmeair.service;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public abstract class AuthService {
	protected static final int DAYS_TO_ALLOW_SESSION = 1;

	@Autowired
	protected KeyGenerator keyGenerator;
	
	private Gson gson = new GsonBuilder().create();



	// TODO: Do I really need to create a JSONObject here or just return a Json string?
	public JsonObject validateSession(String sessionid) {
		String cSession = getSession(sessionid);
		if (cSession == null) {
			return null;
		}

		Date now = new Date();
		JsonObject sessionJson = gson.fromJson(cSession, JsonObject.class);
		Long timeout = sessionJson.get("timeoutTime").getAsLong();
		if (now.getTime() > timeout) {
			removeSession(cSession);
			return null;
		}

		return sessionJson;
	}

	protected abstract String getSession(String sessionid);

	protected abstract void removeSession(String sessionJson);

	// TODO: Do I really need to create a JSONObject here or just return a Json string?
	// TODO: Maybe simplify as Moss did, but need to change node.js version first
	public JsonObject createSession(String customerId) {
		String sessionId = keyGenerator.generate().toString();
		Date now = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DAY_OF_YEAR, DAYS_TO_ALLOW_SESSION);
		Date expiration = c.getTime();

		JsonObject sessionJson = null;

		try{
			sessionJson = gson.fromJson(createSession(sessionId, customerId, now, expiration), JsonObject.class);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return sessionJson;
	}

	protected abstract String createSession(String sessionId, String customerId, Date creation, Date expiration);

	public abstract void invalidateSession(String sessionid);

	public abstract Long countSessions();

	public abstract void dropSessions();

}
