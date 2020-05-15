package jds;

import java.util.function.Function;

public class JDSIndex<K, V> {

	private String name;
	
	private Class<K> keyType;
	
	private Class<V> valueType;
	
	private Function<V, K> indexer;

	private boolean unique;
	
	public JDSIndex() { }

	public JDSIndex( 
		String name, 
		Class<K> keyType,
		Class<V> valueType,
		Function<V, K> indexer,
		boolean unique 
	) {
		this.name = name;
		this.keyType = keyType;
		this.valueType = valueType;
		this.indexer = indexer;
		this.unique = unique;
	}

	public String name() {
		return name;
	}
	
	public JDSIndex<K, V> name( String n ) {
		this.name = n;
		return this;
	}
	
	public Class<K> keyType() {
		return keyType;
	}
	
	public JDSIndex<K, V> keyType( Class<K> k ) {
		this.keyType = k;
		return this;
	}
	
	public Class<V> valueType() {
		return valueType;
	}
	
	public JDSIndex<K, V> valueType( Class<V> v ) {
		this.valueType = v;
		return this;
	}
	
	public Function<V, K> indexer() {
		return indexer;
	}
	
	public JDSIndex<K, V> indexer( Function<V, K> i ) {
		this.indexer = i;
		return this;
	}
	
	public boolean unique() {
		return unique;
	}
	
	public JDSIndex<K, V> unique( boolean u ) {
		this.unique = u;
		return this;
	}
}
