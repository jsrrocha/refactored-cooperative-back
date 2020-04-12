package com.challenge.cooperative.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.cooperative.model.dtos.AssociateDTO;
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
	public ResponseEntity<?> addAssociate(@RequestBody AssociateDTO associateDTO){
		try {
			String errorMessage = associateUtil.validateAssociateDTO(associateDTO);
			if(errorMessage != null) {
				return new ResponseEntity<String>(errorMessage, HttpStatus.BAD_REQUEST); 
			}
			
			Associate associate = associateUtil.convertToEntity(associateDTO);
			associate = associateService.saveAssociate(associate);
			associateDTO = associateUtil.convertToDTO(associate);
			
			return new ResponseEntity<AssociateDTO>(associateDTO, HttpStatus.CREATED); 
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
		}
	}
	
	@GetMapping("/")
	public ResponseEntity<?> getAssociate(){
		try {
			Iterable<Associate> associates = associateService.getAllAssociates();
			List<AssociateDTO> associatesDTO = associateUtil.convertListToDTO(associates); 
			return new ResponseEntity<List<AssociateDTO>>(associatesDTO, HttpStatus.OK);  
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
		}
	} 
}
