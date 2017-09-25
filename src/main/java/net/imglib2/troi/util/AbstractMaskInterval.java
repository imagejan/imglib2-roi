package net.imglib2.troi.util;

import java.util.function.Predicate;
import net.imglib2.AbstractInterval;
import net.imglib2.Interval;
import net.imglib2.Localizable;
import net.imglib2.troi.BoundaryType;
import net.imglib2.troi.MaskInterval;

public class AbstractMaskInterval extends AbstractInterval implements MaskInterval
{
	private final BoundaryType boundaryType;

	private final Predicate< ? super Localizable > predicate;

	public AbstractMaskInterval(
			final Interval interval,
			final BoundaryType boundaryType,
			Predicate< ? super Localizable > predicate )
	{
		super( interval );
		this.boundaryType = boundaryType;
		this.predicate = predicate;
	}

	@Override
	public BoundaryType boundaryType()
	{
		return boundaryType;
	}

	@Override
	public boolean test( final Localizable localizable )
	{
		return predicate.test( localizable );
	}
}