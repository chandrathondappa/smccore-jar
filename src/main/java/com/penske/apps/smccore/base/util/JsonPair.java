/**
 * @author john.shiffler (600139252)
 */
package com.penske.apps.smccore.base.util;

/**
 * Class similar to Apache Commons Pair, but which can be serialized by Jackson so it contains a "left" and a "right" property.
 * The Apache Commons Pair class is not suitible for serialization to JSON, because of the way Jackson serializes it. For instance:
 * 	Pair.of("foo", 23) gets transformed by Jackson into: {"foo": 23}. This is difficult to consume in JavaScript. Instead:
 * 	JsonPair.of("foo", 23) becomes {left: "foo", right: 23}.
 * @param <K> The type of object stored in the left-hand element of the pair.
 * @param <V> The type of object stored in the right-hand element of the pair.
 * @see https://stackoverflow.com/q/44905502 for more information on this behavior
 */
public class JsonPair<K, V>
{
	private final K key;
	private final V value;
	
	private JsonPair(K left, V right)
	{
		this.key = left;
		this.value = right;
	}
	
	public static <L, R> JsonPair<L, R> of(L left, R right)
	{
		return new JsonPair<L, R>(left, right);
	}

	/** {@inheritDoc} */
	@Override
	public String toString()
	{
		return "{L: " + (key == null ? "" : key.toString()) + ", R: " + (value == null ? "" : value.toString()) + "}";
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonPair<?, ?> other = (JsonPair<?, ?>) obj;
		if (key == null)
		{
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null)
		{
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}	
	
	//***** DEFAULT ACCESSORS *****//
	public K getKey()
	{
		return key;
	}

	public V getValue()
	{
		return value;
	}

}