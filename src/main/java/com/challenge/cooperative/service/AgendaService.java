package com.challenge.cooperative.service;


import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.challenge.cooperative.model.entities.Agenda;
import com.challenge.cooperative.repository.AgendaRepository;

@Service
public class AgendaService {
	@Autowired
	private AgendaRepository agendaRepository;

	public Agenda saveAgenda(Agenda agenda) {
		return agendaRepository.save(agenda);
	}
	
	public Agenda getAgendaById(Long id) {
		Agenda agenda = null;
		Optional<Agenda> optionalAgenda = agendaRepository.findById(id);
		if (optionalAgenda.isPresent()) {
			agenda = optionalAgenda.get();
		}
		return agenda;
	}
	
	public Iterable<Agenda> getAllAgendas() {
		Iterable<Agenda> agendas = agendaRepository.findAll();
		return agendas;
	} 
	
	public boolean isThereAgendaWithName(String name) {
		boolean agendaExists = false;
		
		Agenda agenda = agendaRepository.findByName(name);
		if (agenda != null) {
			agendaExists = true;
		}
		return agendaExists;
	}
	
	public boolean isThereAgendaWithId(Long id) {
		boolean agendaExists = false;
		
		Optional<Agenda> optionalAgenda = agendaRepository.findById(id);
		if (optionalAgenda.isPresent()) {
			agendaExists = true;
		} 
		return agendaExists;
	}
}
