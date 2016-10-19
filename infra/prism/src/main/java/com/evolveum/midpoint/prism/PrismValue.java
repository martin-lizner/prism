/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.prism;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.MiscUtil;
import com.evolveum.midpoint.util.exception.SchemaException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author semancik
 *
 */
public abstract class PrismValue implements IPrismValue {
	
	private OriginType originType;
    private Objectable originObject;
    private Itemable parent;
    protected Element domElement = null;
    private transient Map<String,Object> userData = new HashMap<>();
	protected boolean immutable;

    PrismValue() {
		super();
	}
    
    PrismValue(OriginType type, Objectable source) {
		super();
		this.originType = type;
		this.originObject = source;
	}
    
    PrismValue(OriginType type, Objectable source, Itemable parent) {
		super();
		this.originType = type;
		this.originObject = source;
		this.parent = parent;
	}

	public void setOriginObject(Objectable source) {
        this.originObject = source;
    }

    public void setOriginType(OriginType type) {
        this.originType = type;
    }
    
    @Override
	public OriginType getOriginType() {
        return originType;
    }

    @Override
	public Objectable getOriginObject() {
        return originObject;
    }

    public Map<String, Object> getUserData() {
        return userData;
    }

    @Override
	public Object getUserData(@NotNull String key) {
        return userData.get(key);
    }

    @Override
	public void setUserData(@NotNull String key, Object value) {
        userData.put(key, value);
    }

    @Override
	public Itemable getParent() {
		return parent;
	}

	@Override
	public void setParent(Itemable parent) {
		if (this.parent != null && parent != null && this.parent != parent) {
			throw new IllegalStateException("Attempt to reset value parent from "+this.parent+" to "+parent);
		}
		this.parent = parent;
	}
	
	@NotNull
	@Override
	public ItemPath getPath() {
		Itemable parent = getParent();
		if (parent == null) {
			throw new IllegalStateException("No parent, cannot create value path for "+this); 
		}
		return parent.getPath();
	}
	
	/**
	 * Used when we are removing the value from the previous parent.
	 * Or when we know that the previous parent will be discarded and we
	 * want to avoid unnecessary cloning.
	 */
	@Override
	public void clearParent() {
		parent = null;
	}
	
	public static <T> void clearParent(List<PrismPropertyValue<T>> values) {
		if (values == null) {
			return;
		}
		for (PrismPropertyValue<T> val: values) {
			val.clearParent();
		}
	}
	
	@Override
	public PrismContext getPrismContext() {
		if (parent != null) {
			return parent.getPrismContext();
		}
		return null;
	}
	
	protected ItemDefinition getDefinition() {
		Itemable parent = getParent();
    	if (parent == null) {
    		return null;
    	}
    	return parent.getDefinition();
    }
	
	@Override
	public void applyDefinition(ItemDefinition definition) throws SchemaException {
		checkMutability();		// TODO reconsider
		applyDefinition(definition, true);
	}
	
	@Override
	public void applyDefinition(ItemDefinition definition, boolean force) throws SchemaException {
		checkMutability();		// TODO reconsider
		// Do nothing by default
	}
	
	public void revive(PrismContext prismContext) throws SchemaException {
		recompute(prismContext);
	}
	
	/**
	 * Recompute the value or otherwise "initialize" it before adding it to a prism tree.
	 * This may as well do nothing if no recomputing or initialization is needed.
	 */
	@Override
	public void recompute() {
		recompute(getPrismContext());
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public void accept(Visitor visitor, ItemPath path, boolean recursive) {
		// This implementation is supposed to only work for non-hierarchical values, such as properties and references.
		// hierarchical values must override it.
		if (recursive) {
			accept(visitor);
		} else {
			visitor.visit(this);
		}
	}
	
    public abstract void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope);
		
	/**
	 * Returns true if this and other value represent the same value.
	 * E.g. if they have the same IDs, OIDs or it is otherwise know
	 * that they "belong together" without a deep examination of the
	 * values.
	 */
	public boolean representsSameValue(PrismValue other) {
		return false;
	}
	
	public static <V extends PrismValue> boolean containsRealValue(Collection<V> collection, V value) {
		if (collection == null) {
			return false;
		}
		for (V colVal: collection) {
			if (colVal.equalsRealValue(value)) {
				return true;
			}
		}
		return false;
	}
	
	public static <V extends PrismValue> boolean equalsRealValues(Collection<V> collection1, Collection<V> collection2) {
		Comparator comparator = new Comparator<V>() {
			@Override
			public int compare(V v1, V v2) {
				if (v1.equalsRealValue(v2)) {
					return 0;
				};
				return 1;
			}
		};
		return MiscUtil.unorderedCollectionEquals(collection1, collection2, comparator);
	}

	@Override
	public void normalize() {
		// do nothing by default
	}

