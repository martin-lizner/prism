/*
 * Copyright (c) 2010-2018 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.prism.impl;

import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.ItemDefinitionTransformer.TransformableValue;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.equivalence.EquivalenceStrategy;
import com.evolveum.midpoint.prism.equivalence.ParameterizedEquivalenceStrategy;
import com.evolveum.midpoint.prism.impl.metadata.ValueMetadataAdapter;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.DebugUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * @author semancik
 *
 */
public abstract class PrismValueImpl extends AbstractFreezable implements PrismValue, TransformableValue {

    private OriginType originType;
    private Objectable originObject;
    private Itemable parent;

    private ValueMetadata valueMetadata;

    @SuppressWarnings("FieldMayBeFinal") // Cannot be final because it is transient
    private transient Map<String,Object> userData = new HashMap<>();

    // FIXME: always null
    protected EquivalenceStrategy defaultEquivalenceStrategy;

    protected transient PrismContext prismContext;

    private boolean isTransient;

    PrismValueImpl() {
    }

    PrismValueImpl(PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    PrismValueImpl(OriginType type, Objectable source) {
        this(null, type, source);
    }

    PrismValueImpl(PrismContext prismContext, OriginType type, Objectable source) {
        this.prismContext = prismContext;
        this.originType = type;
        this.originObject = source;
    }

    PrismValueImpl(PrismContext prismContext, OriginType type, Objectable source, Itemable parent) {
        this.prismContext = prismContext;
        this.originType = type;
        this.originObject = source;
        this.parent = parent;
    }

    @Override
    public void setPrismContext(PrismContext prismContext) {
        this.prismContext = prismContext;
    }

    @Override
    public void setOriginObject(Objectable source) {
        this.originObject = source;
    }

    @Override
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

    @Override
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

    protected Object getPathComponent() {
        return null;
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

    @Override
    public PrismContext getPrismContext() {
        if (prismContext != null) {
            return prismContext;
        }
        if (parent != null) {
            prismContext = parent.getPrismContext();
            return prismContext;
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
        checkMutable();        // TODO reconsider
        applyDefinition(definition, true);
    }

    @Override
    public void applyDefinition(ItemDefinition definition, boolean force) throws SchemaException {
        checkMutable();        // TODO reconsider
        // Do nothing by default
    }

    @Override
    public void revive(PrismContext prismContext) throws SchemaException {
        if (this.prismContext == null) {
            this.prismContext = prismContext;
        }
        if (isMutable()) {
            recompute(prismContext);
        }
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
    public abstract void recompute(PrismContext prismContext);

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

    @Override
    public abstract void checkConsistenceInternal(Itemable rootItem, boolean requireDefinitions, boolean prohibitRaw, ConsistencyCheckScope scope);

    @Override
    public boolean representsSameValue(PrismValue other, boolean lax) {
        return false;
    }

    @Override
    public void normalize() {
        // do nothing by default
    }

    /**
     * Literal clone.
     */
    @Override
    public abstract PrismValue clone();

    @Override
    public PrismValue createImmutableClone() {
        PrismValue clone = clone();
        clone.freeze();
        return clone;
    }

    /**
     * Complex clone with different cloning strategies.
     * @see CloneStrategy
     */
    @Override
    public abstract PrismValue cloneComplex(CloneStrategy strategy);

    protected void copyValues(CloneStrategy strategy, PrismValueImpl clone) {
        clone.originType = this.originType;
        clone.originObject = this.originObject;
        // Do not clone parent. The clone will most likely go to a different prism
        // and setting the parent will make it difficult to add it there.
        clone.parent = null;
        // Do not clone immutable flag.
        if (clone.prismContext == null) {
            clone.prismContext = this.prismContext;
        }
        clone.valueMetadata = valueMetadata != null ? valueMetadata.clone() : null;
        clone.isTransient = isTransient;
    }

    private EquivalenceStrategy getEqualsHashCodeStrategy() {
        return defaultIfNull(defaultEquivalenceStrategy, EquivalenceStrategy.DATA);
    }

    @Override
    public int hashCode() {
        return hashCode(getEqualsHashCodeStrategy());
    }

    @Override
    public int hashCode(@NotNull ParameterizedEquivalenceStrategy equivalenceStrategy) {
        return 0;
    }

    @Override
    public int hashCode(@NotNull EquivalenceStrategy equivalenceStrategy) {
        return equivalenceStrategy.hashCode(this);
    }

    @Override
    public boolean equals(PrismValue otherValue, @NotNull EquivalenceStrategy equivalenceStrategy) {
        if (equivalenceStrategy instanceof ParameterizedEquivalenceStrategy) {   // todo or skip this check?
            return equals(otherValue, (ParameterizedEquivalenceStrategy) equivalenceStrategy);
        } else {
            return equivalenceStrategy.equals(this, otherValue);
        }
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(PrismValue other, @NotNull ParameterizedEquivalenceStrategy strategy) {
        // parent is not considered at all. it is not relevant.
        // neither the immutable flag
        // neither the value origin
        if (strategy.isConsideringValueMetadata()) {
            return getValueMetadata().equals(other.getValueMetadata(), strategy.exceptForValueMetadata());
        } else {
            return true;
        }
    }

    // original equals was "isLiteral = false"!
    @Override
    public boolean equals(Object other) {
        return this == other ||
                (other == null || other instanceof PrismValue) &&
                equals((PrismValue) other, getEqualsHashCodeStrategy());
    }

    /**
     * Assumes matching representations. I.e. it assumes that both this and otherValue represent the same instance of item.
     * E.g. the container with the same ID.
     */
    @Override
    public Collection<? extends ItemDelta> diff(PrismValue otherValue, ParameterizedEquivalenceStrategy strategy) {
        Collection<? extends ItemDelta> itemDeltas = new ArrayList<>();
        diffMatchingRepresentation(otherValue, itemDeltas, strategy);
        return itemDeltas;
    }

    public final void diffMatchingRepresentation(PrismValue otherValue,
            Collection<? extends ItemDelta> deltas, ParameterizedEquivalenceStrategy strategy) {
        diffMatchingRepresentation(otherValue, deltas, strategy, false);
    }

    public boolean diffMatchingRepresentation(PrismValue otherValue,
            Collection<? extends ItemDelta> deltas, ParameterizedEquivalenceStrategy strategy, boolean exitOnDiff) {
        // Nothing to do by default
        return false;
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

    @Override
    public abstract String toHumanReadableString();

    @Override
    @Nullable
    abstract public Class<?> getRealClass();

    @Override
    @Nullable
    abstract public <T> T getRealValue();

    // Returns a root of PrismValue tree. For example, if we have a AccessCertificationWorkItemType that has a parent (owner)
    // of AccessCertificationCaseType, which has a parent of AccessCertificationCampaignType, this method returns the PCV
    // of AccessCertificationCampaignType.
    //
    // Generally, this method returns either "this" (PrismValue) or a PrismContainerValue.
    @Override
    public PrismValue getRootValue() {
        PrismValue current = this;
        for (;;) {
            PrismContainerValue<?> parent = PrismValueUtil.getParentContainerValue(current);
            if (parent == null) {
                return current;
            }
            current = parent;
        }
    }

    @Override
    public PrismContainerValue<?> getParentContainerValue() {
        return PrismValueUtil.getParentContainerValue(this);
    }

    @Override
    public QName getTypeName() {
        ItemDefinition definition = getDefinition();
        return definition != null ? definition.getTypeName() : null;
    }

    // Path may contain ambiguous segments (e.g. assignment/targetRef when there are more assignments)
    // Note that the path can contain name segments only (at least for now)
    @Override
    @NotNull
    public Collection<PrismValue> getAllValues(ItemPath path) {
        if (path.isEmpty()) {
            return singleton(this);
        } else {
            return emptySet();
        }
    }

    @Override
    @NotNull
    public ValueMetadata getValueMetadata() {
        if (valueMetadata == null) {
            assert isMutable();
            valueMetadata = createEmptyMetadata();
        }
        return valueMetadata;
    }

    private ValueMetadata createEmptyMetadata() {
        if (prismContext != null && prismContext.getValueMetadataFactory() != null) {
            return prismContext.getValueMetadataFactory().createEmpty();
        } else {
            return ValueMetadataAdapter.holding(new PrismContainerImpl<>(PrismConstants.VALUE_METADATA_CONTAINER_NAME));
        }
    }

    @Override
    public boolean hasValueMetadata() {
        return valueMetadata != null && valueMetadata.hasAnyValue();
    }

    @Override
    public void setValueMetadata(ValueMetadata valueMetadata) {
        checkMutable();
        if (valueMetadata != null) {
            valueMetadata.checkConsistence(ConsistencyCheckScope.MANDATORY_CHECKS_ONLY); // TODO optimize
        }
        this.valueMetadata = valueMetadata;
    }

    @Override
    public void setValueMetadata(PrismContainer<?> valueMetadataContainer) {
        if (valueMetadataContainer != null) {
            setValueMetadata(ValueMetadataAdapter.holding(valueMetadataContainer));
        } else {
            setValueMetadata((ValueMetadata) null);
        }
    }

    @Override
    public void setValueMetadata(Containerable realValue) throws SchemaException {
        if (realValue != null) {
            ValueMetadata newMetadata = createEmptyMetadata();
            //noinspection unchecked
            newMetadata.add(realValue.asPrismContainerValue());
            setValueMetadata(newMetadata);
        } else {
            setValueMetadata((ValueMetadata) null);
        }
    }

    @Override
    protected void performFreeze() {
        getValueMetadata(); // to create empty metadata if there's none
        if (valueMetadata != null) {
            valueMetadata.freeze();
        }
        super.performFreeze();
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public void setTransient(boolean value) {
        isTransient = value;
    }

}
