package jds;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import jds.converter.JDSConverter;

public class JDSTable<K, V> {

	private String name;
	
	private Class<K> keyType;
	
	private Class<V> valueType;
	
	private Function<V, K> keyProvider;
	
	private JDSConverter<K> keyConverter;
	
	private JDSConverter<V> valueConverter;

	private final List<JDSIndex<?,V>> indexes = new ArrayList<>();
	
	private boolean temporary;
	
	public JDSTable() { }
	
	public JDSTable( String name ) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JDSTable<K,V> other = (JDSTable<K, V>) obj;
		return name.equals(other.name);
	}
	
	@Override
	public String toString() {
		return "JDSTable [name=" + name + "]";
	}

	public String name() {
		return name;
	}
	
	public JDSTable<K, V> name( String n ) {
		this.name = n;
		return this;
	}
	
	public Class<K> keyType() {
		return keyType;
	}
	
	public JDSTable<K, V> keyType( Class<K> keyType ) {
		this.keyType = keyType;
		return this;
	}
	
	public Class<V> valueType() {
		return valueType;
	}
	
	public JDSTable<K, V> valueType( Class<V> keyType ) {
		this.valueType = keyType;
		return this;
	}
	
	public Function<V, K> keyProvider() {
		return keyProvider;
	}
	
	public JDSTable<K, V> keyProvider( Function<V, K> p ) {
		this.keyProvider = p;
		return this;
	}
	
	public JDSConverter<K> keyConverter() {
		return keyConverter;
	}
	
	public JDSTable<K, V> keyConverter( JDSConverter<K> c ) {
		this.keyConverter = c;
		return this;
	}
	
	public JDSConverter<V> valueConverter() {
		return valueConverter;
	}
	
	public JDSTable<K, V> valueConverter( JDSConverter<V> c ) {
		this.valueConverter = c;
		return this;
	}
	
	public <I> JDSTable<K, V> uniqueIndex( String name, Class<I> keytype, Function<V, I> indexer ) {
		return index( new JDSIndex<I, V>( name, keytype, valueType, indexer, true ) );
	}
	
	public <I> JDSTable<K, V> nonUniqueIndex( String name, Class<I> keytype, Function<V, I> indexer ) {
		return index( new JDSIndex<I, V>( name, keytype, valueType, indexer, false ) );
	}
	
	public JDSTable<K, V> index( JDSIndex<?, V> i ) {
		Objects.requireNonNull( i.name() );
		Objects.requireNonNull( i.keyType() );
		Objects.requireNonNull( i.valueType() );
		Objects.requireNonNull( i.indexer() );
		
		this.indexes.add( i );
		return this;
	}
	
	public List<JDSIndex<?, V>> indexes() {
		return indexes;
	}
	
	public boolean temporary() {
		return temporary;
	}
	
	public JDSTable<K, V> temporary( boolean b ) {
		this.temporary = b;
		return this;
	}
}
