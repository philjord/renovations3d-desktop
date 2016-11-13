package desktop.javaawt.image;

import javaawt.image.SampleModel;

public class DesktopSampleModel extends SampleModel
{
	private java.awt.image.SampleModel delegate = null;

	public DesktopSampleModel(java.awt.image.SampleModel delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public java.awt.image.SampleModel getDelegate()
	{
		return delegate;
	}
	
	
	@Override
	public int getNumBands()
	{
		return delegate.getNumBands();
	}

	@Override
	public int getDataType()
	{
		return delegate.getDataType();
	}
}
