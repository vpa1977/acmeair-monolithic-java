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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.service.BookingService;

@RestController
@RequestMapping("/customer")
public class BookingsREST {

	@Autowired
	private BookingService bs;
	
	@RequestMapping(value = "/bookflights", method = RequestMethod.POST, produces = "text/plain", consumes = "application/x-www-form-urlencoded")
	public /*BookingInfo*/ ResponseEntity<String> bookFlights(
			@RequestParam("userid") String userid,
			@RequestParam("toFlightId") String toFlightId,
			@RequestParam("toFlightSegId") String toFlightSegId,
			@RequestParam("retFlightId") String retFlightId,
			@RequestParam("retFlightSegId") String retFlightSegId,
			@RequestParam("oneWayFlight") boolean oneWay) {
		try {
			String bookingIdTo = bs.bookFlight(userid, toFlightSegId, toFlightId);
			
			String bookingInfo = "";
			
			String bookingIdReturn = null;
			if (!oneWay) {
				bookingIdReturn = bs.bookFlight(userid, retFlightSegId, retFlightId);
				bookingInfo = "{\"oneWay\":false,\"returnBookingId\":\"" + bookingIdReturn + "\",\"departBookingId\":\"" + bookingIdTo + "\"}";
			}else {
				bookingInfo = "{\"oneWay\":true,\"departBookingId\":\"" + bookingIdTo + "\"}";
			}
			return new ResponseEntity<>(bookingInfo, HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
		
	@RequestMapping(value = "/byuser/{user}", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<String> getBookingsByUser(@PathVariable("user") String user) {
		try {
			return new ResponseEntity<>(bs.getBookingsByUser(user).toString(), HttpStatus.OK);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@RequestMapping(value = "/cancelbooking", method = RequestMethod.POST, produces = "text/plain", consumes = "application/x-www-form-urlencoded")
	public ResponseEntity<String> cancelBookingsByNumber(
			@RequestParam("number") String number,
			@RequestParam("userid") String userid) {
		try {
			bs.cancelBooking(userid, number);
			return new ResponseEntity<>("booking " + number + " deleted.", HttpStatus.OK);
					
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

}