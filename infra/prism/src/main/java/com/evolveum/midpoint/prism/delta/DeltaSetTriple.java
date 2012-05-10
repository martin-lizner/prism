/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.prism.delta;

import com.evolveum.midpoint.prism.PrismPropertyValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.util.DebugDumpable;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.Dumpable;
import com.evolveum.midpoint.util.MiscUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * The triple of values (added, unchanged, deleted) that represents difference between two collections of values.
 * <p/>
 * The DeltaSetTriple is used as a result of a "diff" operation or it is constructed to determine a ObjectDelta or
 * PropertyDelta. It is a very useful structure in numerous situations when dealing with relative changes.
 * <p/>
 * DeltaSetTriple (similarly to other parts of this system) deal only with unordered values.
 *
 * @author Radovan Semancik
 */
public class DeltaSetTriple<T> implements Dumpable, DebugDumpable {

    /**
     * Collection of values that were not changed.
     */
    protected Collection<T> zeroSet;

    /**
     * Collection of values that were added.
     */
    protected Collection<T> plusSet;

    /**
     * Collection of values that were deleted.
     */
    protected Collection<T> minusSet;

    public DeltaSetTriple() {
        zeroSet = createSet();
        plusSet = createSet();
        minusSet = createSet();
    }

    public DeltaSetTriple(Collection<T> zeroSet, Collection<T> plusSet, Collection<T> minusSet) {
        this.zeroSet = zeroSet;
        this.plusSet = plusSet;
        this.minusSet = minusSet;
    }

    /**
     * Compares two (unordered) collections and creates a triple describing the differences.
     */
    public static <T> DeltaSetTriple<T> diff(Collection<T> valuesOld, Collection<T> valuesNew) {
        DeltaSetTriple<T> triple = new DeltaSetTriple<T>();
        diff(valuesOld, valuesNew, triple);
        return triple;
    }
    
    protected static <T> void diff(Collection<T> valuesOld, Collection<T> valuesNew, DeltaSetTriple<T> triple) {
        if (valuesOld == null && valuesNew == null) {
        	// No values, no change -> empty triple
        	return;
        }
        if (valuesOld == null) {
        	triple.getPlusSet().addAll(valuesNew);
        	return;
        }
        if (valuesNew == null) {
        	triple.getMinusSet().addAll(valuesOld);
        	return;
        }
        for (T val : valuesOld) {
            if (valuesNew.contains(val)) {
                triple.getZeroSet().add(val);
            } else {
                triple.getMinusSet().add(val);
            }
        }
        for (T val : valuesNew) {
            if (!valuesOld.contains(val)) {
                triple.getPlusSet().add(val);
            }
        }
    }

    protected Collection<T> createSet() {
        return new ArrayList<T>();
    }

    public Collection<T> getZeroSet() {
        return zeroSet;
    }

    public Collection<T> getPlusSet() {
        return plusSet;
    }

    public Collection<T> getMinusSet() {
        return minusSet;
    }
    
    public boolean hasPlusSet() {
    	return (plusSet != null && !plusSet.isEmpty());
    }

    public boolean hasZeroSet() {
    	return (zeroSet != null && !zeroSet.isEmpty());
    }

    public boolean hasMinusSet() {
    	return (minusSet != null && !minusSet.isEmpty());
    }
    
    public void addToPlusSet(T item) {
    	if (plusSet == null) {
    		plusSet = createSet();
    	}
    	plusSet.add(item);
    }

    public void addToMinusSet(T item) {
    	if (minusSet == null) {
    		minusSet = createSet();
    	}
    	minusSet.add(item);
    }

    public void addToZeroSet(T item) {
    	if (zeroSet == null) {
    		zeroSet = createSet();
    	}
    	zeroSet.add(item);
    }

    public void addAllToPlusSet(Collection<T> items) {
    	if (plusSet == null) {
    		plusSet = createSet();
    	}
    	plusSet.addAll(items);
    }

    public void addAllToMinusSet(Collection<T> items) {
    	if (minusSet == null) {
    		minusSet = createSet();
    	}
    	minusSet.addAll(items);
    }

    public void addAllToZeroSet(Collection<T> items) {
    	if (zeroSet == null) {
    		zeroSet = createSet();
    	}
    	zeroSet.addAll(items);
    }

    /**
     * Returns all values, regardless of the internal sets.
     */
    public Collection<T> union() {
        return MiscUtil.union(zeroSet, plusSet, minusSet);
    }

    public Collection<T> getNonNegativeValues() {
        return MiscUtil.union(zeroSet, plusSet);
    }
    
	public void merge(DeltaSetTriple<T> triple) {
		zeroSet.addAll(triple.zeroSet);
		plusSet.addAll(triple.plusSet);
		minusSet.addAll(triple.minusSet);
	}
	
	@Override
    public String toString() {
        return dump();
    }

    @Override
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append(debugName()).append("(");
        dumpSet(sb, "zero", zeroSet);
        dumpSet(sb, "plus", plusSet);
        dumpSet(sb, "minus", minusSet);
        sb.append(")");
        return sb.toString();
    }
    
    protected String debugName() {
    	return "DeltaSetTriple";
    }

    private void dumpSet(StringBuilder sb, String label, Collection<T> set) {
        sb.append(label).append(": ").append(set).append("; ");
    }

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.util.DebugDumpable#debugDump()
	 */
	@Override
	public String debugDump() {
		return debugDump(0);
	}

	/* (non-Javadoc)
	 * @see com.evolveum.midpoint.util.DebugDumpable#debugDump(int)
	 */
	@Override
	public String debugDump(int indent) {
		StringBuilder sb = new StringBuilder();
		DebugUtil.indentDebugDump(sb, indent);
        sb.append("DeltaSetTriple:\n");
        debugDumpSet(sb, "zero", zeroSet, indent + 1);
        sb.append("\n");
        debugDumpSet(sb, "plus", plusSet, indent + 1);
        sb.append("\n");
        debugDumpSet(sb, "minus", minusSet, indent + 1);
        return sb.toString();
	}

	private void debugDumpSet(StringBuilder sb, String label, Collection<T> set, int indent) {
		DebugUtil.indentDebugDump(sb, indent);
		sb.append(label).append(":");
		if (set == null) {
			sb.append(" null");
		} else {
			for (T val: set) {
				sb.append("\n");
				sb.append(DebugUtil.debugDump(val, indent +1));
			}
		}
	}

}