	public static <X extends PrismValue> Collection<X> cloneValues(Collection<X> values) {
		Collection<X> clonedCollection = new ArrayList<X>(values.size());
		for (X val: values) {
			clonedCollection.add((X) val.clone());
		}
		return clonedCollection;
	}

	public abstract PrismValue clone();
	
	protected void copyValues(PrismValue clone) {
		clone.originType = this.originType;
		clone.originObject = this.originObject;
		// Do not clone parent. The clone will most likely go to a different prism
		// and setting the parent will make it difficult to add it there.
		clone.parent = null;
		// Do not clone immutable flag.
	}

	@NotNull
	public static <T extends PrismValue> Collection<T> cloneCollection(Collection<T> values) {
		Collection<T> clones = new ArrayList<T>();
		if (values != null) {
			for (T value : values) {
				clones.add((T) value.clone());
			}
		}
		return clones;
	}
	
	/**
     * Sets all parents to null. This is good if the items are to be "transplanted" into a
     * different Containerable.
     */
	public static <T extends PrismValue> Collection<T> resetParentCollection(Collection<T> values) {
    	for (T value: values) {
    		value.setParent(null);
    	}
    	return values;
	}

	@Override
	public int hashCode() {
		int result = 1;
		return result;
	}
	
	public boolean equalsComplex(PrismValue other, boolean ignoreMetadata, boolean isLiteral) {
		// parent is not considered at all. it is not relevant.
		// neither the immutable flag
		if (!ignoreMetadata) {
			if (originObject == null) {
				if (other.originObject != null)
					return false;
			} else if (!originObject.equals(other.originObject))
				return false;
			if (originType != other.originType)
				return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(PrismValue otherValue, boolean ignoreMetadata) {
		return equalsComplex(otherValue, ignoreMetadata, false);
	}
	
	public boolean equals(PrismValue thisValue, PrismValue otherValue) {
		if (thisValue == null && otherValue == null) {
			return true;
		}
		if (thisValue == null || otherValue == null) {
			return false;
		}
		return thisValue.equalsComplex(otherValue, false, false);
	}
	
	public boolean equalsRealValue(PrismValue otherValue) {
		return equalsComplex(otherValue, true, false);
	}
	
	public boolean equalsRealValue(PrismValue thisValue, PrismValue otherValue) {
		if (thisValue == null && otherValue == null) {
			return true;
		}
		if (thisValue == null || otherValue == null) {
			return false;
		}
		return thisValue.equalsComplex(otherValue, true, false);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrismValue other = (PrismValue) obj;
		return equalsComplex(other, false, false);
	}
	
	/**
	 * Assumes matching representations. I.e. it assumes that both this and otherValue represent the same instance of item.
	 * E.g. the container with the same ID. 
	 */
	@Override
	public Collection<? extends ItemDelta> diff(PrismValue otherValue) {
		return diff(otherValue, true, false);
	}
	
	/**
	 * Assumes matching representations. I.e. it assumes that both this and otherValue represent the same instance of item.
	 * E.g. the container with the same ID. 
	 */
	@Override
	public Collection<? extends ItemDelta> diff(PrismValue otherValue, boolean ignoreMetadata, boolean isLiteral) {
		Collection<? extends ItemDelta> itemDeltas = new ArrayList<ItemDelta>();
		diffMatchingRepresentation(otherValue, itemDeltas, ignoreMetadata, isLiteral);
		return itemDeltas;
	}
	
	void diffMatchingRepresentation(PrismValue otherValue,
			Collection<? extends ItemDelta> deltas, boolean ignoreMetadata, boolean isLiteral) {
		// Nothing to do by default
	}

	protected void appendOriginDump(StringBuilder builder) {
		if (DebugUtil.isDetailedDebugDump()) {
	        if (getOriginType() != null || getOriginObject() != null) {
		        builder.append(", origin: ");
		        builder.append(getOriginType());
		        builder.append(":");
		        builder.append(getOriginObject());
	        }
		}
	}

    public static <T> Set<T> getRealValuesOfCollection(Collection<PrismPropertyValue<T>> collection) {
        Set<T> retval = new HashSet<T>(collection.size());
        for (PrismPropertyValue<T> value : collection) {
            retval.add(value.getValue());
        }
        return retval;
    }


	public static <V extends PrismValue> boolean collectionContainsEquivalentValue(Collection<V> collection, V value) {
		if (collection == null) {
			return false;
		}
		for (V collectionVal: collection) {
			if (collectionVal.equals(value, true)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean isImmutable() {
		return immutable;
	}

	public void setImmutable(boolean immutable) {
		this.immutable = immutable;
	}

	protected void checkMutability() {
		if (immutable) {
			throw new IllegalStateException("An attempt to modify an immutable value of " + toHumanReadableString());
		}
	}

	@Nullable
	abstract public Class<?> getRealClass();

}
