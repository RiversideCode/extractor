package com.riversidecode.extractor.utils;

import com.google.common.base.Strings;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
public class TempFileUtil {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
		.ofPattern("yyyy-MM-dd_HH-mm-ss")
		.withZone(ZoneOffset.UTC);

	private String prefix;
	private Path pathDirectory;

	public TempFileUtil(@NonNull String prefix, @NonNull String directory) {
		this.prefix = prefix;
		setDirectory(Paths.get(directory));
	}

	public TempFileUtil(@NonNull String prefix, @NonNull Path pathDirectory) {
		this.prefix = prefix;
		setDirectory(pathDirectory);
	}

	@SneakyThrows
	private void setDirectory(Path directory) {
		pathDirectory = directory;
		directoryExists(directory);

		if (!Files.isDirectory(directory)) {
			throw new IOException(directory + " is not a directory.");
		}
		if (!Files.isWritable(directory)) {
			throw new IOException(directory + " is not writable.");
		}
	}

	public Path createNewTempFile() {
		return createNewTempFile(null, null);
	}

	@SneakyThrows
	public Path createNewTempFile(String optionalPrefix, String optionalExtension) {
		// Create inner directory.
		String innerDir = Integer.toHexString(ThreadLocalRandom.current().nextInt()).substring(0, 4);
		Path newDir = Paths.get(pathDirectory + "/" + innerDir);

		directoryExists(newDir);

		// Create temp file.
		StringBuilder path = new StringBuilder(128)
			.append(this.prefix)
			.append('_')
			.append(DATE_FORMATTER.format(Instant.now()))
			.append('_');

		if (!Strings.isNullOrEmpty(optionalPrefix)) {
			path.append(optionalPrefix).append('_');
		}

		if (Strings.isNullOrEmpty(optionalExtension)) {
			optionalExtension = ".tmp";
		}

		return Files.createTempFile(newDir, path.toString(), optionalExtension);
	}

	@SneakyThrows
	private void directoryExists(Path path) {
		if (Files.notExists(path)) {
			try {
				Files.createDirectory(path);
			}
			catch (FileAlreadyExistsException e) {
				log.warn("File exists", e);
			}
		}
	}
}
