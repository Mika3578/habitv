package com.dabi.habitv.plugintester;

import org.apache.log4j.Logger;

import com.dabi.habitv.api.plugin.api.UpdatablePluginInterface;
import com.dabi.habitv.api.plugin.holder.DownloaderPluginHolder;
import com.dabi.habitv.api.plugin.pub.Publisher;
import com.dabi.habitv.api.plugin.pub.Subscriber;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent;

public class BasePluginUpdateTester implements Subscriber<UpdatablePluginEvent>  {

	private static final Logger LOG = Logger.getLogger(BasePluginUpdateTester.class);
	private DownloaderPluginHolder downloaders;
	private Publisher<UpdatablePluginEvent> publisher;

	public static void setUpBeforeClass() {
	}

	public static void tearDownAfterClass() {
	}

	public BasePluginUpdateTester() {
		super();
	}

	public void setUp() {
		downloaders = new DownloaderPluginHolder("", null, null, "downloads", "index", "bin", "plugins");
		publisher = new Publisher<>();
		publisher.attach(this);
	}

	public void tearDown() {

	}

	protected void testUpdatablePlugin(final Class<? extends UpdatablePluginInterface> class1) throws ReflectiveOperationException {
		final UpdatablePluginInterface updatablePlugin = class1.getDeclaredConstructor().newInstance();
		updatablePlugin.update(publisher, downloaders);
	}

	@Override
	public void update(final UpdatablePluginEvent event) {
		LOG.info(event);
	}
} 