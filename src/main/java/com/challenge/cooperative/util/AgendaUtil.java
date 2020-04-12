package com.challenge.cooperative.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.challenge.cooperative.model.dtos.AgendaDTO;
import com.challenge.cooperative.model.dtos.VoteDTO;
import com.challenge.cooperative.model.entities.Agenda;
import com.challenge.cooperative.model.entities.Associate;
import com.challenge.cooperative.model.entities.Vote;
import com.challenge.cooperative.model.entities.Voting;
import com.challenge.cooperative.service.AgendaService;
import com.challenge.cooperative.service.AssociateService;
import com.challenge.cooperative.service.SessionService;
import com.challenge.cooperative.service.VotingService;

@Component
public class AgendaUtil {

	@Autowired
	private SessionService sessionService; 
	
	@Autowired
	private AgendaService agendaService; 
	
	@Autowired
	private AssociateService associateService; 
	
	@Autowired
	private VotingService votingService; 
	
	@Autowired
	private SessionUtil sessionUtil;
	

	public String validateAgendaDTO(AgendaDTO agendaDTO) {
		String name = agendaDTO.getName();
		if (name == null) {
			return "Preencha o nome da pauta";
		}
		
		if (agendaService.isThereAgendaWithName(name)) {
			return "Já existe uma pauta com esse nome";
		}
		return null;
	}
	
	public Agenda convertToEntity(AgendaDTO agendaDTO) {
		Agenda agenda = new Agenda();
		agenda.setName(agendaDTO.getName());
		return agenda;
	}
	
	public AgendaDTO convertToDTO(Agenda agenda) {
		AgendaDTO agendaDTO = new AgendaDTO();
		agendaDTO.setName(agenda.getName());
		return agendaDTO;
	} 
	
	public List<AgendaDTO> convertListToDTO(Iterable<Agenda> agendas){
		List<AgendaDTO> agendasDTO = new ArrayList<AgendaDTO>();
		agendas.forEach(agenda ->{
			AgendaDTO agendaDTO = new AgendaDTO();
			agendaDTO.setId(agenda.getId());
			agendaDTO.setName(agenda.getName());
			agendasDTO.add(agendaDTO);
		});
		return agendasDTO;
	}

	public String validateOpenSessionInput(Long id, Integer time) {
		if (!agendaService.isThereAgendaWithId(id)) {
			return "Pauta não existe";
		}
	
		if (time <= 0) {
			return "Preencha o tempo da sessão com um valor maior que 0";
		}

		List<Map<String, String>> sessionAttributes = sessionService.findAllSessionAttributes();
		boolean foundOpenSession = sessionUtil.isAgendaSessionOpen(sessionAttributes, id.toString());
		if (foundOpenSession) {
			return "Já existe uma votação aberta para essa pauta";
		}
		return null;
	}

	public String validateVote(VoteDTO voteDTO) {
		if (voteDTO.getVote() == null) { 
			return "Preencha o voto";
		}

		if (voteDTO.getAssociateId() == null) {
			return "Preencha o associado";
		}
		
		if (voteDTO.getAgendaId() == null) {
			return "Preencha a pauta";
		}
		
		Associate associate = associateService
				.getAssociateById(new Long(voteDTO.getAssociateId()));
		if (associate == null) {
			return "Associado não existe";
		}
		
		Agenda agenda = agendaService.getAgendaById(new Long(voteDTO.getAgendaId()));
		if (agenda == null) {
			return "Agenda não existe";
		}
		
		List<Voting> votingList = votingService.getVotingByAgenda(agenda);
		if (votingList.isEmpty()) {
			return "Não existe votação para essa pauta";
		}

		List<Map<String, String>> sessionAttributes = sessionService
				.findAllSessionAttributes();
		boolean foundOpenSession = sessionUtil
				.isAgendaSessionOpen(sessionAttributes, voteDTO.getAgendaId().toString());
		if (!foundOpenSession) {
			return "Não existe votação aberta para essa pauta";
		}
		
		Voting voting = votingList.get(0); 
		if (associateAlreadyVoted(associate, voting.getVotes())) {
			return "Associado já votou nessa pauta";
		}

		return null;
	} 

	public boolean associateAlreadyVoted(Associate associate, List<Vote> votes) {
		return votes.stream()
				.filter(vote -> vote.getAssociate().getId().equals(associate.getId())).count() > 0;
	}

	public String validateResultInput(Long id) {
		
		Agenda agenda = agendaService.getAgendaById(id);
		if (agenda == null) {
			return "Agenda não existe";
		}
		
		List<Voting> votingList = votingService.getVotingByAgenda(agenda);
		if (votingList.isEmpty()) {
			return "Não existe votação para essa pauta";
		}
		
		List<Map<String, String>> sessionAttributes = sessionService.findAllSessionAttributes();
		boolean foundOpenSession = sessionUtil
				.isAgendaSessionOpen(sessionAttributes,votingList.get(0).getAgenda().getId().toString());
		if (foundOpenSession) {
			return "Votação para essa pauta ainda está aberta";
		}

		return null;
	} 

	public String getResult(List<Vote> votes) {
		Long optionYes = votes.stream().filter(vote-> vote.isVote() == true).count();
        Long optionNo = votes.stream().filter(vote-> vote.isVote() == false).count(); 
		
        String result = "";
		if (optionYes > optionNo) {
			result = "Pauta aprovada";
		} else if (optionYes < optionNo) {
			result = "Pauta reprovada";
		} else {
			result = "Votação deu empate";
		}

		return result;
	}

	public Map<String, String> buildResponseMap(String result) {
		Map<String, String> response = new HashMap<String, String>();
		response.put("result", result);

		return response;
	}

}
