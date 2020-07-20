/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2020 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
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

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.imglib2.type.numeric.IntegerType;

/**
 * The LabelingMapping maps a set of labelings of a pixel to an index value
 * which can be more compactly stored than the set of labelings. It provides an
 * {@link #intern(Set)} function that supplies a canonical object for each set
 * of labelings in a container.
 *
 * @param <T>
 * 		the desired type of the pixel labels, for instance {@link Integer}
 * 		to number objects or {@link String} for user-assigned label names.
 *
 * @author Lee Kamentsky
 * @author Tobias Pietzsch
 */
public class LabelingMapping< T >
{
	/**
	 * Maximum number of distinct label sets that can be represented by this
	 * mapping. Depends on the {@link IntegerType} to which label sets are
	 * mapped.
	 */
	private final int maxNumLabelSets;

	/**
	 * Stores labels for all interned sets.
	 */
	private final ArrayList< T > data = new ArrayList<>();

	/**
	 * Maps indices to {@link InternedSet} (canonical label sets).
	 * {@code setsByIndex.get( i ).index == i} holds.
	 */
	private final ArrayList< InternedSet< T > > setsByIndex = new ArrayList<>();

	/**
	 * TODO
	 */
	private final Map< Set< T >, InternedSet< T > > internedSets = new ConcurrentHashMap<>();

	/**
	 * the empty label set.
	 */
	private InternedSet< T > theEmptySet;


	/**
	 * Create a new {@link LabelingMapping} that maps label sets to the given
	 * integral {@code indexType}.
	 */
	public LabelingMapping( final IntegerType< ? > indexType )
	{
		this( ( int ) indexType.getMaxValue() );
	}

	private LabelingMapping( final int maxNumLabelSets )
	{
		this.maxNumLabelSets = maxNumLabelSets;
		theEmptySet = intern( new HashSet<>( 0 ) );
	}

	/**
	 * Create a new, empty {@link LabelingMapping} of the same type as this one.
	 */
	LabelingMapping< T > newInstance()
	{
		return new LabelingMapping<>( maxNumLabelSets );
	}

	void clear()
	{
		clearCacheMaps();
		data.clear();
		setsByIndex.clear();
		internedSets.clear();
		theEmptySet = intern( new HashSet<>( 0 ) );
	}

	public InternedSet< T > emptySet()
	{
		return theEmptySet;
	}

	/**
	 * Return the index value of the given set.
	 */
	int indexOf( final Set< T > key )
	{
		return intern( key ).index;
	}

	/**
	 * Return the canonical set for the given index value.
	 */
	InternedSet< T > setAtIndex( final int index )
	{
		return setsByIndex.get( index );
	}

	/**
	 * Returns the (unmodifiable) set of labels for the given index value.
	 */
	public Set< T > labelsAtIndex( final int index )
	{
		return setAtIndex( index );
	}

	/**
	 * Return the canonical set for the given label set.
	 */
	public InternedSet< T > intern( final Set< T > src )
	{
		return internedSets.computeIfAbsent( src, this::create );
	}

	/**
	 * Returns the number of indexed labeling sets
	 */
	public int numSets()
	{
		return setsByIndex.size();
	}

	/**
	 * Return the set of all labels defined in this {@link LabelingMapping}.
	 */
	public Set< T > getLabels()
	{
		return new HashSet<>( data );
	}

	/**
	 * Returns a snapshot of the {@link LabelingMapping} represented as a list
	 * of sets.
	 * <p>
	 * {@link List#get(int)} of the returned list will give the same value
	 * {@link LabelingMapping#labelsAtIndex(int)}. The size of the return listed
	 * equals {@link LabelingMapping#numSets()}.
	 */
	public List< Set< T > > getLabelSets()
	{
		final ArrayList< Set< T > > labelSets = new ArrayList<>( numSets() );
		labelSets.addAll( setsByIndex );
		return labelSets;
	}

