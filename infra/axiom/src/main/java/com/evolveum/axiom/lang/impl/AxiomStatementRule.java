package com.evolveum.axiom.lang.impl;

import com.evolveum.axiom.api.AxiomIdentifier;
import com.evolveum.axiom.api.AxiomItem;
import com.evolveum.axiom.api.AxiomItemDefinition;
import com.evolveum.axiom.api.AxiomTypeDefinition;
import com.evolveum.axiom.api.AxiomValue;
import com.evolveum.axiom.lang.api.IdentifierSpaceKey;
import com.evolveum.axiom.lang.spi.AxiomSemanticException;
import com.evolveum.axiom.reactor.Dependency;

public interface AxiomStatementRule<V> {

    String name();

    boolean isApplicableTo(AxiomItemDefinition definition);

    void apply(Lookup<V> context, ActionBuilder<V> action) throws AxiomSemanticException;


    interface Lookup<V> {
        default AxiomTypeDefinition typeDefinition() {
            return itemDefinition().typeDefinition();
        }

        AxiomItemDefinition itemDefinition();

        Dependency<NamespaceContext> namespace(AxiomIdentifier name, IdentifierSpaceKey namespaceId);

        <T> Dependency<AxiomItem<T>> child(AxiomItemDefinition namespace, Class<T> valueType);

        Dependency<AxiomValueContext<?>> modify(AxiomIdentifier identifierSpace, IdentifierSpaceKey identifier);

        Dependency.Search<AxiomValue<?>> global(AxiomIdentifier identifierSpace, IdentifierSpaceKey identifier);

        Dependency.Search<AxiomValue<?>> namespaceValue(AxiomIdentifier space, IdentifierSpaceKey itemName);

        Dependency<V> finalValue();

        V currentValue();

        V originalValue();

        boolean isMutable();

        Lookup<?> parentValue();

        AxiomSemanticException error(String message, Object... arguments);
    }

    interface ActionBuilder<V> {


        AxiomSemanticException error(String message, Object... arguments);

        ActionBuilder<V> apply(Action<V> action);

        Dependency<AxiomValue<?>> require(AxiomValueContext<?> ext);



        <V,X extends Dependency<V>> X require(X req);

        /*<V> Optional<V> optionalChildValue(AxiomItemDefinition supertypeReference, Class<V> type);

        <V> V requiredChildValue(AxiomItemDefinition supertypeReference, Class<V> type) throws AxiomSemanticException;

        V requireValue() throws AxiomSemanticException;



        Context<V> errorMessage(Supplier<RuleErrorMessage> errorFactory);

        RuleErrorMessage error(String format, Object... arguments);



        Optional<V> optionalValue();

        Search<AxiomItemValue<?>> requireGlobal(AxiomIdentifier space, IdentifierSpaceKey key);

        Dependency<AxiomItemValue<?>> requireChild(AxiomItemDefinition required);

        Dependency<NamespaceContext> requireNamespace(AxiomIdentifier name, IdentifierSpaceKey namespaceId);

        Dependency<AxiomValueContext<?>> modify(AxiomIdentifier identifierSpace, IdentifierSpaceKey identifier);

        Dependency<AxiomItemValue<?>> require(AxiomValueContext<?> ext);*/
    }

    public interface Action<V> {

        void apply(AxiomValueContext<V> context) throws AxiomSemanticException;
    }
}
