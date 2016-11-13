package desktop.javaawt.image;

import javaawt.image.DataBuffer;

public abstract class DesktopDataBuffer implements DataBuffer
{
	private java.awt.image.DataBuffer delegate = null;

	public DesktopDataBuffer(java.awt.image.DataBuffer delegate)
	{
		this.delegate = delegate;
	}
	
	public java.awt.image.DataBuffer getDelegate()
	{
		return delegate;
	}
}
