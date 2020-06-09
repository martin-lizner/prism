/*
 * Copyright (c) 2020 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.axiom.lang.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.evolveum.axiom.api.AxiomItem;
import com.evolveum.axiom.api.AxiomName;
import com.evolveum.axiom.api.schema.AxiomIdentifierDefinition;
import com.evolveum.axiom.api.schema.AxiomItemDefinition;
import com.evolveum.axiom.api.schema.AxiomTypeDefinition;
import com.evolveum.axiom.concepts.Lazy;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class AxiomBuiltIn {

    public static final Lazy<Map<AxiomName, AxiomItemDefinition>> EMPTY = Lazy.instant(ImmutableMap.of());
    public static final Lazy<AxiomItemDefinition> NO_ARGUMENT = Lazy.nullValue();


    private AxiomBuiltIn() {
        throw new UnsupportedOperationException("Utility class");
    }



    public static class Item implements AxiomItemDefinition {
        public static final Item NAME = new Item("name", Type.IDENTIFIER, true);
        public static final Item ARGUMENT = new Item("argument", Type.IDENTIFIER, false);
        public static final AxiomItemDefinition DOCUMENTATION = new Item("documentation", Type.STRING, true);
        public static final AxiomItemDefinition NAMESPACE = new Item("namespace", Type.STRING, true);
        public static final AxiomItemDefinition VERSION = new Item("version", Type.STRING, true);
        public static final AxiomItemDefinition TYPE_REFERENCE = new Item("type", Type.TYPE_REFERENCE, true);
        public static final AxiomItemDefinition TYPE_DEFINITION = new Item("type", Type.TYPE_DEFINITION, false);

        public static final AxiomItemDefinition SUPERTYPE_REFERENCE = new Item("superType", Type.TYPE_REFERENCE, false);
        public static final Item ROOT_DEFINITION = new Item("root", Type.ROOT_DEFINITION, false);
        public static final AxiomItemDefinition ITEM_DEFINITION = new Item("item", Type.ITEM_DEFINITION, false) {

            @Override
            public Optional<AxiomIdentifierDefinition> identifierDefinition() {
                return Optional.of(NAME_IDENTIFIER.get());
            }
        };
        public static final Item MODEL_DEFINITION = new Item("model", Type.MODEL, false);
        public static final AxiomItemDefinition MIN_OCCURS = new Item("minOccurs", Type.STRING, false);
        public static final AxiomItemDefinition MAX_OCCURS = new Item("maxOccurs", Type.STRING, false);
        public static final AxiomItemDefinition TARGET_TYPE = new Item("targetType", Type.IDENTIFIER, true);
        public static final AxiomItemDefinition OPERATIONAL = new Item("operational", Type.IDENTIFIER, true);

        public static final AxiomItemDefinition IDENTIFIER_DEFINITION = new Item("identifier", Type.IDENTIFIER_DEFINITION, true);

        public static final AxiomItemDefinition ID_MEMBER = new Item("key", Type.STRING, false);
        public static final AxiomItemDefinition ID_SCOPE = new Item("scope", Type.STRING, false);
        public static final AxiomItemDefinition ID_SPACE = new Item("space", Type.IDENTIFIER, false);

        public static final AxiomItemDefinition TARGET = new Item("target", Type.TYPE_REFERENCE, true);
        public static final AxiomItemDefinition REF_TARGET = new Item("target", Type.TYPE_DEFINITION, true);
        public static final AxiomItemDefinition USES = new Item("uses", Type.TYPE_REFERENCE, true);

        public static final AxiomItemDefinition VALUE = new Item("value", null, true);

        protected static final Lazy<AxiomIdentifierDefinition> NAME_IDENTIFIER = Lazy.from(
                ()-> (AxiomIdentifierDefinition.parent(ITEM_DEFINITION.name(), Item.NAME.name())));

        private final AxiomName identifier;
        private final AxiomTypeDefinition type;
        private boolean required;


        private Item(String identifier, AxiomTypeDefinition type, boolean required) {
            this.identifier = AxiomName.axiom(identifier);
            this.type = type;
            this.required = required;
        }

        @Override
        public Optional<AxiomTypeDefinition> type() {
            return Optional.of(type);
        }

        @Override
        public AxiomName name() {
            return identifier;
        }

        @Override
        public String documentation() {
            return "";
        }


        @Override
        public AxiomTypeDefinition typeDefinition() {
            return type;
        }

        @Override
        public boolean required() {
            return required;
        }

        @Override
        public int minOccurs() {
            return 0;
        }

        @Override
        public boolean operational() {
            return false;
        }

        @Override
        public int maxOccurs() {
            return Integer.MAX_VALUE;
        }

        @Override
        public String toString() {
            return AxiomItemDefinition.toString(this);
        }

        @Override
        public AxiomTypeDefinition definingType() {
            return null;
        }

        @Override
        public Optional<AxiomIdentifierDefinition> identifierDefinition() {
            return Optional.empty();
        }

        @Override
        public Map<AxiomName, AxiomItem<?>> itemMap() {
            return null;
        }

        @Override
        public Map<AxiomName, AxiomItem<?>> infraItems() {
            return null;
        }
    }

    public static class Type implements AxiomTypeDefinition {
        public static final Type UUID = new Type("uuid");
        public static final Type STRING = new Type("string");
        public static final Type IDENTIFIER = new Type("AxiomName");

        public static final Type TYPE_REFERENCE = new Type("AxiomTypeReference", null, () -> Item.NAME, () -> itemDefs(
                    Item.NAME,
                    Item.REF_TARGET
                ));
        public static final Type BASE_DEFINITION =
                new Type("AxiomBaseDefinition", null, () -> Item.NAME, () -> itemDefs(
                        Item.NAME,
                        Item.DOCUMENTATION
                ));

        public static final Type MODEL =
                new Type("AxiomModel", BASE_DEFINITION,  () -> itemDefs(
                    Item.NAMESPACE,
                    Item.VERSION,
                    Item.TYPE_DEFINITION,
                    Item.ROOT_DEFINITION
                ));


        public static final Type TYPE_DEFINITION =
                new Type("AxiomTypeDefinition", BASE_DEFINITION, () -> itemDefs(
                    Item.ARGUMENT,
                    Item.SUPERTYPE_REFERENCE,
                    Item.ITEM_DEFINITION
                ));


        public static final Type ITEM_DEFINITION =
                new Type("AxiomItemDefinition", BASE_DEFINITION, () -> itemDefs(
                    Item.TYPE_REFERENCE,
                    Item.IDENTIFIER_DEFINITION,
                    Item.MIN_OCCURS,
                    Item.MAX_OCCURS,
                    Item.OPERATIONAL
                ));

        public static final Type ROOT_DEFINITION = new Type("AxiomRootDefinition", ITEM_DEFINITION);

        public static final Type IDENTIFIER_DEFINITION =
                new Type("AxiomIdentifierDefinition", BASE_DEFINITION, () -> Item.ID_MEMBER, () -> itemDefs(
                    Item.ID_MEMBER,
                    Item.ID_SCOPE,
                    Item.ID_SPACE
                ));
        public static final Type IMPORT_DEFINITION = new Type("AxiomImportDeclaration");
        public static final Type AUGMENTATION_DEFINITION = new Type("AxiomAugmentationDefinition",TYPE_DEFINITION);

        public static final Type AXIOM_VALUE = new Type("AxiomValue", null, () -> itemDefs(
                Item.TYPE_REFERENCE,
                Item.VALUE
                ));

        private final AxiomName identifier;
        private final AxiomTypeDefinition superType;
        private final Lazy<AxiomItemDefinition> argument;
        private final Lazy<Map<AxiomName, AxiomItemDefinition>> items;


        private Type(String identifier) {
            this(identifier, null, Lazy.nullValue(), EMPTY);
        }

        private Type(String identifier, Lazy.Supplier<Map<AxiomName, AxiomItemDefinition>> items) {
            this(identifier, null, Lazy.nullValue(), Lazy.from(items));
        }

        private Type(String identifier, AxiomTypeDefinition superType) {
            this(identifier, superType, NO_ARGUMENT, EMPTY);
        }

        private Type(String identifier, AxiomTypeDefinition superType, Lazy.Supplier<Map<AxiomName, AxiomItemDefinition>> items) {
            this(identifier, superType, NO_ARGUMENT, Lazy.from(items));
        }


        private Type(String identifier, AxiomTypeDefinition superType, Lazy.Supplier<AxiomItemDefinition> argument,
                Lazy.Supplier<Map<AxiomName, AxiomItemDefinition>> items) {
            this(identifier, superType, Lazy.from(argument), Lazy.from(items));
        }

        private Type(String identifier, AxiomTypeDefinition superType, Lazy<AxiomItemDefinition> argument,
                Lazy<Map<AxiomName, AxiomItemDefinition>> items) {
            this.identifier = AxiomName.axiom(identifier);
            this.argument = argument;
            this.superType = superType;
            this.items = items;
        }

        @Override
        public AxiomName name() {
            return identifier;
        }

        @Override
        public String documentation() {
            return "";
        }

        @Override
        public Optional<AxiomTypeDefinition> superType() {
            return Optional.ofNullable(superType);
        }

        @Override
        public Map<AxiomName, AxiomItemDefinition> itemDefinitions() {
            return items.get();
        }

        private static Map<AxiomName, AxiomItemDefinition> itemDefs(AxiomItemDefinition... items) {
            Builder<AxiomName, AxiomItemDefinition> builder = ImmutableMap.builder();

            for (AxiomItemDefinition item : items) {
                builder.put(item.name(), item);
            }
            return builder.build();
        }

        @Override
        public Collection<AxiomIdentifierDefinition> identifierDefinitions() {
            return Collections.emptyList();
        }

        @Override
        public Optional<AxiomItemDefinition> argument() {
            if(argument.get() != null) {
                return Optional.of(argument.get());
            }
            if(superType != null) {
                return superType.argument();
            }
            return Optional.empty();
        }

        @Override
        public String toString() {
            // TODO Auto-generated method stub
            return "typedef " + name();
        }

        @Override
        public Map<AxiomName, AxiomItem<?>> itemMap() {
            return null;
        }

        @Override
        public boolean isComplex() {
            if(superType != null && superType.isComplex()) {
                return true;
            }
            return !itemDefinitions().isEmpty();
        }

        @Override
        public Map<AxiomName, AxiomItem<?>> infraItems() {
            return null;
        }
    }


}
