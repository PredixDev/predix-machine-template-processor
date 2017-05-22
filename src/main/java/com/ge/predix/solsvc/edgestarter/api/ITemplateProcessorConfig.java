package com.ge.predix.solsvc.edgestarter.api;

public interface ITemplateProcessorConfig {
	public static final String PROPKEY_PREDIXKIT_GETDEVICE_URL = "com.ge.predix.solsvc.edgestarter.predixkit.device.get.url";	
	public static final String PROPKEY_EXPIRATION_CHECK_INTERVAL = "com.ge.predix.solsvc.edgestarter.task.interval";
	
	String getPredixKitGetDeviceURL();
	String getTaskInterval();
}
