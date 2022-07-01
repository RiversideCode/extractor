package com.riversidecode.extractor.service;

import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command
public class GenericCli {
	@Inject
	protected GenericCli() {
		// injection
	}
}
