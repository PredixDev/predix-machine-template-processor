package com.ge.predix.solsvc.edgestarter.model;
/*
 * DeviceConfig object representing config data in the get device response.
 */
public class DeviceConfig {
	private String predixUaaIssuer;
	private String client;
	private String predixTimeSeriesIngestUri;
	private String predixTimeSeriesZoneid;
	private String deviceDeactivationPeriod;
	private String cloudApplicationUrl;
	public String getPredixUaaIssuer() {
		return predixUaaIssuer;
	}
	public void setPredixUaaIssuer(String predixUaaIssuer) {
		this.predixUaaIssuer = predixUaaIssuer;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getPredixTimeSeriesIngestUri() {
		return predixTimeSeriesIngestUri;
	}
	public void setPredixTimeSeriesIngestUri(String predixTimeSeriesIngestUri) {
		this.predixTimeSeriesIngestUri = predixTimeSeriesIngestUri;
	}
	public String getPredixTimeSeriesZoneid() {
		return predixTimeSeriesZoneid;
	}
	public void setPredixTimeSeriesZoneid(String predixTimeSeriesZoneid) {
		this.predixTimeSeriesZoneid = predixTimeSeriesZoneid;
	}
	public String getDeviceDeactivationPeriod() {
		return deviceDeactivationPeriod;
	}
	public void setDeviceDeactivationPeriod(String deviceDeactivationPeriod) {
		this.deviceDeactivationPeriod = deviceDeactivationPeriod;
	}
	public String getCloudApplicationUrl() {
		return cloudApplicationUrl;
	}
	public void setCloudApplicationUrl(String cloudApplicationUrl) {
		this.cloudApplicationUrl = cloudApplicationUrl;
	}
}
