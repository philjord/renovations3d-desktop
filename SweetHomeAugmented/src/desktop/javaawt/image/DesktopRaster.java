package desktop.javaawt.image;

import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

import javaawt.image.DataBuffer;
import javaawt.image.Raster;

public class DesktopRaster implements Raster
{
	private java.awt.image.Raster delegate = null;

	public DesktopRaster(java.awt.image.Raster delegate)
	{
		this.delegate = delegate;
	}

	public java.awt.image.Raster getDelegate()
	{
		return delegate;
	}

	public void getDataElements(int w, int h, Object pixel)
	{
		delegate.getDataElements(w, h, pixel);
	}

	public int getNumDataElements()
	{

		return delegate.getNumDataElements();
	}

	public int getTransferType()
	{

		return delegate.getTransferType();
	}

	public DataBuffer getDataBuffer()
	{
		if (delegate.getDataBuffer() instanceof DataBufferInt)
			return new DesktopDataBufferInt((DataBufferInt) delegate.getDataBuffer());
		else
			return new DesktopDataBufferByte((DataBufferByte) delegate.getDataBuffer());
	}
}
