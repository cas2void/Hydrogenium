/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.util;

public class Debug
{
    public static final boolean ENABLED = true;
    public static final boolean FPS = false;

    public static final boolean STATS = true;

    public static void println(String line)
    {
        if (ENABLED)
        {
            System.out.println(line);
        }
    }

    public static void format(String format, Object... args)
    {
        if (ENABLED)
        {
            System.out.format(format, args);
        }
    }
}
