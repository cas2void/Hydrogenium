/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.console;

import hydrogenium.util.InputHandler;
import hydrogenium.util.InputListener;

public class HydrogeniumClientConsole implements InputListener
{
    private static final int DEFAULT_FPS = 60;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5000;

    TestClientCore client;

    public HydrogeniumClientConsole(boolean standallone, int fps, String host, int port, int id)
    {
        InputHandler input = new InputHandler(this);
        new Thread(input).start();

        if (standallone)
        {
            client = new TestClientCore(fps);
        }
        else
        {
            client = new TestClientCore(host, port, id);
        }
        client.start();
    }

    public void shutdown()
    {
        client.shutdown();
    }

    public boolean isDown()
    {
        return client.isDown();
    }

    public void gotLine(String line)
    {
    }

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        String host = DEFAULT_HOST;
        int fps = DEFAULT_FPS;
        int port = DEFAULT_PORT;

        boolean standallone = true;
        int id = 1;
        boolean reset = false;

        // Collect command line args
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].contains("-multiscreen"))
            {
                standallone = false;
            } 
            else if (args[i].contains("-framerate"))
            {
                fps = Integer.parseInt(args[i].substring(10));
            } 
            else if (args[i].contains("-host"))
            {
                host = args[i].substring(5);
            } 
            else if (args[i].contains("-port"))
            {
                port = Integer.parseInt(args[i].substring(5));
            }
            else if (args[i].contains("-id"))
            {
                id = Integer.parseInt(args[i].substring(3));
            }
            else if (args[i].contains("-reset"))
            {
                reset = true;
            }
            else
            {
                printHelpMessage();
            }
        }

        new HydrogeniumClientConsole(standallone, fps, host, port, id);
    }
}
