package com.riversidecode.extractor.inject;

import com.riversidecode.extractor.Extractor;
import dagger.Component;

import javax.inject.Singleton;

@Component(modules = {
	ExecutorsModule.class,
	JacksonModule.class
})
@Singleton
public interface ExtractorComponent {
	Extractor exctractor();

	void inject (Extractor extractor);
}
