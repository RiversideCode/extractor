package com.riversidecode.extractor;

import com.riversidecode.extractor.utils.CleanUtil;
import dagger.Component;
import dagger.Module;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CleanUtilTest {
	@Module
	public static class TestModule {
	}

	@Component(modules = {
		TestModule.class
	})
	@Singleton
	public interface TestComponent {
		void inject(CleanUtilTest obj);
	}

	@Inject
	CleanUtil cleanUtil;

	@BeforeEach
	public void before() {
		DaggerCleanUtilTest_TestComponent.create().inject(this);
	}

	@Test
	@SneakyThrows
	public void shouldDoSomething() throws Exception {
		String fix = cleanUtil.aliasRemoval("morty+asdf@gmail.com");

		assertEquals("morty@gmail.com", fix);
	}
}