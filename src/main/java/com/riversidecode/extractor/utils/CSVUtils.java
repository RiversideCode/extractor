package com.riversidecode.extractor.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVParser;
import com.opencsv.RFC4180Parser;
import lombok.SneakyThrows;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Singleton
public class CSVUtils {
	@Inject
	public CSVUtils() {
		// injection
	}

	@SneakyThrows
	public CSVWriter createWriterGz(File file) {
		return new CSVWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file)), "UTF-8"));
	}

	@SneakyThrows
	public CSVWriter createWriter(File file) {
		FileWriter outputfile = new FileWriter(file);
		return new CSVWriter(outputfile);
	}

	@SneakyThrows
	public CSVReader createReader(File file) {
		ICSVParser parser = new RFC4180Parser();
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		return new CSVReaderBuilder(br)
			.withSkipLines(0)
			.withCSVParser(parser)
			.withKeepCarriageReturn(false)
			.build();
	}

	public void writeRowData(CSVWriter csvWriter, String record) {
		csvWriter.writeNext(new String[]{record});
	}

	public void writeRowData(CSVWriter csvWriter, List<String> records) {
		records
			.stream()
			.map(record -> new String[]{record})
			.forEach(csvWriter::writeNext);
	}
}
