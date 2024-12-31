package net.mrbt0907.configex;

import java.util.LinkedHashMap;
import java.util.Map;

import net.mrbt0907.configex.api.IConfigEX;
import net.mrbt0907.configex.manager.ConfigInstance;

/*TODO:
 * - Configurator uses minecraft's system
 * - ServerSide: server config -> (If configurating in menu: add default config)
 * - ClientSide: client config
 * - No Annotation: common config
 * 
 * */
public class ConfigManager
{
	private static final Map<String, ConfigInstance> configs = new LinkedHashMap<String, ConfigInstance>();
	
	public static void register(IConfigEX config)
	{
		ConfigInstance instance = new ConfigInstance(config);
		configs.put(instance.registryName, instance);
	}
}