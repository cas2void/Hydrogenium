/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.util;

public interface InputListener
{
    abstract public void shutdown();
    abstract public boolean isDown();
    abstract public void gotLine(String line);
}
