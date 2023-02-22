/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
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
package com.acmeair.web;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.service.AuthService;
import com.acmeair.service.CustomerService;


@RestController
@RequestMapping("/login")
public class LoginREST {
	
	public static String SESSIONID_COOKIE_NAME = "acmeair_sessionid";
			
	@Autowired
	AuthService authService;

	@Autowired
	CustomerService customerService;
	
	@RequestMapping(value = "login", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded", produces = "text/plain")
	public ResponseEntity<String> login(@RequestParam("login") String login, @RequestParam("password") String password) {
		try {

			boolean validCustomer = customerService.validateCustomer(login, password);
			
			if (!validCustomer) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}	
			
			JSONObject sessionJson = authService.createSession(login);
			return ResponseEntity.status(HttpStatus.OK).header("Set-Cookie", SESSIONID_COOKIE_NAME + "=" + sessionJson.get("_id") + "; Path=/").body("logged in");
	
		}
		catch (Exception e) {
			e.printStackTrace();
      		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@RequestMapping(value = "logout", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<String> logout(@RequestParam("login") String login, @CookieValue("acmeair_sessionid") String sessionid) {
		try {
			if (sessionid == null) {
				System.out.println("sessionid is null");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			if (sessionid.equals(""))
			{
				System.out.println("sessionid is empty");
			} else {
				authService.invalidateSession(sessionid);
			}

			return ResponseEntity.status(HttpStatus.OK).body("logged out");
		}
		catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
