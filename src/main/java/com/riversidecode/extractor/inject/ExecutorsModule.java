package com.riversidecode.extractor.inject;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 */
@Module
public class ExecutorsModule {
	@Provides
	@Singleton
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newScheduledThreadPool(2, new ThreadFactoryBuilder()
			.setDaemon(true)
			.setNameFormat("Scheduled-%d")
			.build()
		);
	}

	@Provides
	@Singleton
	public ExecutorService executorService(){
		return Executors.newFixedThreadPool(4, new ThreadFactoryBuilder()
			.setDaemon(true)
			.setNameFormat("Service-%d")
			.build()
		);
	}
}
