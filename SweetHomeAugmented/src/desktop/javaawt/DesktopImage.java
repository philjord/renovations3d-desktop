package desktop.javaawt;

import desktop.javaawt.image.DesktopImageProducer;
import javaawt.Graphics;
import javaawt.Image;
import javaawt.image.ImageObserver;
import javaawt.image.ImageProducer;

public class DesktopImage extends Image
{
	private java.awt.Image delegate = null;

	public DesktopImage(java.awt.Image delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public Object getDelegate()
	{
		return delegate;
	}
	
	@Override
	public void flush()
	{
		delegate.flush();
	}

	@Override
	public float getAccelerationPriority()
	{
		return delegate.getAccelerationPriority();
	}

	@Override
	public Graphics getGraphics()
	{
		return new DesktopGraphics(delegate.getGraphics());
	}

	@Override
	public int getHeight(ImageObserver observer)
	{
		return delegate.getHeight((java.awt.image.ImageObserver) observer.getDelegate());
	}

	@Override
	public Object getProperty(String name, ImageObserver observer)
	{
		return delegate.getProperty(name, (java.awt.image.ImageObserver) observer.getDelegate());
	}

	@Override
	public Image getScaledInstance(int width, int height, int hints)
	{
		return new DesktopImage(delegate.getScaledInstance(width, height, hints));
	}

	@Override
	public ImageProducer getSource()
	{
		return new DesktopImageProducer(delegate.getSource());
	}

	@Override
	public int getWidth(ImageObserver observer)
	{
		return delegate.getWidth((java.awt.image.ImageObserver) observer.getDelegate());
	}

	@Override
	public void setAccelerationPriority(float priority)
	{
		delegate.setAccelerationPriority(priority);
	}

}
