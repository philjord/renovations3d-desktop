package desktop.javaawt.image;

import javaawt.color.ColorSpace;

public class DesktopColorSpace implements ColorSpace
{
	private java.awt.color.ColorSpace delegate = null;

	public DesktopColorSpace(java.awt.color.ColorSpace delegate)
	{
		this.delegate = delegate;
	}

	//@Override
	public java.awt.color.ColorSpace getDelegate()
	{
		return delegate;
	}

	//@Override
	public boolean isCS_sRGB()
	{
		return delegate.isCS_sRGB();
	}

	//@Override
	public float[] toRGB(float[] colorvalue)
	{

		return delegate.toRGB(colorvalue);
	}

	//@Override
	public float[] fromRGB(float[] rgbvalue)
	{

		return delegate.fromRGB(rgbvalue);
	}

	//@Override
	public float[] toCIEXYZ(float[] colorvalue)
	{

		return delegate.toCIEXYZ(colorvalue);
	}

	//@Override
	public float[] fromCIEXYZ(float[] colorvalue)
	{

		return delegate.fromCIEXYZ(colorvalue);
	}

	//@Override
	public int getType()
	{

		return delegate.getType();
	}

	//@Override
	public int getNumComponents()
	{

		return delegate.getNumComponents();
	}

	//@Override
	public String getName(int idx)
	{
		return delegate.getName(idx);
	}

	//@Override
	public float getMinValue(int component)
	{

		return delegate.getMinValue(component);
	}

	//@Override
	public float getMaxValue(int component)
	{

		return delegate.getMaxValue(component);
	}
}
