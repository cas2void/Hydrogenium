/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.server.console;

import hydrogenium.util.InputHandler;
import hydrogenium.util.InputListener;
import hydrogenium.server.core.ServerCore;

public class HydrogeniumServerConsole implements InputListener
{
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_FPS = 60;

    ServerCore server;

    public HydrogeniumServerConsole(int port, int fps, boolean reset)
    {
        InputHandler input = new InputHandler(this);
        new Thread(input).start();

        server = new ServerCore(port, fps, reset);
        new Thread(server).start();
    }

    public void shutdown()
    {
        server.shutdown();
    }

    public boolean isDown()
    {
        return server.isDown();
    }

    public void gotLine(String line)
    {
        if (line.startsWith("l"))
        {
            server.printClientList();
        }
        else if (line.startsWith("p"))
        {
            server.pause();
        }
        else if (line.startsWith("r"))
        {
            server.resume();
        }
    }

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT;
        int fps = DEFAULT_FPS;
        boolean reset = false;

        // Collect command line args
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].contains("-framerate"))
            {
                fps = Integer.parseInt(args[i].substring(10));
            } 
            else if (args[i].contains("-port"))
            {
                port = Integer.parseInt(args[i].substring(5));
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

        new HydrogeniumServerConsole(port, fps, reset);
    }
}
