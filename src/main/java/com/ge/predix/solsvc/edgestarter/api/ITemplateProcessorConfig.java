package com.ge.predix.solsvc.edgestarter.api;

public interface ITemplateProcessorConfig {
	/*
	 * Key to read device get URL
	 */
	public static final String PROPKEY_PREDIXKIT_GETDEVICE_URL = "com.ge.predix.solsvc.edgestarter.predixkit.device.get.url";
	/*
	 * Key to read default device get URL
	 */
	public static final String PROPKEY_PREDIXKIT_GETDEVICE_DEFAULT_URL = "com.ge.predix.solsvc.edgestarter.predixkit.device.get.default.url";
	/*
	 * Key to sleep before calling the getDevice API
	 */
	public static final String PROPKEY_EXPIRATION_CHECK_INTERVAL = "com.ge.predix.solsvc.edgestarter.task.interval";
	/*
	 * Key to get Status code for expired device
	 */
	public static final String PROPKEY_EXPIRED_STATUS_CODE = "com.ge.predix.solsvc.edgestarter.expired.statuscode";
	
	/*
	 *Return the default Predix Kit Get Device URL 
	 */
	public String getDefaultPredixKitGetDeviceURL();
	/*
	 *Return the Predix Kit Get Device URL 
	 */
	public String getPredixKitGetDeviceURL();
	/*
	 *Return the time interval to sleep before calling the getDevice API 
	 */
	public int getTaskInterval();
	/*
	 *Return the Status Code for Device expired
	 */
	public Integer getExpiredStatusCode();
}