	/**
	 * Replaces the current label mapping, with the mapping given as list of
	 * sets.
	 * <p>
	 * WARNING: Using this method could easily result in a malfunctioning
	 * {@link ImgLabeling}. This is certainly the case, if values of the index
	 * image don't map to any value in this list. This is the case, if the index
	 * image contains negative values or values greater than or equal to the
	 * size of the list.
	 * <p>
	 *
	 * @param labelSets
	 *            The given list must not be empty. The first entry must be the
	 *            empty set. All list entries must be unique. If used together
	 *            with a {@link ImgLabeling}, a pixel in the index image will be
	 *            mapped to the set with the given index in the list.
	 *
	 * @see LabelingMapping#getLabelSets()
	 */
	public void setLabelSets( final List< Set< T > > labelSets )
	{
		if ( labelSets.isEmpty() )
			throw new IllegalArgumentException( "expected non-empty list of label-sets" );

		if ( !labelSets.get( 0 ).isEmpty() )
			throw new IllegalArgumentException( "label-set at index 0 expected to be the empty label set" );

		// clear everything and add the empty set
		clear();

		// add remaining label sets
		final int numLabelSets = labelSets.size();
		for ( int i = 1; i < numLabelSets; ++i )
		{
			final Set< T > set = labelSets.get( i );
			if ( internedSets.get( set ) != null )
				throw new IllegalArgumentException( "no duplicates allowed in list of label-sets" );
			intern( set );
		}

		data.trimToSize();
		setsByIndex.trimToSize();
	}

	private synchronized InternedSet< T > create( final Set< T > set )
	{
		final int index = setsByIndex.size();
		if ( index > maxNumLabelSets )
			throw new AssertionError( String.format( "Too many labels (or types of multiply-labeled pixels): %d maximum", index ) );

		final int offset = data.size();
		final int size = set.size();
		final int hashCode = set.hashCode();

		for ( T label : set )
			data.add( label );

		InternedSet< T > internedSet = new InternedSet<>( this, offset, size, hashCode, index );
		setsByIndex.add( internedSet );
		return internedSet;
	}

	/**
	 * Canonical representative for a label set. Contains the
	 * index to which the label set is mapped.
	 */
	public static class InternedSet< T > extends AbstractCollection< T > implements Set< T >
	{
		private final LabelingMapping< T > container;

		private final int offset;

		private final int size;

		final int hashCode;

		final int index;

		private InternedSet( final LabelingMapping< T > container, final int offset, final int size, final int hashCode, final int index )
		{
			this.container = container;
			this.offset = offset;
			this.size = size;
			this.hashCode = hashCode;
			this.index = index;
		}

		@Override
		public int size()
		{
			return size;
		}

		@Override
		public boolean isEmpty()
		{
			return size == 0;
		}

		@Override
		public int hashCode()
		{
			return hashCode;
		}

		@Override
		public boolean equals( final Object obj )
		{
			return obj == this;
		}

		@Override
		public boolean contains( final Object o )
		{
			if ( o == null )
				return false;

			final int size = size();
			for ( int i = 0; i < size; i++ )
				if ( o.equals( container.data.get( offset + i ) ) )
					return true;
			return false;
		}

		@Override
		public Iterator< T > iterator()
		{
			return new Iter();
		}

		class Iter implements Iterator< T >
		{
			private int index = 0;

			@Override
			public boolean hasNext()
			{
				return index < size;
			}

			@Override
			public T next()
			{
				final T label = container.data.get( offset + index );
				++index;
				return label;
			}
		}
	}

	private List< WeakReference< AddRemoveCacheMap > > cacheMaps = new ArrayList<>();

	private void clearCacheMaps()
	{
		cacheMaps.forEach( ref -> {
			final AddRemoveCacheMap cacheMap = ref.get();
			if ( cacheMap != null )
				cacheMap.clear();
		} );
	}

