package com.ge.predix.solsvc.edgestarter.model;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * Resources : read resources properties from TemplateProcessorResources.
 */
public class TemplateProcessorResources
{
  private static final String BUNDLE = "TemplateProcessorResources";
  private static Logger _logger = LoggerFactory.getLogger(TemplateProcessorResources.class);
  private static TemplateProcessorResources _resource;
  private ResourceBundle resources;
  
  protected TemplateProcessorResources()
  {
    try
    {
      this.resources = ResourceBundle.getBundle(BUNDLE);
    }
    catch (MissingResourceException ee)
    {
      _logger.error("Missing Resource Bundle CloudGatewayResources", ee);
    }
  }
  
  public static TemplateProcessorResources getInstance()
  {
    if (_resource == null) {
      _resource = new TemplateProcessorResources();
    }
    return _resource;
  }
  
  public ResourceBundle getResourceBundle()
  {
    return this.resources;
  }
  
  public String getString(String key)
  {
    if (this.resources == null)
    {
      _logger.error("Missing Resource Bundle TemplateProcessorResources");
      return key;
    }
    try
    {
      return this.resources.getString(key);
    }
    catch (MissingResourceException ee)
    {
      _logger.warn("Missing resource key=" + key);
    }
    return key;
  }
  
  public String getString(String key, Object... args)
  {
    MessageFormat formatter = new MessageFormat(getString(key));
    return formatter.format(args);
  }
}
