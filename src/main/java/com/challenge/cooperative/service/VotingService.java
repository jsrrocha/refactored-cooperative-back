package com.challenge.cooperative.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.challenge.cooperative.model.entities.Agenda;
import com.challenge.cooperative.model.entities.Voting;
import com.challenge.cooperative.repository.VotingRepository;

public class VotingService {
	
	@Autowired
	private VotingRepository votingRepository;
	
	public Voting saveVoting(String name, Agenda agenda) {
		Voting voting = new Voting();
		voting.setName(name); 
		voting.setAgenda(agenda);
		voting = votingRepository.save(voting);
		return voting;
	}
	
	public List<Voting> getVotingByAgenda(Agenda agenda) {
		return votingRepository.findByAgendaOrderByIdDesc(agenda);
	}
	
	public boolean isThereVotingWithAgenda(Agenda agenda) {
		boolean votingExists = false;
		
		List<Voting> votingList = votingRepository.findByAgendaOrderByIdDesc(agenda);
		if (!votingList.isEmpty()) {
			votingExists = true;
		}
		return votingExists;
	}
}
