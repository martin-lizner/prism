/*
 * Copyright (c) 2010-2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.prism;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.xml.namespace.QName;

import org.jetbrains.annotations.NotNull;

import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.util.annotation.Experimental;
import com.evolveum.midpoint.util.exception.SchemaException;

/**
 * @author mederly
 */
public interface ItemDefinition<I extends Item> extends Definition, PrismItemAccessDefinition {

    @NotNull
    ItemName getItemName();

    String getNamespace();

    int getMinOccurs();

    int getMaxOccurs();

    default boolean isSingleValue() {
        int maxOccurs = getMaxOccurs();
        return maxOccurs >= 0 && maxOccurs <= 1;
    }

    default boolean isMultiValue() {
        int maxOccurs = getMaxOccurs();
        return maxOccurs < 0 || maxOccurs > 1;
    }

    boolean isMandatory();

    boolean isOptional();

    boolean isOperational();

    /**
     * EXPERIMENTAL. If true, this item is not stored in XML representation in repo.
     * TODO better name
     */
    @Experimental
    boolean isIndexOnly();

    /**
     * Whether an item is inherited from a supertype.
     */
    boolean isInherited();

    /**
     * Returns true if definition was created during the runtime based on a dynamic information
     * such as xsi:type attributes in XML. This means that the definition needs to be stored
     * alongside the data to have a successful serialization "roundtrip". The definition is not
     * part of any schema and therefore cannot be determined. It may even be different for every
     * instance of the associated item (element name).
     */
    boolean isDynamic();

    /**
     * Returns the name of an element this one can be substituted for (e.g. c:user -&gt; c:object,
     * s:pipeline -&gt; s:expression, etc). EXPERIMENTAL
     */
    @Experimental
    QName getSubstitutionHead();

    /**
     * Can be used in heterogeneous lists as a list item. EXPERIMENTAL.
     */
    @Experimental
    boolean isHeterogeneousListItem();

    PrismReferenceValue getValueEnumerationRef();

    boolean isValidFor(QName elementQName, Class<? extends ItemDefinition> clazz);

    boolean isValidFor(@NotNull QName elementQName, @NotNull Class<? extends ItemDefinition> clazz, boolean caseInsensitive);

    void adoptElementDefinitionFrom(ItemDefinition otherDef);

    /**
     * Create an item instance. Definition name or default name will
     * used as an element name for the instance. The instance will otherwise be empty.
     *
     * @return created item instance
     */
    @NotNull
    I instantiate() throws SchemaException;

    /**
     * Create an item instance. Definition name will use provided name.
     * for the instance. The instance will otherwise be empty.
     *
     * @return created item instance
     */
    @NotNull
    I instantiate(QName name) throws SchemaException;

    <T extends ItemDefinition> T findItemDefinition(@NotNull ItemPath path, @NotNull Class<T> clazz);

    ItemDelta createEmptyDelta(ItemPath path);

    @Override
    @NotNull
    ItemDefinition<I> clone();

    ItemDefinition<I> deepClone(boolean ultraDeep, Consumer<ItemDefinition> postCloneAction);

    ItemDefinition<I> deepClone(Map<QName, ComplexTypeDefinition> ctdMap, Map<QName, ComplexTypeDefinition> onThisPath, Consumer<ItemDefinition> postCloneAction);

    @Override
    void revive(PrismContext prismContext);

    /**
     * Used in debugDumping items. Does not need to have name in it as item already has it. Does not need
     * to have class as that is just too much info that is almost anytime pretty obvious anyway.
     */
    void debugDumpShortToString(StringBuilder sb);

    boolean canBeDefinitionOf(I item);

    boolean canBeDefinitionOf(PrismValue pvalue);

    @Override
    MutableItemDefinition<I> toMutable();


    /**
     * Returns complex type definition of item, if underlying value is possible structured.
     *
     * NOTE: This seems weird, since properties and references are simple values,
     * but actually object reference is serialized as structured value and some of properties
     * are also.
     *
     * @return
     */
    @Experimental
    Optional<ComplexTypeDefinition> structuredType();
}
