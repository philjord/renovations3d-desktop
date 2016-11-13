package desktop.javaawt.imageio;

import java.io.IOException;
import java.io.InputStream;

import desktop.javaawt.image.DesktopBufferedImage;
import javaawt.image.BufferedImage;
import javaawt.imageio.ImageIO;

public class DesktopImageIO extends ImageIO
{

	public static BufferedImage read(InputStream in)
	{
		try
		{
			return new DesktopBufferedImage(javax.imageio.ImageIO.read(in));
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

}
