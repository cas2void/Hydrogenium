/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.test;

import hydrogenium.client.gui.Config;
import java.io.InputStream;

public class TestConfig
{
    //private static final String DEFAULT_CONFIG_FILE = "hydrogenium/test/config/client.xml";
    private static final String DEFAULT_CONFIG_FILE = "config/client.xml";

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        String configFile = DEFAULT_CONFIG_FILE;

        // Collect command line args
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].contains("-C"))
            {
                configFile = args[i].substring(2);
            } 
            else
            {
                printHelpMessage();
            }
        }

        Config config = new Config();
        //config.load(TestConfig.class.getClassLoader().getResourceAsStream(configFile));
        config.load(TestConfig.class.getResourceAsStream(configFile));
    }
}
