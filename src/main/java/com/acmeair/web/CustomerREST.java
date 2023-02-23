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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.AcmeAirConstants;
import com.acmeair.service.CustomerService;
import com.acmeair.service.FlightService;
import com.acmeair.web.dto.CustomerInfo;

@RestController
@RequestMapping("/api/customer")
public class CustomerREST {
	
	@Autowired
	CustomerService customerService;
	
	@Autowired 
	private HttpServletRequest request;

	private boolean validate(String customerid)	{
		String loginUser = (String) request.getAttribute(RESTCookieSessionFilter.LOGIN_USER);
		if(logger.isLoggable(Level.FINE)){
			logger.fine("validate : loginUser " + loginUser + " customerid " + customerid);
		}
		return customerid.equals(loginUser);
	}
	
	protected Logger logger =  Logger.getLogger(FlightService.class.getName());

	@RequestMapping(value = "/byid/{custid}", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<String> getCustomer(@CookieValue(AcmeAirConstants.SESSIONID_COOKIE_NAME) String sessionid, @PathVariable("custid") String customerid) {
		if(logger.isLoggable(Level.FINE)){
			logger.fine("getCustomer : session ID " + sessionid + " userid " + customerid);
		}
		try {
			// make sure the user isn't trying to update a customer other than the one currently logged in
			if (!validate(customerid)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			return new ResponseEntity<>(customerService.getCustomerByUsername(customerid), HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = "/byid/{custid}", method = RequestMethod.POST, consumes = "application/json", produces = "text/plain")
	public /* Customer */ ResponseEntity<String> putCustomer(@CookieValue(AcmeAirConstants.SESSIONID_COOKIE_NAME) String sessionid, @RequestBody CustomerInfo customer) {

		if (customer == null)
		{
			logger.severe("Missing customerInfo for session "+sessionid);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String username = customer.get_id();
		
		if (!validate(username)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		String customerFromDB = customerService.getCustomerByUsernameAndPassword(username, customer.getPassword());
		if(logger.isLoggable(Level.FINE)){
			logger.fine("putCustomer : " + customerFromDB);
		}

		if (customerFromDB == null) {
			// either the customer doesn't exist or the password is wrong
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		customerService.updateCustomer(username, customer);
		
		//Retrieve the latest results
		customerFromDB = customerService.getCustomerByUsernameAndPassword(username, customer.getPassword());
		return new ResponseEntity<>(customerFromDB, HttpStatus.OK);
	}
	

	
}
