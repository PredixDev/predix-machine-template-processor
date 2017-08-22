package com.ge.predix.solsvc.edgestarter.impl;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.predix.solsvc.edgestarter.api.ITemplateProcessorConfig;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;
import aQute.configurable.Config;

@Component(immediate=true,name=TemplateProcessorConfigImpl.SERVICE_PID,
	designateFactory=Config.class,
	configurationPolicy=ConfigurationPolicy.require,
	provide=ITemplateProcessorConfig.class
)
public class TemplateProcessorConfigImpl implements ITemplateProcessorConfig {
	
	private static Logger _logger = LoggerFactory.getLogger(TemplateProcessorConfigImpl.class);
	
	private String defaultPredixKitGetDeviceURL;
	private String predixKitGetDeviceURL;
	private int taskInterval;
	private Integer expiredStatusCode;
	
	/** Service PID for Sample Machine Adapter */
	public static final String SERVICE_PID = "com.ge.predix.solsvc.edgestarter.processor.config"; //$NON-NLS-1$

	@Activate
	public void activate(ComponentContext ctx) {
		Config config = Configurable.createConfigurable(Config.class, ctx.getProperties());
		this.defaultPredixKitGetDeviceURL = config.getDefaultPredixKitGetDeviceURL();
		this.predixKitGetDeviceURL = config.getPredixKitGetDeviceURL();
		if (config.getTaskInterval() != null && !"".equals(config.getTaskInterval())) {
			this.taskInterval = new Integer(config.getTaskInterval());
		}
		if (config.getExpiredStatusCode() != null && !"".equals(config.getExpiredStatusCode())) {
			this.expiredStatusCode = new Integer(config.getExpiredStatusCode());
		}
		_logger.info("Starting TemplateProcessorConfigImpl : " + SERVICE_PID); //$NON-NLS-1$	
	}
	
	

	// Meta mapping for configuration properties
	@Meta.OCD(name="%component.name", factory=true, localization="OSGI-INF/l10n/bundle")
	interface Config {
		@Meta.AD(name = "%predix.kit.getdevice.default.url.name", description = "%predix.kit,getdevice.default.url.description", id = PROPKEY_PREDIXKIT_GETDEVICE_DEFAULT_URL, required = false, deflt = "")
		String getDefaultPredixKitGetDeviceURL();
		
		@Meta.AD(name = "%predix.kit.getdevice.url.name", description = "%predix.kit,getdevice.url.description", id = PROPKEY_PREDIXKIT_GETDEVICE_URL, required = false, deflt = "")
		String getPredixKitGetDeviceURL();
		
		@Meta.AD(name = "%predix.kit.task.interval.name", description = "%predix.kit.task.interval.description", id = PROPKEY_EXPIRATION_CHECK_INTERVAL, required = false, deflt = "")
		String getTaskInterval();

		@Meta.AD(name = "%predix.kit.expired.statuscode.name", description = "%predix.kit.expired.statuscode.description", id = PROPKEY_EXPIRED_STATUS_CODE, required = false, deflt = "")
		String getExpiredStatusCode();
	}



	@Override
	public String getPredixKitGetDeviceURL() {
		return this.predixKitGetDeviceURL;
	}



	@Override
	public int getTaskInterval() {
		return this.taskInterval;
	}



	@Override
	public String getDefaultPredixKitGetDeviceURL() {
		return this.defaultPredixKitGetDeviceURL;
	}



	@Override
	public Integer getExpiredStatusCode() {
		return this.expiredStatusCode;
	}

}
