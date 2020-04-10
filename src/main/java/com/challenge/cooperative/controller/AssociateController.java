package com.challenge.cooperative.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.cooperative.model.entities.Associate;
import com.challenge.cooperative.service.AssociateService;
import com.challenge.cooperative.util.AssociateUtil;

@RestController
@RequestMapping("associate") 
public class AssociateController { 
	
	@Autowired 
	private AssociateService associateService;
	
	@Autowired
	private AssociateUtil associateUtil;
	
	@PostMapping("/add")
	public ResponseEntity<?> addAssociate(@RequestBody Map<String, String> associateMap){
		try {
			String errorMessage = associateUtil.validateAssociateMap(associateMap);
			if(errorMessage != null) {
				return new ResponseEntity<String>(errorMessage, HttpStatus.BAD_REQUEST); 
			}
			
			Associate associate = associateService
					.saveAssociate(associateMap.get("name").toString());
			return new ResponseEntity<Associate>(associate, HttpStatus.OK); 

		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
		}
	}
	
	@GetMapping("/")
	public ResponseEntity<?> getAssociate(){
		try {
			Iterable<Associate> associate = associateService.getAllAssociates();
			return new ResponseEntity<>(associate, HttpStatus.OK);  
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
		}
	} 
}
