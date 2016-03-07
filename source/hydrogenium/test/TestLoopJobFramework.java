/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.test;

import hydrogenium.util.InputHandler;
import hydrogenium.util.InputListener;
import hydrogenium.util.LoopJobFramework;

class LoopJob extends LoopJobFramework
{
    public LoopJob(int fps)
    {
        super(fps, "LoopJob", true, true);
    }

    protected void comply()
    {
        //System.out.println("LoopJob.comply()");
    }
}

public class TestLoopJobFramework implements InputListener
{
    LoopJob loopJob;

    public TestLoopJobFramework(int fps)
    {
        loopJob = new LoopJob(fps);
        new Thread(loopJob).start();

        InputHandler input = new InputHandler(this);
        new Thread(input).start();
    }

    public void shutdown()
    {
        loopJob.shutdown();
    }

    public boolean isDown()
    {
        return loopJob.isDown();
    }

    public void gotLine(String line)
    {
        if (line.startsWith("r"))
        {
            loopJob.resume();
        }
        else if (line.startsWith("p"))
        {
            loopJob.pause();
        }
    }

    public static void main(String[] args)
    {
        new TestLoopJobFramework(100);
    }
}
