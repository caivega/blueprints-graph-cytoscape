package org.cytoscape.blueprints;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import com.tinkerpop.blueprints.pgm.Element;

public class ElementCyRow implements CyRow {

	private final Element ele;
	private final CyTable table;
	
	ElementCyRow (final CyTable table, final Element ele) {
		this.ele = ele;
		this.table = table;
		
		// Automatically add the SUID as the Primary Key
		ele.setProperty(table.getPrimaryKey().getName(), ele.getId());
	}

	@Override
	public <T> T get(String columnName, Class<? extends T> type) {
		if (table.getColumn(columnName) == null)
			throw new IllegalArgumentException("No Such Column");
		Object o = ele.getProperty(columnName); //wrong type, o null
		return type.cast(o);
	}

	@Override
	public <T> List<T> getList(String columnName, Class<T> listElementType) {
		if (table.getColumn(columnName) == null)
			throw new IllegalArgumentException("No Such Column");
		if (table.getColumn(columnName).getType() != List.class)
			throw new IllegalArgumentException("Not a list, please use get()");
		if (table.getColumn(columnName).getListElementType() != listElementType)
			throw new IllegalArgumentException("Invalid List Element Type");
		Object o = ele.getProperty(columnName); //check valid column
		if(o instanceof List) {
			return (List<T>) o;
		}
		//return Collections.emptyList();
		return null;
	}

	@Override
	public <T> void set(String columnName, T value) {
		if (columnName == null)
			throw new NullPointerException("Accessing Null Column");
		if (table.getColumn(columnName) == null) {
			throw new IllegalArgumentException("No Such Column");
		}
		if (value == null) 
			ele.removeProperty(columnName);
		else if (!table.getColumn(columnName).getType().isAssignableFrom(value.getClass()))
			throw new IllegalArgumentException("Values of wrong type" + table.getColumn(columnName).getType() + " vs " + value.getClass() );
		else
			ele.setProperty(columnName, value);
	}

	@Override
	public boolean isSet(String columnName) {
		return (ele.getProperty(columnName) != null);
	}

	@Override
	public Map<String, Object> getAllValues() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		for(String key : ele.getPropertyKeys()) {
			map.put(key, ele.getProperty(key));
		}
		return map;
	}

	@Override
	public Object getRaw(String columnName) {
		return ele.getProperty(columnName);
	}

	@Override
	public CyTable getTable() {
		return table;
	}
	
	//Return Elements SUID
	public Long getID() {
		return Long.parseLong(ele.getId().toString());
	}

}
