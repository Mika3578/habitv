package com.dabi.habitv.plugintester;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.dabi.habitv.api.plugin.api.UpdatablePluginInterface;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.api.plugin.pub.Subscriber;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent;

public class BasePluginUpdateTester implements Subscriber<UpdatablePluginEvent>  {

	private static final Logger LOG = Logger.getLogger(BasePluginUpdateTester.class);
	private DownloaderPluginHolder downloaders;
	private Publisher<UpdatablePluginEvent> publisher;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	public BasePluginUpdateTester() {
		super();
	}

	@Before
	public void setUp() throws Exception {
		downloaders = new DownloaderPluginHolder("", null, null, "downloads", "index", "bin", "plugins");
		publisher = new Publisher<>();
		publisher.attach(this);
	}

	@After
	public void tearDown() throws Exception {
	
	}

	protected void testUpdatablePlugin(final Class<? extends UpdatablePluginInterface> class1) throws InstantiationException, IllegalAccessException, ReflectiveOperationException {
		final UpdatablePluginInterface updatablePlugin = class1.getDeclaredConstructor().newInstance();
		updatablePlugin.update(publisher, downloaders);
	}

	@Override
	public void update(final UpdatablePluginEvent event) {
		LOG.info(event);
	}
} 