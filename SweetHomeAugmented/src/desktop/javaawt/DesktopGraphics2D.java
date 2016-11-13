package desktop.javaawt;

import java.awt.image.BufferedImageOp;

import javaawt.Graphics2D;
import javaawt.image.BufferedImage;

public class DesktopGraphics2D implements Graphics2D
{
	private java.awt.Graphics2D delegate = null;

	public DesktopGraphics2D(java.awt.Graphics2D delegate)
	{
		this.delegate = delegate;
	}

	public Object getDelegate()
	{
		return delegate;
	}

	public void dispose()
	{
		delegate.dispose();
	}

	public void drawImage(BufferedImage image, Object object, int i, int j)
	{
		delegate.drawImage((java.awt.image.BufferedImage)image.getDelegate(), (BufferedImageOp) object, i, j);
	}

}
