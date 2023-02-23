package com.acmeair.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acmeair.loader.Loader;


@RestController
@RequestMapping("/info/loader")
public class LoaderREST {

//	private static Logger logger = Logger.getLogger(LoaderREST.class.getName());


/*
 * Disabling to test out the new acmeair code frist
 */

	@Autowired
	private Loader loader;

	@RequestMapping(value ="/query", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<String> queryLoader() {
		String response = loader.queryLoader();
		return ResponseEntity.ok(response);
	}


	@RequestMapping(value ="/load", method = RequestMethod.GET, produces = "text/plain")
	public ResponseEntity<String> loadDB(@RequestParam(name = "numCustomers", defaultValue = "-1") long numCustomers) {
		String response = loader.loadDB(numCustomers);
		return ResponseEntity.ok(response);
	}

}
