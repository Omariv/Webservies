/**
 * 
 */
package com.alstom.plm4aproxy.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class manage configuration for PLM4AProxy WebApps project
 * @author Accenture
 * @version 1.0
 */
public class ConfigUtils {
	
	private Properties props;
	private static ConfigUtils instance;
	
	/**
	 * Private class constructor
	 */
	private ConfigUtils() {
		//TODO : To be reworked
		try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("config.properties")) {

            props = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            //load a properties file from class path, inside static method
            props.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	/**
	 * This method give access to as single instance of the class
	 * @return Instance of class
	 */
	public static ConfigUtils getInstance() {
		if(instance==null) {
			instance=new ConfigUtils();
		}
		return instance;
	}
	
	/**
	 * This method return the property value associated to the key
	 * @param key Property key to look for
	 * @return Value associated to the key
	 */
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	/**
	 * This method return loaded properties
	 * @return Loaded Properties
	 */
	public Properties getProperties() {
		return props;
	}

}
