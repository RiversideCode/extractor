package com.riversidecode.extractor.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class DuplicateCacheService {
	protected Cache<String, Object> duplicateCache = CacheBuilder.newBuilder()
		.concurrencyLevel(Runtime.getRuntime().availableProcessors())
		.initialCapacity(1 << 20)
		.maximumSize(1 << 20)
		.expireAfterWrite(1, TimeUnit.HOURS)
		.build();
	private static final Object CACHE_OBJECT = new Object();

	@Inject
	public DuplicateCacheService() {
		// injection
	}

	public boolean recordExist(String cacheRecord) {
		if(duplicateCache.getIfPresent(cacheRecord) == null) {
			putRecord(cacheRecord);
			return false;
		}

		return true;
	}

	public void putRecord(String cacheRecord) {
		duplicateCache.put(cacheRecord, CACHE_OBJECT);
	}
}
