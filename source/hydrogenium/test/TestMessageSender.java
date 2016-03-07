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
import hydrogenium.util.MessageSender;

public class TestMessageSender implements InputListener
{
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_FPS = 60;

    WorkHorseMessageSender workHorse;

    public TestMessageSender(int port)
    {
        InputHandler input = new InputHandler(this);
        new Thread(input).start();

        workHorse = new WorkHorseMessageSender(port);
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
        workHorse.sendMessage(line);
    }

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT;
        int fps = DEFAULT_FPS;

        new TestMessageSender(port);
    }
}

class WorkHorseMessageSender extends DemoRequestListener
{
    MessageSender sender;

    public WorkHorseMessageSender(int port)
    {
        super(port);
    }

    public void comply(Socket socket)
    {
        sender = new MessageSender(socket);
        new Thread(sender).start();
    }

    public void sendMessage(String message)
    {
        sender.sendMessage(message);
    }
}

