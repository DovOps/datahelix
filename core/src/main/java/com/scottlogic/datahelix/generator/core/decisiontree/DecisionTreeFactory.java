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

package com.scottlogic.datahelix.generator.core.decisiontree;

import com.scottlogic.datahelix.generator.core.fieldspecs.relations.FieldSpecRelation;
import com.scottlogic.datahelix.generator.core.profile.Profile;
import com.scottlogic.datahelix.generator.core.profile.constraints.Constraint;
import com.scottlogic.datahelix.generator.core.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.datahelix.generator.core.profile.constraints.grammatical.AndConstraint;
import com.scottlogic.datahelix.generator.core.profile.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.datahelix.generator.core.profile.constraints.grammatical.NegatedGrammaticalConstraint;
import com.scottlogic.datahelix.generator.core.profile.constraints.grammatical.OrConstraint;

import java.util.*;
import java.util.stream.Collectors;

public class DecisionTreeFactory {
    private final DecisionTreeSimplifier decisionTreeSimplifier = new DecisionTreeSimplifier();

    public DecisionTree analyse(Profile profile) {
        Iterator<ConstraintNode> nodes = profile.getConstraints().stream()
            .map(this::convertConstraint)
            .map(decisionTreeSimplifier::simplify)
            .iterator();

        return new DecisionTree(ConstraintNode.merge(nodes), profile.getFields());
    }

    private ConstraintNode convertConstraint(Constraint constraintToConvert) {
        if (constraintToConvert instanceof NegatedGrammaticalConstraint) {
            return convertNegatedConstraint(constraintToConvert);
        }
        else if (constraintToConvert instanceof AndConstraint constraint) {
            return convertAndConstraint(constraint);
        }
        else if (constraintToConvert instanceof OrConstraint constraint) {
            return convertOrConstraint(constraint);
        } else if (constraintToConvert instanceof ConditionalConstraint constraint) {
            return convertConditionalConstraint(constraint);
        } else if (constraintToConvert instanceof FieldSpecRelation relation) {
            return asConstraintNode(relation);
        } else {
            AtomicConstraint atomicConstraint = (AtomicConstraint) constraintToConvert;
            return asConstraintNode(atomicConstraint);
        }
    }

    private ConstraintNode convertNegatedConstraint(Object constraintToConvert) {
        Constraint negatedConstraint = ((NegatedGrammaticalConstraint) constraintToConvert).getNegatedConstraint();

        // ¬AND(X, Y, Z) reduces to OR(¬X, ¬Y, ¬Z)
        if (negatedConstraint instanceof AndConstraint constraint) {
            Collection<Constraint> subConstraints = constraint.getSubConstraints();

            return convertOrConstraint(
                new OrConstraint(negateEach(subConstraints)));
        }
        // ¬OR(X, Y, Z) reduces to AND(¬X, ¬Y, ¬Z)
        else if (negatedConstraint instanceof OrConstraint constraint) {
            Collection<Constraint> subConstraints = constraint.subConstraints;

            return convertAndConstraint(
                new AndConstraint(negateEach(subConstraints)));
        }
        // ¬IF(X, then: Y) reduces to AND(X, ¬Y)
        // ¬IF(X, then: Y, else: Z) reduces to OR(AND(X, ¬Y), AND(¬X, ¬Z))
        else if (negatedConstraint instanceof ConditionalConstraint conditional) {

            AndConstraint positiveNegation =
                new AndConstraint(conditional.condition, conditional.whenConditionIsTrue.negate());

            if (conditional.whenConditionIsFalse == null) {
                return convertAndConstraint(positiveNegation);
            }

            Constraint negativeNegation =
                new AndConstraint(conditional.condition.negate(), conditional.whenConditionIsFalse.negate());

            return convertOrConstraint(
                new OrConstraint(positiveNegation, negativeNegation));

        }
        // if we got this far, it must be an atomic constraint
        else {
            if (constraintToConvert instanceof FieldSpecRelation relation) {
                return asConstraintNode(relation);
            }
            AtomicConstraint atomicConstraint = (AtomicConstraint) constraintToConvert;
            return asConstraintNode(atomicConstraint);
        }
    }

    private ConstraintNode convertAndConstraint(AndConstraint constraintToConvert) {
        // AND(X, Y, Z) becomes a flattened list of constraint nodes
        Collection<Constraint> subConstraints = constraintToConvert.getSubConstraints();

        Iterator<ConstraintNode> iterator = subConstraints.stream().map(this::convertConstraint)
            .iterator();

        return ConstraintNode.merge(iterator);
    }

    private ConstraintNode convertOrConstraint(OrConstraint constraintToConvert) {
        // OR(X, Y, Z) becomes a decision node
        Collection<Constraint> subConstraints = constraintToConvert.subConstraints;

        Set<ConstraintNode> options = subConstraints.stream()
            .map(this::convertConstraint)
            .collect(Collectors.toSet());

        return asConstraintNode(new DecisionNode(options));
    }

    private ConstraintNode convertConditionalConstraint(ConditionalConstraint constraintToConvert) {
        Constraint ifConstraint = constraintToConvert.condition;
        Constraint thenConstraint = constraintToConvert.whenConditionIsTrue;
        Constraint elseConstraint = constraintToConvert.whenConditionIsFalse;

        OrConstraint convertedConstraint = new OrConstraint(
            new AndConstraint(ifConstraint, thenConstraint),
            elseConstraint == null ? ifConstraint.negate() : new AndConstraint(ifConstraint.negate(), elseConstraint));

        return convertOrConstraint(convertedConstraint);
    }

    private static List<Constraint> negateEach(Collection<Constraint> constraints) {
        return constraints.stream()
            .map(Constraint::negate)
            .collect(Collectors.toList());
    }

    private static ConstraintNode asConstraintNode(AtomicConstraint constraint) {
        return new ConstraintNodeBuilder()
            .addAtomicConstraints(Collections.singleton(constraint))
            .setDecisions(Collections.emptySet())
            .build();
    }

    private static ConstraintNode asConstraintNode(DecisionNode decision) {
        return new ConstraintNodeBuilder()
            .addAtomicConstraints(Collections.emptySet())
            .setDecisions(Collections.singleton(decision))
            .build();
    }

    private static ConstraintNode asConstraintNode(FieldSpecRelation relation) {
        return new ConstraintNodeBuilder()
            .addRelations(relation)
            .setDecisions(Collections.emptySet())
            .build();
    }
}
