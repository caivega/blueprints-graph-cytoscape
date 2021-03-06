package org.cytoscape.blueprints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;

import com.tinkerpop.blueprints.pgm.Element;

/**
 * Property 
 *
 */
public class ElementCyRow implements CyRow {

	// A row is always associated Blueprints Vertex or Edge.
	private final Element ele;
	
	private final CyTable table;
	
	private final CyEventHelper eventHelper;
	
	ElementCyRow (final CyTable table, final Element ele, final CyEventHelper eventHelper) {
		if(table == null)
			throw new NullPointerException("Table is null.");
		if(ele == null)
			throw new NullPointerException("Graph Element is null.");
		
		this.ele = ele;
		this.table = table;
		
		this.eventHelper = eventHelper;
		
		// FIXME: this does not work for Sail.  We may need some condition to handle special case?
		// Automatically add the SUID as the Primary Key
		try {
			ele.setProperty(table.getPrimaryKey().getName(), ele.getId());
		} catch(Exception e) {
			// TODO: What's needed for exception handling?
			
		}
		//if (!table.rowExists(ele.getId())) {
			//((ElementCyTable)table).addRow(this);
		//}
	}

	@Override
	public <T> T get(final String columnName, final Class<? extends T> type) {
		if(columnName == null)
			throw new IllegalArgumentException("columnName is null.");
		
		final CyColumn column = table.getColumn(columnName);
		if (column != null) {
			VirtualColumnInfo virtual = column.getVirtualColumnInfo();
			if (virtual.isVirtual())
				return virtual.getSourceTable().getRow(this.getID()).get(virtual.getSourceColumn(), type);
		} else
			throw new IllegalArgumentException("No Such Column");

		final Object o = ele.getProperty(columnName); //wrong type, o null
		return type.cast(o);
	}

	@Override
	public <T> List<T> getList(String columnName, Class<T> listElementType) {
		if (table.getColumn(columnName) != null) {
			VirtualColumnInfo virtual = table.getColumn(columnName).getVirtualColumnInfo();
			if (virtual.isVirtual()) {
				return virtual.getSourceTable().getRow(this.getID()).getList(virtual.getSourceColumn(), listElementType);
			}
		} else {
			throw new IllegalArgumentException("No Such Column");
		}
		if (table.getColumn(columnName).getType() != List.class)
			throw new IllegalArgumentException("Not a list, please use get()");
		if (table.getColumn(columnName).getListElementType() != listElementType)
			throw new IllegalArgumentException("Invalid List Element Type");
		Object o = ele.getProperty(columnName); //check valid column
		if(o instanceof List) {
			return (List<T>) o;
		}
		return null;
	}

	@Override
	public <T> void set(final String columnName, final T value) {
		if (columnName == null)
			throw new NullPointerException("columName is null.");
		
		if (table.getColumn(columnName) != null) {
			VirtualColumnInfo virtual = table.getColumn(columnName).getVirtualColumnInfo();
			if (virtual.isVirtual()) {
				virtual.getSourceTable().getRow(this.getID()).set(virtual.getSourceColumn(), value);
			}
		} else {
			throw new IllegalArgumentException("No Such Column");
		}
		if (value == null) {
			ele.removeProperty(columnName);
			eventHelper.addEventPayload(table, new RowSetRecord(this, columnName, null, null), RowsSetEvent.class);
		} else if (!table.getColumn(columnName).getType().isAssignableFrom(value.getClass()))
			throw new IllegalArgumentException("Values of wrong type" + table.getColumn(columnName).getType() + " vs " + value.getClass() );
		else {
			ele.setProperty(columnName, value);
			eventHelper.addEventPayload(table, new RowSetRecord(this, columnName, value, value), RowsSetEvent.class);
		}
	}

	@Override
	public boolean isSet(String columnName) {
		if (table.getColumn(columnName) != null) {
			VirtualColumnInfo virtual = table.getColumn(columnName).getVirtualColumnInfo();
			if (virtual.isVirtual()) {
				//Should this use getMatchingRows for different key matching?
				return virtual.getSourceTable().getRow(this.getID()).isSet(virtual.getSourceColumn());
			}
			return (ele.getProperty(columnName) != null);
		} else {
			return false;
		}
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
		VirtualColumnInfo virtual = table.getColumn(columnName).getVirtualColumnInfo();
		if (virtual.isVirtual()) {
			//This should return the getRaw method for the proper row
		}
		return ele.getProperty(columnName);
	}

	@Override
	public CyTable getTable() {
		return table;
	}

	/**
	 * This returns Element's (Vertex/Edge) ID.
	 * THIS CAN BE ANY OBJECT TYPE.
	 * @return
	 */
	Object getID() {
		return ele.getId();
	}
}
