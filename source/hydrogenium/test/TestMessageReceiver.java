/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.test;

import java.net.Socket;
import hydrogenium.util.InputHandler;
import hydrogenium.util.InputListener;
import hydrogenium.util.MessageReceiver;

public class TestMessageReceiver implements InputListener
{
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_FPS = 60;

    WorkHorseMessageReceiver workHorse;

    public TestMessageReceiver(int port)
    {
        InputHandler input = new InputHandler(this);
        new Thread(input).start();

        workHorse = new WorkHorseMessageReceiver(port);
        new Thread(workHorse).start();
    }

    public void shutdown()
    {
        workHorse.shutdown();
    }

    public boolean isDown()
    {
        return workHorse.isDown();
    }

    public void gotLine(String line)
    {
        if (line.startsWith("g"))
        {
            System.out.println(workHorse.getMessage());
        }
        else if (line.startsWith("p"))
        {
            System.out.println(workHorse.pickMessage("heartbeat"));
        }
        else if (line.startsWith("e"))
        {
            System.out.println(workHorse.excludeMessage("heartbeat"));
        }
    }

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT;
        int fps = DEFAULT_FPS;

        // Collect command line args
        for (int i = 0; i < args.length; i++)
        {
/*
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
*/
        }

        new TestMessageReceiver(port);
    }
}

class WorkHorseMessageReceiver extends DemoRequestListener
{
    MessageReceiver receiver;

    public WorkHorseMessageReceiver(int port)
    {
        super(port);
    }

    public void comply(Socket socket)
    {
        receiver = new MessageReceiver(socket);
        new Thread(receiver).start();
    }

    public String getMessage()
    {
        return receiver.getMessage();
    }

    public boolean pickMessage(String message)
    {
        return receiver.pickMessage(message);
    }

    public String excludeMessage(String message)
    {
        return receiver.excludeMessage(message);
    }
}
