package net.mrbt0907.weather2.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MapEX<A, B>
{
	private int index;
	private final List<A> keys;
	private final Map<A, B> map;
	
	public MapEX()
	{
		keys = new ArrayList<A>();
		map = new LinkedHashMap<A, B>(); 
	}
	
	public MapEX(Map<A, B> input)
	{
		map = new LinkedHashMap<A, B>(input);
		keys = new ArrayList<A>(map.keySet());
	}
	
	public B get(A key)
	{
		if (map.containsKey(key))
		{
			index = keys.indexOf(key);
			return map.get(key);
		}
		
		return null;
	}
	
	public B getCurrent()
	{
		return map.get(keys.get(index));
	}
	
	public A nextKey()
	{
		index++;
		
		if (index >= keys.size())
			index = 0;
		
		return keys.get(index);
	}
	
	public B nextValue()
	{
		index++;
		
		if (index >= keys.size())
			index = 0;
		
		return map.get(keys.get(index));
	}
	
	public int indexOf(A key)
	{
		return keys.indexOf(key);
	}
	
	public A randomKey()
	{
		int index = Maths.random(keys.size() - 1);
		this.index = index;
		
		return keys.get(index);
	}
	
	public B randomValue()
	{
		int index = Maths.random(keys.size() - 1);
		this.index = index;
		
		return map.get(keys.get(index));
	}
	
	public void put(A key, B value)
	{
		if (!map.containsKey(key))
			keys.add(key);
		map.put(key, value);
	}
	
	public void remove(A key)
	{
		if (map.containsKey(key))
		{
			map.remove(key);
			keys.remove(key);
			if (index >= keys.size())
				index = Math.max(0, keys.size() - 1);
		}
	}
	
	public int size()
	{
		return keys.size();
	}
	
	public List<A> keys()
	{
		return new ArrayList<A>(keys);
	}
	
	public List<B> values()
	{
		return new ArrayList<B>(map.values());
	}
	
	public void clear()
	{
		keys.clear();
		map.clear();
	}
	
	public boolean containsKey(A key)
	{
		return map.containsKey(key);
	}
	
	public void forEach(BiConsumer <? super A, ? super B> action)
	{
		map.forEach(action);
	}
}
