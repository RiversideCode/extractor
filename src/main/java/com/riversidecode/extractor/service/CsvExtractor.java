package com.riversidecode.extractor.service;

import com.google.common.base.Strings;
import com.riversidecode.extractor.utils.CSVUtils;
import com.riversidecode.extractor.utils.CleanUtil;
import com.riversidecode.extractor.utils.DuplicateCacheService;
import com.riversidecode.extractor.utils.TempFileUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Hex;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.inject.Inject;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Command(name = "csvextractor")
public class CsvExtractor implements Runnable {

	@Option(names = {"-h", "?", "-help", "--help"},
		descriptionKey = "Add (-c OR --column <number>) for the column number to extract, Starting at 0.\n" +
			"Add (-f OR --file <filelocation>) for the file that is to be extracted\n" +
			"Add (-u OR --unknown <true/false>) for letting the program guess the column to extract"
	)
	public boolean help;

	@Option(names = {"-c", "--column"}, descriptionKey = "Column to be extracted from file.")
	public int columnNumber = 0;

	@Option(names = {"-f", "--file"}, descriptionKey = "File to run extraction on.")
	public String fileLocation = "";

	@Option(names = {"-u", "--unknown"}, descriptionKey = "Guess which column needs to be extracted.")
	public boolean unknownColumn;

	private final TempFileUtil temporaryFiles = new TempFileUtil(
		getClass().getSimpleName() + '-',
		"/tmp/extractor"
	);

	@Inject
	protected CSVUtils csvUtils;
	@Inject
	protected DuplicateCacheService duplicateCacheService;
	@Inject
	protected CleanUtil cleanUtil;

	@Inject
	public CsvExtractor() {
		// injection
	}

	@Override
	@SneakyThrows
	public void run() {
		if (help) {
			log.info(
				"Add (-c OR --column <number>) for the column number to extract, Starting at 0.\n" +
				"Add (-f OR --file <filelocation>) for the file that is to be extracted\n" +
				"Add (-u OR --unknown <true/false>) for letting the program guess the column to extract"
			);
			return;
		}

		File file = temporaryFiles.createNewTempFile("extracted-emails", ".csv.gz").toFile();
		File readFile = new File(fileLocation);

		Instant start = Instant.now();
		if (unknownColumn){
			guessProcess(file, readFile);
		}
		else {
			batchProcess(file, readFile);
		}

		Instant end = Instant.now();
		log.info("Time Taken: " + Duration.between(start, end));
	}

	private void batchProcess(File file, File readFile) {
		List<String> extractedEmails = new ArrayList<>(10_000);
		try (CSVWriter csvWriter = csvUtils.createWriterGz(file)) {
			int rows = 0;
			int emails = 0;
			try (CSVReader csvReader = csvUtils.createReader(readFile)) {
				String[] line;
				while ((line = csvReader.readNext()) != null) {
					rows++;

					String lineRecord = line[columnNumber];
					if(!Strings.isNullOrEmpty(lineRecord)) {
						boolean exists = duplicateCacheService.recordExist(
							hash(lineRecord)
						);

						if(!exists) {
							emails++;
							extractedEmails.add(lineRecord);
						}
					}

					if(extractedEmails.size() % 10_000 == 0) {
						log.info("Rows Scanned: " + rows);
						csvUtils.writeRowData(csvWriter, extractedEmails);
						extractedEmails = new ArrayList<>();
					}
				}
			}

			if(!extractedEmails.isEmpty()) {
				log.info("Writing last batch of data.");
				csvUtils.writeRowData(csvWriter, extractedEmails);
			}

			log.info("Total Rows scanned: " + rows);
			log.info("Total Emails Extracted: " + emails);
			log.info("New file location: " + file.toString());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void guessProcess(File file, File readFile) {
		List<String> extractedEmails = new ArrayList<>(10_000);
		try (CSVWriter csvWriter = csvUtils.createWriterGz(file)) {
			int rows = 0;
			int emails = 0;

			try (CSVReader csvReader = csvUtils.createReader(readFile)) {
				String[] line;
				while ((line = csvReader.readNext()) != null) {
					rows++;

					// iterate entire row, and find all emails
					for(String lineRecord: line) {
						if(shouldExtract(lineRecord)) {
							lineRecord = cleanUtil.aliasRemoval(lineRecord);
							boolean exists = duplicateCacheService.recordExist(
								hash(lineRecord)
							);

							if(!exists) {
								emails++;
								extractedEmails.add(lineRecord);
							}
						}
					}

					if(extractedEmails.size() % 10_000 == 0) {
						log.info("Rows Scanned: " + rows);
						csvUtils.writeRowData(csvWriter, extractedEmails);
						extractedEmails = new ArrayList<>();
					}
				}
			}

			if(!extractedEmails.isEmpty()) {
				log.info("Writing last batch of data.");
				csvUtils.writeRowData(csvWriter, extractedEmails);
			}

			log.info("Total Rows scanned: " + rows);
			log.info("Total Emails Extracted: " + emails);
			log.info("New file location: " + file.toString());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean shouldExtract(String data) {
		return data.contains("@");
	}

	public String hash(String record) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		return Hex.encodeHexString(md.digest(record.getBytes()));
	}
}
