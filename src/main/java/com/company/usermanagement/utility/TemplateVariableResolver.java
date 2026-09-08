package com.company.usermanagement.utility;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateVariableResolver {

	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$([a-zA-Z][a-zA-Z0-9_]*)");

	public String resolve(String template, Map<String, Object> variables) {
		if (template == null || template.isEmpty()) {
			return template;
		}

		Matcher matcher = VARIABLE_PATTERN.matcher(template);
		StringBuilder result = new StringBuilder();

		while (matcher.find()) {
			String key = matcher.group(1);
			Object value = variables.get(key);
			String replacement = value != null ? Matcher.quoteReplacement(String.valueOf(value)) : "";
			matcher.appendReplacement(result, replacement);
		}
		matcher.appendTail(result);

		return result.toString();
	}

}
