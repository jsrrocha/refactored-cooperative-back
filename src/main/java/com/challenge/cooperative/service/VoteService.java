package com.challenge.cooperative.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.challenge.cooperative.model.dtos.VoteDTO;
import com.challenge.cooperative.model.entities.Agenda;
import com.challenge.cooperative.model.entities.Associate;
import com.challenge.cooperative.model.entities.Vote;
import com.challenge.cooperative.model.entities.Voting;
import com.challenge.cooperative.repository.VoteRepository;

@Service
public class VoteService {
	
	@Autowired 
	private VoteRepository voteRepository;
	
	@Autowired
	private AssociateService associateService; 
	
	@Autowired
	private VotingService votingService;
	
	@Autowired 
	private AgendaService agendaService;
	
	public Vote saveVote(Vote vote) {
		return voteRepository.save(vote);
	}
	
	public Vote convertToEntity(VoteDTO voteDTO) {
		Associate associate = associateService.getAssociateById(new Long(voteDTO.getAssociateId()));
		Agenda agenda = agendaService.getAgendaById(new Long(voteDTO.getAgendaId()));
		Voting voting = votingService.getVotingByAgenda(agenda).get(0);
		Vote vote =  new Vote();
		vote.setVote(voteDTO.getVote().equals("Sim") ? true : false); 
		vote.setAssociate(associate);
		vote.setVoting(voting);
		
		return vote;
	}
}
