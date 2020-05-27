package com.evolveum.axiom.lang.api;

import java.util.Collection;
import java.util.Optional;

import com.evolveum.axiom.api.AxiomIdentifier;
import com.evolveum.axiom.api.AxiomItemDefinition;
import com.evolveum.axiom.api.AxiomTypeDefinition;

public interface AxiomSchemaContext {

    Collection<AxiomItemDefinition> roots();

    Optional<AxiomItemDefinition> getRoot(AxiomIdentifier type);

    Optional<AxiomTypeDefinition> getType(AxiomIdentifier type);

    Collection<AxiomTypeDefinition> types();
}
