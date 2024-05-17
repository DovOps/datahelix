/*
 * Copyright 2019 Scott Logic Ltd
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

package com.scottlogic.datahelix.generator.core.decisiontree.treepartitioning;

import com.scottlogic.datahelix.generator.core.decisiontree.DecisionNode;
import com.scottlogic.datahelix.generator.core.fieldspecs.relations.FieldSpecRelation;
import com.scottlogic.datahelix.generator.core.profile.constraints.atomic.AtomicConstraint;

import java.util.Objects;

class RootLevelConstraint {
    private Object constraint;

    RootLevelConstraint(DecisionNode decisionNode) {
        constraint = decisionNode;
    }

    RootLevelConstraint(AtomicConstraint atomicConstraint) {
        constraint = atomicConstraint;
    }

    RootLevelConstraint(FieldSpecRelation fieldSpecRelation) {
        constraint = fieldSpecRelation;
    }

    DecisionNode getDecisionNode() {
        return constraint instanceof DecisionNode dn
            ? dn
            : null;
    }

    AtomicConstraint getAtomicConstraint() {
        return constraint instanceof AtomicConstraint ac
            ? ac
            : null;
    }

    FieldSpecRelation getRelations() {
        return constraint instanceof FieldSpecRelation fsr
            ? fsr
            : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RootLevelConstraint that = (RootLevelConstraint) o;
        return Objects.equals(constraint, that.constraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraint);
    }
}
