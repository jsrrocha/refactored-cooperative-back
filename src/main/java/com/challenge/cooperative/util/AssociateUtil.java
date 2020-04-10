package com.challenge.cooperative.util;

import java.util.Map;

import org.springframework.stereotype.Service;


@Service
public class AssociateUtil {

	public String validateAssociateMap(Map<String, String> associateMap) {
		if (associateMap.get("name") == null) {
			return "Preencha o nome do associado";
		}
		return null;
	}
	
}
