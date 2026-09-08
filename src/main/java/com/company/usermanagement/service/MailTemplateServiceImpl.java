package com.company.usermanagement.service;

import com.company.usermanagement.entity.MailTemplate;
import com.company.usermanagement.exception.MailTemplateNotFoundException;
import com.company.usermanagement.repository.MailTemplateRepository;
import com.company.usermanagement.utility.TemplateVariableResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailTemplateServiceImpl implements MailTemplateService {

	private final MailTemplateRepository mailTemplateRepository;
	private final TemplateVariableResolver templateVariableResolver;

	@Override
	public MailTemplate getActiveTemplate(String templateCode) {
		return mailTemplateRepository.findByTemplateCodeAndIsActive(templateCode, true)
				.orElseThrow(() -> new MailTemplateNotFoundException(
						"Active mail template not found for code: " + templateCode));
	}

	@Override
	public String resolveSubject(MailTemplate template, Map<String, Object> variables) {
		return templateVariableResolver.resolve(template.getSubject(), variables);
	}

	@Override
	public String resolveBody(MailTemplate template, Map<String, Object> variables) {
		return templateVariableResolver.resolve(template.getBody(), variables);
	}

}
