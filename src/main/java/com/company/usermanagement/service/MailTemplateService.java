package com.company.usermanagement.service;


import com.company.usermanagement.entity.MailTemplate;

import java.util.Map;

public interface MailTemplateService {

	MailTemplate getActiveTemplate(String templateCode);

	String resolveSubject(MailTemplate template, Map<String, Object> variables);

	String resolveBody(MailTemplate template, Map<String, Object> variables);

}
