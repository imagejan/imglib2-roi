/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2017 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

package net.imglib2.roi.labeling;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.list.ListImgFactory;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ImgLabelingTest {

	@Test public void testBackedByListImgRandomAccess()
	{
		Img< IntType > image =
				new ListImgFactory<>( new IntType() ).create( new long[] { 2 } );
		ImgLabeling< String, IntType > labeling = new ImgLabeling<>( image );
		RandomAccess< LabelingType< String > > ra = labeling.randomAccess();
		ra.setPosition( 0, 0 );
		ra.get().add( "a" );
		ra.setPosition( 1, 0 );
		ra.get().add( "b" );
		assertEquals( Collections.singleton( "b" ), ra.get() );
	}

	@Test public void testBackedByListImgCursor()
	{
		Img< IntType > image =
				new ListImgFactory<>( new IntType() ).create( new long[] { 2 } );
		ImgLabeling< String, IntType > labeling = new ImgLabeling<>( image );
		Cursor< LabelingType< String > > cursor = labeling.cursor();
		cursor.next().add( "a" );
		cursor.next().add( "b" );
		assertEquals( Collections.singleton( "b" ), cursor.get() );
	}

	@Test public void testBackedByStack()
	{
		RandomAccessibleInterval< IntType > a = ArrayImgs.ints( 1 );
		RandomAccessibleInterval< IntType > b = ArrayImgs.ints( 1 );
		RandomAccessibleInterval< IntType > image = Views.stack( a, b );
		ImgLabeling< String, IntType > labeling = new ImgLabeling<>( image );
		Cursor< LabelingType< String > > cursor = labeling.cursor();
		cursor.next().add( "a" );
		cursor.next().add( "b" );
		assertEquals( Collections.singleton( "b" ), cursor.get() );
	}
}
