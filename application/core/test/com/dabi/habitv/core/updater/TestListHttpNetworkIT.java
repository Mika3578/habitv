package com.dabi.habitv.core.updater;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dabi.habitv.framework.plugin.utils.update.FindArtifactUtils;

public class TestListHttp {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void test() {
		final String groupId = "com.dabi.habitv";
		final String artifactId = "framework";
		final String coreVersion = "3.7.3";
		final boolean autoriseSnapshot = false;

		System.out.println(FindArtifactUtils.findLastVersionUrl(groupId, artifactId, coreVersion, autoriseSnapshot, ".jar"));
	}

}
