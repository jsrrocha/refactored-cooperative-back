package com.challenge.cooperative.service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

	@Autowired
	JdbcTemplate jdbcTemplate;

	public List<Map<String, String>> findAllSessionAttributes() {
		try {
			String sql = "SELECT * FROM SPRING_SESSION_ATTRIBUTES";
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

			List<Map<String, String>> sessionAttributes = new ArrayList<Map<String, String>>();
			for (Map<String, Object> row : rows) {
				ObjectInput input = new ObjectInputStream(new ByteArrayInputStream((byte[]) row.get("attribute_bytes")));
				String attribute = input.readObject().toString(); 

				Map<String, String> sessionAttribute = new HashMap<String, String>();
				sessionAttribute.put("sessionId", row.get("session_primary_id").toString());
				sessionAttribute.put("attributeName", row.get("attribute_name").toString()); 
				sessionAttribute.put("attribute", attribute); 
				sessionAttributes.add(sessionAttribute);
			} 

			return sessionAttributes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isAgendaSessionOpen(List<Map<String, String>> sessionAttributes, String agendaId) {
		boolean found = false;
		if (sessionAttributes != null) {
			found = sessionAttributes.stream()
					.filter(sessionAttribute -> sessionAttribute.get("attribute").equals(agendaId)).count() > 0;
		}
		return found;
	}

	public Map<String, String> findSessionAttributeAgendaId(String agendaId)  {
		Map<String, String> sessionAttributeMap = null;
		
		List<Map<String, String>> sessionAttributes = findAllSessionAttributes();
		if (sessionAttributes != null) {
			Stream<Map<String, String>> sessionsStream = sessionAttributes.stream()
					.filter(sessionAttribute -> sessionAttribute.get("attribute").equals(agendaId));

			Optional<Map<String, String>> sessionOptional = sessionsStream.findFirst();
			if (sessionOptional.isPresent()) {
				sessionAttributeMap = sessionOptional.get();
			}
		}
		
		return sessionAttributeMap;
	}
	
	public void deleteSessionAttributeAgendaId(String agendaId) {
		Map<String, String> sessionAttributeMap = findSessionAttributeAgendaId(agendaId);
		
		String sql = "DELETE FROM SPRING_SESSION_ATTRIBUTES WHERE session_primary_id = ?";
	    Object[] args = new Object[] {sessionAttributeMap.get("sessionId")};
	    
        jdbcTemplate.update(sql, args); 			
	}
}