	AddRemoveCacheMap createAddRemoveCacheMap()
	{
		cacheMaps.removeIf( ref -> ref.get() == null );
		final AddRemoveCacheMap cacheMap = new AddRemoveCacheMap();
		cacheMaps.add( new WeakReference<>( cacheMap ) );
		return cacheMap;
	}

	static class CachedTriple< T >
	{
		int fromIndex = -1;
		T label = null;
		int toIndex = -1;
	}

	class AddRemoveCacheMap
	{
		private static final int numSignificantIndexBits = 4;
		private static final int significantIndexValues = ( 1 << numSignificantIndexBits );
		private static final int significantIndexBitsMask = significantIndexValues - 1;

		private static final int numSignificantLabelBits = 4;
		private static final int significantLabelValues = ( 1 << numSignificantLabelBits );
		private static final int significantLabelBitsMask = significantLabelValues - 1;

		final CachedTriple< T >[] addCache;
		final CachedTriple< T >[] removeCache;

		@SuppressWarnings( { "unchecked", "raw" } )
		AddRemoveCacheMap()
		{
			addCache = new CachedTriple[ significantIndexValues * significantLabelValues ];
			removeCache = new CachedTriple[ significantIndexValues * significantLabelValues ];
			clear();
		}

		void clear()
		{
			Arrays.setAll( addCache, i -> new CachedTriple<>() );
			Arrays.setAll( removeCache, i -> new CachedTriple<>() );
		}

		public int addLabelToSetAtIndex( final T label, final int index )
		{
			final int row = index & significantIndexBitsMask;
			final int col = label.hashCode() & significantLabelBitsMask;
			final CachedTriple< T > triple = addCache[ row * significantIndexValues + col ];
			if (triple.fromIndex == index && triple.label.equals( label ) )
				return triple.toIndex;
			else
			{
				// update triple
				final Set< T > target = new HashSet<>( setAtIndex( index ) );
				target.add( label );
				final int toIndex = indexOf( target );
				triple.fromIndex = index;
				triple.label = label;
				triple.toIndex = toIndex;
				return toIndex;
			}
		}

		public int removeLabelFromSetAtIndex( final T label, final int index )
		{
			final int row = index & significantIndexBitsMask;
			final int col = label.hashCode() & significantLabelBitsMask;
			final CachedTriple< T > triple = removeCache[ row * significantIndexValues + col ];
			if (triple.fromIndex == index && triple.label.equals( label ) )
				return triple.toIndex;
			else
			{
				// update triple
				final Set< T > target = new HashSet<>( setAtIndex( index ) );
				target.remove( label );
				final int toIndex = indexOf( target );
				triple.fromIndex = index;
				triple.label = label;
				triple.toIndex = toIndex;
				return toIndex;
			}
		}
	}

	/**
	 * @deprecated
	 * Use {@link LabelingMapping#getLabelSets()} or
	 * {@link LabelingMapping#setLabelSets(List)} instead.
	 * <p>
	 * Internals. Can be derived for implementing de/serialisation of the
	 * {@link LabelingMapping}.
	 */
	@Deprecated
	public static class SerialisationAccess< T >
	{
		private final LabelingMapping< T > labelingMapping;

		protected SerialisationAccess( final LabelingMapping< T > labelingMapping )
		{
			this.labelingMapping = labelingMapping;
		}

		@Deprecated
		/**
		 * @deprecated
		 * Use {@link LabelingMapping#getLabelSets()} instead.
		 */
		protected List< Set< T > > getLabelSets()
		{
			return labelingMapping.getLabelSets();
		}

		@Deprecated
		/**
		 * @deprecated
		 * Use {@link LabelingMapping#setLabelSets()} instead.
		 */
		protected void setLabelSets( final List< Set< T > > labelSets )
		{
			labelingMapping.setLabelSets( labelSets );
		}
	}
}
