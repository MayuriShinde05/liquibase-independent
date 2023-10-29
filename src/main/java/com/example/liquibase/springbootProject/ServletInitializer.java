package com.example.liquibase.springbootProject;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author ina082maynara
 * The Class ServletInitializer.
 */
public class ServletInitializer extends SpringBootServletInitializer {
	
	/**
	 * Configure.
	 *
	 * @param application the application
	 * @return the spring application builder
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SpringbootProjectApplication.class);
	}
}