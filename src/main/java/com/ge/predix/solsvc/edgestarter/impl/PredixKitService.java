package com.ge.predix.solsvc.edgestarter.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.http.HttpStatus;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.dspmicro.httpclient.api.HttpResponseWrapper;
import com.ge.dspmicro.httpclient.api.IHttpClient;
import com.ge.dspmicro.httpclient.api.IPredixCloudHttpClientFactory;
import com.ge.predix.solsvc.edgestarter.api.IPredixKitService;
import com.ge.predix.solsvc.edgestarter.api.ITemplateProcessorConfig;
import com.ge.predix.solsvc.edgestarter.model.TemplateProcessorResources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component(name = PredixKitService.SERVICE_PID, service = { IPredixKitService.class })
public class PredixKitService extends Thread implements IPredixKitService {

	private static final Logger _logger = LoggerFactory.getLogger(PredixKitService.class);

	private TemplateProcessorResources _resources = TemplateProcessorResources.getInstance();

	/** Service PID for Sample Machine Adapter */
	public static final String SERVICE_PID = "com.ge.predix.solsvc.edgestarter.kitservice"; //$NON-NLS-1$

	private IPredixCloudHttpClientFactory cloudHttpClientFactory;

	/**
	 * A reserved IHttpClient that supports Predix Cloud authenticated
	 * communication.
	 */
	private IHttpClient cloudHttpClient;

	private ITemplateProcessorConfig config;

	private volatile boolean isShutdown = false;
	private volatile boolean isRetryMode = false;

	private volatile int pollingInterval = 30;
	private volatile int currentInterval;
	private int retries = 0;
	private int maxRetries = 0;

	private ConfigurationAdmin configurationAdmin;

	/**
	 * OSGi component lifecycle activation method
	 *
	 * @param ctx
	 *            component context
	 * @throws IOException
	 *             on fail to load/set configuration properties
	 */
	@Activate
	public void activate(ComponentContext ctx) throws IOException {
		_logger.info("Starting PredixKitService " + SERVICE_PID); //$NON-NLS-1$
		this.currentInterval = this.config.getTaskInterval();
		this.start();
	}

