package desktop.javaawt.image;

import javaawt.image.WritableRaster;

public class DesktopWritableRaster extends DesktopRaster implements WritableRaster
{
	private java.awt.image.WritableRaster delegate = null;

	public DesktopWritableRaster(java.awt.image.WritableRaster delegate)
	{
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public java.awt.image.WritableRaster getDelegate()
	{
		return delegate;
	}

	public int[] getDataElements(int i, int j, int width, int height, Object object)
	{

		return (int[]) delegate.getDataElements(i, j, width, height, object);
	}

}
