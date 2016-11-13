package desktop.javaawt.image;

import javaawt.image.DataBufferInt;

public class DesktopDataBufferInt extends DesktopDataBuffer implements DataBufferInt
{
	private java.awt.image.DataBufferInt delegate = null;

	public DesktopDataBufferInt(java.awt.image.DataBufferInt delegate)
	{
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public java.awt.image.DataBufferInt getDelegate()
	{
		return delegate;
	}

	public int[] getData()
	{  
		return delegate.getData();
	}
}
