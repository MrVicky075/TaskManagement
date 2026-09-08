package com.company.usermanagement.utility;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Compares old vs new field maps and builds human-readable HTML of only changed fields.
 */
@Component
public class ChangeDetectionService {

	private static final Map<String, String> FIELD_LABELS = new LinkedHashMap<>();

	static {
		FIELD_LABELS.put("userName", "Name");
		FIELD_LABELS.put("email", "Email");
		FIELD_LABELS.put("mobileNo", "Mobile");
		FIELD_LABELS.put("role", "Role");
		FIELD_LABELS.put("isActive", "Active Status");
		FIELD_LABELS.put("clientName", "Client Name");
		FIELD_LABELS.put("requirement", "Requirement");
		FIELD_LABELS.put("taskDetails", "Task Details");
		FIELD_LABELS.put("remarks", "Remarks");
		FIELD_LABELS.put("priority", "Priority");
		FIELD_LABELS.put("paid", "Paid");
		FIELD_LABELS.put("status", "Status");
		FIELD_LABELS.put("issue", "Issue");
		FIELD_LABELS.put("fixedOn", "Fixed On");
		FIELD_LABELS.put("days", "Days");
		FIELD_LABELS.put("redmineId", "Redmine ID");
		FIELD_LABELS.put("assignUserName", "Assigned User");
		FIELD_LABELS.put("assignUserId", "Assigned User ID");
	}

	public Map<String, Change> detectChanges(Map<String, Object> oldValues, Map<String, Object> newValues) {
		Map<String, Change> changes = new LinkedHashMap<>();
		if (oldValues == null || newValues == null) {
			return changes;
		}

		for (Map.Entry<String, Object> entry : newValues.entrySet()) {
			String key = entry.getKey();
			if ("taskId".equals(key) || "userId".equals(key)) {
				continue;
			}
			Object newVal = entry.getValue();
			Object oldVal = oldValues.get(key);
			if (!Objects.equals(normalize(oldVal), normalize(newVal))) {
				changes.put(key, new Change(oldVal, newVal));
			}
		}
		return changes;
	}

	public String toHtml(Map<String, Change> changes) {
		if (changes == null || changes.isEmpty()) {
			return "<p>No field-level changes detected.</p>";
		}

		StringBuilder html = new StringBuilder("<ul>");
		for (Map.Entry<String, Change> entry : changes.entrySet()) {
			String label = FIELD_LABELS.getOrDefault(entry.getKey(), entry.getKey());
			Change change = entry.getValue();
			html.append("<li><b>")
					.append(escape(label))
					.append(":</b> ")
					.append(escape(display(change.oldValue())))
					.append(" → ")
					.append(escape(display(change.newValue())))
					.append("</li>");
		}
		html.append("</ul>");
		return html.toString();
	}

	public String buildChangesHtml(Map<String, Object> oldValues, Map<String, Object> newValues) {
		return toHtml(detectChanges(oldValues, newValues));
	}

	public boolean hasChanges(Map<String, Object> oldValues, Map<String, Object> newValues) {
		return !detectChanges(oldValues, newValues).isEmpty();
	}

	private static Object normalize(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String str) {
			String trimmed = str.trim();
			return trimmed.isEmpty() ? null : trimmed;
		}
		return value;
	}

	private static String display(Object value) {
		if (value == null) {
			return "-";
		}
		String text = String.valueOf(value).trim();
		return text.isEmpty() ? "-" : text;
	}

	private static String escape(String value) {
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	public record Change(Object oldValue, Object newValue) {
	}

}
