package desktop.javaawt;

import javaawt.Graphics;

public class DesktopGraphics implements Graphics
{
	private java.awt.Graphics delegate = null;

	public DesktopGraphics(java.awt.Graphics delegate)
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
}
