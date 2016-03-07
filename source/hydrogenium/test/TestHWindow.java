/**
 * Hydrogenium
 *
 * Copyright (c) 2013 Ean Jee
 *
 * @author Ean Jee (yanjieanjee@gmail.com)
 */

package hydrogenium.test;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;
import hydrogenium.client.gui.HWindow;
import java.io.InputStream;
import java.io.IOException;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL2.*;

public class TestHWindow extends HWindow
{
    private static final String DEFAULT_CONFIG_FILE = "client.xml";

    // rotational angle for x-axis in degree
    private float angleX = 0.0f;
    // rotational angle for y-axis in degree
    private float angleY = 0.0f;
    // z-location
    private float z = -6.0f;
    // rotational speed for x-axis
    private float rotateSpeedX = 0.2f;
    // rotational speed for y-axis
    private float rotateSpeedY = 0.2f;

    private Texture tex;

    // Texture image flips vertically. Shall use TextureCoords class to retrieve the
    // top, bottom, left and right coordinates.
    private float textureTop, textureBottom, textureLeft, textureRight;

    public TestHWindow(InputStream is)
    {
        super(is);
    }

    protected void prepare(GL2 gl)
    {
        InputStream img = this.getClass().getResourceAsStream("img/glass.png");
        try
        {
            tex = TextureIO.newTexture(img, false, "png");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (GLException e)
        {
            e.printStackTrace();
        }

        // Linear filter is more compute-intensive
        // Use linear filter if image is larger than the original texture
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // Use linear filter if image is smaller than the original texture
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        // Get the top and bottom coordinates of the textures. Image flips vertically.
        TextureCoords textureCoords;
        textureCoords = tex.getImageTexCoords();
        textureTop = textureCoords.top();
        textureBottom = textureCoords.bottom();
        textureLeft = textureCoords.left();
        textureRight = textureCoords.right();

        // Enable Texture Mapping
        gl.glEnable(GL_TEXTURE_2D);
        // Enable Smooth Shading
	    gl.glShadeModel(GL_SMOOTH);
        // Black Background
	    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
        // Depth Buffer Setup
	    gl.glClearDepth(1.0f);
        // Enables Depth Testing
        //gl.glEnable(GL_DEPTH_TEST);
        // The Type Of Depth Testing To Do
	    gl.glDepthFunc(GL_LEQUAL);
	    // Really Nice Perspective Calculations
	    gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        // Blending control
        // Full Brightness with specific alpha (1 for opaque, 0 for transparent)
        gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
        // Used blending function based On source alpha value
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        gl.glEnable(GL_BLEND);

        gl.glDisable(GL_LIGHTING);
    }

    protected void update()
    {
        // Update the rotational position after each refresh.
        angleX += rotateSpeedX;
        angleY += rotateSpeedY;
    }

    protected void render(GL2 gl, GLU glu)
    {
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers

        // ------ Render a Cube with texture ------
        gl.glLoadIdentity();                    // reset model-view matrix
        gl.glTranslatef(0.0f, 0.0f, z);         // translate into the screen
        gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f); // rotate about the x-axis
        gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f); // rotate about the y-axis

        // Enables this texture's target (e.g., GL_TEXTURE_2D) in the current GL
        // context's state.
        tex.enable(gl);
        // Bind the texture with the currently chosen filter to the current OpenGL
        // graphics context.
        tex.bind(gl);

        // Turn blending on
        gl.glEnable(GL_BLEND);
        // Turn depth testing off
        gl.glDisable(GL_DEPTH_TEST);

        gl.glBegin(GL_QUADS); // of the color cube

        // Front Face
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f); // bottom-left of the texture and quad
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);  // bottom-right of the texture and quad
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);   // top-right of the texture and quad
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);  // top-left of the texture and quad

        // Back Face
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
      
        // Top Face
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
      
        // Bottom Face
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
      
        // Right face
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
      
        // Left Face
        gl.glTexCoord2f(textureLeft, textureBottom);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(textureRight, textureBottom);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glTexCoord2f(textureRight, textureTop);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glTexCoord2f(textureLeft, textureTop);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);

        gl.glEnd();
    }

    protected void reset()
    {
        angleX = 0.0f;
        angleY = 0.0f;
    }

    protected void cleanUp()
    {
        System.out.println("HWindow.cleanUp()");
    }

    protected boolean isClean()
    {
        return true;
    }

    private static void printHelpMessage()
    {}

    public static void main(String[] args)
    {
        String configFile = DEFAULT_CONFIG_FILE;

        // Collect command line args
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].contains("-C"))
            {
                configFile = args[i].substring(2);
            } 
            else
            {
                printHelpMessage();
            }
        }

        new TestHWindow(TestHWindow.class.getResourceAsStream("config/" + configFile));
    }
}
