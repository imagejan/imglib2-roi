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

package net.imglib2.troi.geom.real;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.troi.RealMaskRealInterval;

/**
 * A {@link RealMaskRealInterval} which defines a real space 2D polygon.
 *
 * @author Alison Walter
 */
public interface Polygon2D< T extends RealLocalizable & RealPositionable > extends RealMaskRealInterval
{
	/** Return the vertex at the specified position */
	T vertex( final int pos );

	/** Get the number of vertices */
	int numVertices();

	/** Adds a vertex at the given index. */
	void addVertex( int index, double[] vertex );

	/** Removes the vertex at the given index. */
	void removeVertex( int index );

	// -- RealInterval methods --

	@Override
	default int numDimensions()
	{
		return 2;
	}

	@Override
	default double realMin( final int d )
	{
		double min = vertex( 0 ).getDoublePosition( d );
		for ( int i = 1; i < numVertices(); i++ )
		{
			if ( vertex( i ).getDoublePosition( d ) < min )
				min = vertex( i ).getDoublePosition( d );
		}
		return min;
	}

	@Override
	default double realMax( final int d )
	{
		double max = vertex( 0 ).getDoublePosition( d );
		for ( int i = 0; i < numVertices(); i++ )
		{
			if ( vertex( i ).getDoublePosition( d ) > max )
				max = vertex( i ).getDoublePosition( d );
		}
		return max;
	}
}