/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imglib2.roi.geom.real;

import net.imglib2.AbstractRealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.mask.Mask;

/**
 * Abstract base class for implementations of {@link Box}.
 *
 * @author Alison Walter
 */
public abstract class AbstractBox extends AbstractRealInterval implements Box
{
	/**
	 * Creates an n-d rectangular {@link Mask} in real space. The dimensionality
	 * is dictated by the length of the min array.
	 *
	 * @param min
	 *            An array containing the minimum position in each dimension. A
	 *            copy of this array is stored.
	 * @param max
	 *            An array containing maximum position in each dimension. A copy
	 *            of this array is stored.
	 */
	public AbstractBox( final double[] min, final double[] max )
	{
		super( min, max );
		if ( max.length < min.length )
			throw new IllegalArgumentException( "Max array cannot be smaller than the min array" );
	}

	@Override
	public abstract boolean contains( RealLocalizable l );

	@Override
	public double sideLength( final int d )
	{
		return Math.abs( max[ d ] - min[ d ] );
	}

	@Override
	public double[] center()
	{
		final double[] center = new double[ n ];
		for ( int d = 0; d < n; d++ )
		{
			center[ d ] = ( max[ d ] + min[ d ] ) / 2.0;
		}
		return center;
	}

	/**
	 * Sets the center of the box.
	 *
	 * @param center
	 *            contains the position of new center in each dimension,
	 *            positions dimensions greater than {@code n} will be ignored
	 */
	@Override
	public void setCenter( final double[] center )
	{
		if ( center.length < n )
			throw new IllegalArgumentException( "Center must have at least length " + n );
		for ( int d = 0; d < n; d++ )
		{
			final double halfSideLength = sideLength( d ) / 2.0;
			min[ d ] = center[ d ] - halfSideLength;
			max[ d ] = center[ d ] + halfSideLength;
		}
	}

	@Override
	public void setSideLength( final int d, final double length )
	{
		if ( length < 0 )
			throw new IllegalArgumentException( "Cannot have negative edge lengths " );
		final double[] center = center();
		max[ d ] = center[ d ] + length / 2.0;
		min[ d ] = center[ d ] - length / 2.0;
	}
}
