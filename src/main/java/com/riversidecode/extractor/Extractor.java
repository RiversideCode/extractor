package com.riversidecode.extractor;

import com.riversidecode.extractor.inject.DaggerExtractorComponent;
import com.riversidecode.extractor.inject.ExtractorComponent;
import com.riversidecode.extractor.service.CsvExtractor;
import com.riversidecode.extractor.service.GenericCli;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class Extractor {

	@Inject
	protected GenericCli genericCli;
	@Inject
	protected CsvExtractor csvExtractor;

	@Inject
	public Extractor() {
		// injection
	}

	public static void main(String[] args) throws Exception {
		log.info("Starting " + Extractor.class.getSimpleName());

		// Setup dependency injection.
		Extractor instance;
		try {
			ExtractorComponent component = DaggerExtractorComponent.builder()
				.build();
			instance = component.exctractor();
			component.inject(instance);

			instance.start(args);
		}
		catch (Exception e) {
			log.error("Error during dependency injection", e);
			System.exit(1);
		}
	}

	public void start(String[] args) {
		CommandLine main = new CommandLine(genericCli)
			.addSubcommand(csvExtractor);

		AtomicReference<Exception> exceptionRef = new AtomicReference<>();
		main.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
			exceptionRef.set(ex);
			return 1;
		});
		main.execute(args);
		if (exceptionRef.get() != null) {
			throw new RuntimeException("Execution failed", exceptionRef.get());
		}
		System.exit(0);
	}
}
