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
import hydrogenium.util.ConnectionHandler;
import hydrogenium.util.ConnectionWatcher;
import hydrogenium.util.MessageReceiver;
import hydrogenium.util.MessageSender;

public class TestConnectionWatcher implements InputListener
{
    private static final int DEFAULT_PORT = 5000;
    private static final int DEFAULT_FPS = 60;

    WorkHorseConnectionWatcher workHorse;

    public TestConnectionWatcher(int port)
    {
        InputHandler input = new InputHandler(this);
        new Thread(input).start();

        workHorse = new WorkHorseConnectionWatcher(port);
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

        }
        else if (line.startsWith("p"))
        {

        }
    }

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT;
        int fps = DEFAULT_FPS;

        new TestConnectionWatcher(port);
    }
}

class WorkHorseConnectionWatcher extends DemoRequestListener implements ConnectionWatcher
{
    MessageReceiver receiver;
    MessageSender sender;

    ConnectionHandler connectionHandler;

    public WorkHorseConnectionWatcher(int port)
    {
        super(port);
    }

    public void comply(Socket socket)
    {
        receiver = new MessageReceiver(socket);
        new Thread(receiver).start();

        sender = new MessageSender(socket);
        new Thread(sender).start();

        connectionHandler = new ConnectionHandler(this, receiver, sender);
        new Thread(connectionHandler).start();
    }

    public String getMessage()
    {
        return receiver.getMessage();
    }

    public boolean pickMessage(String message)
    {
        return receiver.pickMessage(message);
    }

    public void sendMessage(String message)
    {
        sender.sendMessage(message);
    }

    public void shutdown()
    {
        super.shutdown();
        receiver.shutdown();
        sender.shutdown();
        connectionHandler.shutdown();
    }

    public boolean isDown()
    {
        return super.isDown() && receiver.isDown() && sender.isDown() && connectionHandler.isDown();
    }

    public void connectionLost()
    {
        System.out.println("WorkHorse.connectionLost()");
    }
}
