package desktop.javaawt.image;

import javaawt.image.ImageProducer;

public class DesktopImageProducer implements ImageProducer
{
	private java.awt.image.ImageProducer delegate = null;

	public DesktopImageProducer(java.awt.image.ImageProducer delegate)
	{
		this.delegate = delegate;
	}
	
	public java.awt.image.ImageProducer getDelegate()
	{
		return delegate;
	}
}
