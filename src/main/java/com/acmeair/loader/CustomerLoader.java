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
package com.acmeair.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.acmeair.service.CustomerService;

@Component
public class CustomerLoader {
	
	@Autowired
	CustomerService customerService;

	public void dropCustomers() {		
		customerService.dropCustomers();
	}
	
	public void loadCustomers(long numCustomers) {				
		
		String addressJson =  "{streetAddress1 : \"123 Main St.\", streetAddress2 :null, city: \"Anytown\", stateProvince: \"NC\", country: \"USA\", postalCode: \"27617\"}";
		
		for (long ii = 0; ii < numCustomers; ii++) {
			customerService.createCustomer("uid"+ii+"@email.com", "password", "GOLD", 1000000, 1000, "919-123-4567", "BUSINESS", addressJson);
		}
	}

}