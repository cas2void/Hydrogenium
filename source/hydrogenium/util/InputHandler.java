/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.util;

import java.util.Scanner;

public class InputHandler implements Runnable
{
    InputListener listener;

    public InputHandler(InputListener listener)
    {
        this.listener = listener;
    }

    public void run()
    {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine())
        {
            String line = sc.nextLine();
            if (line.startsWith("q") || line.startsWith("Q"))
            {
                listener.shutdown();
                while (!listener.isDown())
                {
                    Thread.yield();
                }
                break;
            }
            else
            {
                listener.gotLine(line);
            }

            Thread.yield();
        }

        // we're here because users press 'q', 
        // asuming other resource has been released with listener.shutdown()
        Debug.println("All cleaned up.");
        System.exit(0);
    }
}