	/**
	 * This method is called when the bundle is stopped.
	 *
	 * @param ctx
	 *            Component Context
	 */
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		_logger.info("Stopped sample for " + ctx.getBundleContext().getBundle().getSymbolicName()); //$NON-NLS-1$
		shutdown();
	}

	/**
	 * Dependency injection for IPredixCloudHttpClientFactory
	 *
	 * @param clientFactory
	 *            The IPredixCloudHttpClientFactory to inject
	 */
	@Reference
	public void setPredixCloudHttpClientFactory(IPredixCloudHttpClientFactory clientFactory) {
		this.cloudHttpClientFactory = clientFactory;
		try {
			this.cloudHttpClient = this.cloudHttpClientFactory.createPredixCloudHttpClient();
		} catch (ConfigurationException e) {
			//Dont throw else other features wont work
			_logger.error("Error occurred in creating authenticated HTTP client", e); //$NON-NLS-1$
		}
	}

	/**
	 * Clear the injected IPredixCloudHttpClientFactory
	 *
	 * @param clientFactory
	 *            The factory to clear.
	 */
	public void unsetPredixCloudHttpClientFactory(IPredixCloudHttpClientFactory clientFactory) {
		if (this.cloudHttpClientFactory == clientFactory) {
			this.cloudHttpClientFactory.deleteHttpClient(this.cloudHttpClient);
			this.cloudHttpClient = null;
			this.cloudHttpClientFactory = null;
		}
	}

	@Reference
	public void setConfig(ITemplateProcessorConfig config) {
		this.config = config;
	}

	public void validateDevice() {
		if (!isShutdown && this.cloudHttpClient != null && this.config.getPredixKitGetDeviceURL() != null
				&& !"".equals(this.config.getPredixKitGetDeviceURL())) {
				// Sending a GET request to the cloud.
			String url = null;
			HttpResponseWrapper httpResponse = null;
			String content = null;
			try {	
				url = this.config.getPredixKitGetDeviceURL()+"/device/"+InetAddress.getLocalHost().getHostName();
				_logger.info("Kit Device URL : " + url);
				URI getDeviceURL = new URI(url);

				httpResponse = this.cloudHttpClient.get(getDeviceURL);

				_logger.debug("GetDeviceResponse : " + httpResponse.getStatusCode());

				content = httpResponse.getContent();
				_logger.info("HTTP Status : "+httpResponse.getStatusCode()+" ExpiredStatus Code : "+config.getExpiredStatusCode().intValue());
				_logger.info("Response : " + content);
				if (!"".equals(content)) {
					JsonParser parser = new JsonParser();
					JsonObject obj = (JsonObject)parser.parse(content);
					
					switch (httpResponse.getStatusCode()) {
					case HttpStatus.SC_BAD_REQUEST :
						if (obj.get("status").getAsInt() == config.getExpiredStatusCode().intValue()){
							_logger.error("Error : "+obj.get("error").getAsString());
							_logger.error("Error Description : "+obj.get("message").getAsString());
							disconnectingMachineFromPredix();
						}
						break;
					case HttpStatus.SC_UNAUTHORIZED :
						_logger.error("Error : "+obj.get("error").getAsString());
						_logger.error("Error Description : "+obj.get("message").getAsString());
						break;
					default:
						_logger.error("Http Status code not handled : "+httpResponse.getStatusCode());
					}
					
					
				}
			} catch (IOException | InterruptedException | URISyntaxException e) {
				String msg = "Error occured while validating device. Url : "+url;
				if (httpResponse != null) {
					msg += "HttpStatus Code : "+httpResponse.getStatusCode();
				}
				msg += "Response Content : "+content;
				throw new RuntimeException(msg,e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public void run() {
		//Check if GetDeviceURL is set in the config
		if (this.config.getPredixKitGetDeviceURL() != null && !"".equals(this.config.getPredixKitGetDeviceURL())) {
			
		//put try catch here
		if (this.pollingInterval == 0) {
			shutdown();
			return;
		}
		if (_logger.isDebugEnabled()) {
			// _logger.debug(_resources.getString("GatewayThread.gateway_starting"));
		}
		_logger.info("Kit URL : " + this.config.getPredixKitGetDeviceURL());
		validateDevice();
		while (!this.isShutdown && this.config.getPredixKitGetDeviceURL() != null
				&& !"".equals(this.config.getPredixKitGetDeviceURL())) {
			try {
				sleep(this.currentInterval * 1000);
				validateDevice();
			} catch (InterruptedException e) {
				if (this.isRetryMode) {
					if (this.retries >= this.maxRetries) {
						if (_logger.isDebugEnabled()) {
							// _logger.debug(_resources.getString("GatewayThread.max.retries.reached"));
						}
						normalMode();
						continue;
					}
					this.retries += 1;
					if (_logger.isDebugEnabled()) {
						_logger.debug(_resources.getString("GatewayThread.retry.count",
								new Object[] { Integer.valueOf(this.retries), Integer.valueOf(this.maxRetries) }));
					}
				}
			}catch(Exception e){
				//Do not throw else the thread will exit
				e.printStackTrace();
				_logger.error("Error occured when validating device",e);
			}

		}
		if (_logger.isDebugEnabled()) {
			_logger.debug(_resources.getString("GatewayThread.gateway_stopping"));
		}
		}else {
			_logger.warn("com.ge.predix.solsvc.edgestarter.predixkit.device.get.url is not configured");
		}
		
	}

	private void normalMode() {
		if (this.isRetryMode) {
			this.isRetryMode = false;
			this.retries = 0;
			setCurrentInterval(this.pollingInterval);
			if (_logger.isDebugEnabled()) {
				_logger.debug(_resources.getString("GatewayThread.normal.mode"));
			}
		}
	}


	private void setCurrentInterval(int interval) {
		if (_logger.isDebugEnabled()) {
			_logger.debug(_resources.getString("GatewayThread.set.current.interval",
					new Object[] { Integer.valueOf(interval) }));
		}
		this.currentInterval = interval;
		interrupt();
	}

	private void shutdown() {
		this.isShutdown = true;
		interrupt();
	}

	private void disconnectingMachineFromPredix() {
		_logger.warn("Disconnecting .....");
		if (this.configurationAdmin != null) {
			try {
				Configuration[] configs = this.configurationAdmin.listConfigurations(null);
				for (Configuration config : configs) {

					if ("com.ge.dspmicro.websocketriver.send".equals(config.getFactoryPid())) {
						_logger.debug("factory : " + config.getFactoryPid());
						Dictionary<String, Object> props = config.getProperties();
						_logger.warn("About to remove com.ge.dspmicro.websocketriver.send.destination.url and com.ge.dspmicro.websocketriver.send.header.zone.value . props ="+props.toString());
						props.put("com.ge.dspmicro.websocketriver.send.destination.url", "");
						props.put("com.ge.dspmicro.websocketriver.send.header.zone.value", "");
						
						Hashtable<String, Object> properties = new Hashtable<String, Object>();
						Enumeration<String> keys = properties.keys();
						while (keys.hasMoreElements()) {
							String key = keys.nextElement();
							properties.put(key, props.get(key));
						}
						config.update(properties);
					}
				}
			} catch (IOException|InvalidSyntaxException e) {
				String msg = "Unable to disconnect Predix Machine from Predix";
				throw new RuntimeException(msg,e);
			}
		}
	}

	@Reference
	public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
		this.configurationAdmin = configurationAdmin;
	}

	@Override
	public Boolean isRegistered() {
		return this.config.getPredixKitGetDeviceURL() != null && !"".equals(this.config.getPredixKitGetDeviceURL());
	}
}
