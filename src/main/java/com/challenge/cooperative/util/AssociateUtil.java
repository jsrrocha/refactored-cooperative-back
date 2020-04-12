package com.challenge.cooperative.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import com.challenge.cooperative.model.dtos.AssociateDTO;
import com.challenge.cooperative.model.entities.Associate;

@Component
public class AssociateUtil {

	public String validateAssociateDTO(AssociateDTO associateDTO) {
		if (associateDTO.getName() == null) {
			return "Preencha o nome do associado";
		}
		return null;
	}
	
	public Associate convertToEntity(AssociateDTO associateDTO) {
		Associate associate = new Associate();
		associate.setName(associateDTO.getName());
		return associate;
	}
	
	public AssociateDTO convertToDTO(Associate associate) {
		AssociateDTO associateDTO = new AssociateDTO();
		associateDTO.setId(associate.getId());
		associateDTO.setName(associate.getName());
		return associateDTO;
	}
	
	public List<AssociateDTO> convertListToDTO(Iterable<Associate> associates){
		List<AssociateDTO> associatesDTO = new ArrayList<AssociateDTO>();
		associates.forEach(associate ->{
			AssociateDTO associateDTO = new AssociateDTO();
			associateDTO.setId(associate.getId());
			associateDTO.setName(associate.getName());
			associatesDTO.add(associateDTO);
		});
		return associatesDTO;
	}
	
	
}
