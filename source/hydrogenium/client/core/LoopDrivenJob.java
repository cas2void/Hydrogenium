/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.client.core;

import hydrogenium.util.LoopJobFramework;

public class LoopDrivenJob extends LoopJobFramework
{
    ClientCore core;

    public LoopDrivenJob(ClientCore core, int fps)
    {
        super(fps, "LoopDrivenJob", true);
        this.core = core;
    }
    
    protected void update()
    {
        core.updateCore();
    }

    protected void comply()
    {
        core.complyCore();
    }
}
