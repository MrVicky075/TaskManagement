package com.company.usermanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail")
@Getter
@Setter
public class MailConfig {

	private String from;
	private String host;
	private int port;
	private Notification notification = new Notification();

	@Getter
	@Setter
	public static class Notification {
		private String cc;
	}

}
