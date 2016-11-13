package desktop.javaawt.image;

import javaawt.color.ColorSpace;
import javaawt.image.ColorModel;

public class DesktopColorModel implements ColorModel
{
	private java.awt.image.ColorModel delegate = null;

	public DesktopColorModel(java.awt.image.ColorModel delegate)
	{
		this.delegate = delegate;
	}

	public java.awt.image.ColorModel getDelegate()
	{
		return delegate;
	}

	public ColorSpace getColorSpace()
	{
		return new DesktopColorSpace(delegate.getColorSpace());
	}

	public boolean isAlphaPremultiplied()
	{
		return delegate.isAlphaPremultiplied();
	}

	public int getRed(Object pixel)
	{
		return delegate.getRed(pixel);
	}

	public int getGreen(Object pixel)
	{
		return delegate.getGreen(pixel);
	}

	public int getBlue(Object pixel)
	{
		return delegate.getBlue(pixel);
	}

	public int getAlpha(Object pixel)
	{
		return delegate.getAlpha(pixel);
	}
}
