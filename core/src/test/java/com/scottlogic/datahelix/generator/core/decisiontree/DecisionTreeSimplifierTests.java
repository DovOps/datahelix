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

import com.scottlogic.datahelix.generator.common.SetUtils;
import com.scottlogic.datahelix.generator.common.profile.Field;
import com.scottlogic.datahelix.generator.common.profile.ProfileFields;
import com.scottlogic.datahelix.generator.common.whitelist.DistributedList;
import com.scottlogic.datahelix.generator.common.whitelist.WeightedElement;
import com.scottlogic.datahelix.generator.core.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.datahelix.generator.core.profile.constraints.atomic.InSetConstraint;
import com.scottlogic.datahelix.generator.core.profile.constraints.atomic.IsNullConstraint;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.datahelix.generator.common.profile.FieldBuilder.createField;

class DecisionTreeSimplifierTests {
    // TODO: Simplifier tests needs fleshing out

    private static DistributedList<Object> setOf(Object... objects) {
        return new DistributedList<>(Stream.of(objects)
            .map(element -> new WeightedElement<>(element, 1.0F))
            .collect(Collectors.toList()));
    }

    @Test
    void simplify_decisionContainsSingleOptiontWithMatchingConstraintOnRootNode_doesNotSimplifyTree() {
        DecisionTree tree = new DecisionTree(
            new ConstraintNodeBuilder().addAtomicConstraints(SetUtils.setOf(
                new InSetConstraint(createField("Field 1"), setOf(1, 2),false),
                new IsNullConstraint(createField("Field 1")).negate()
            )).setDecisions(Collections.singleton(
                new DecisionNode(
                    Collections.singleton(
                        new ConstraintNodeBuilder().addAtomicConstraints(Collections.singleton(
                            new InSetConstraint(createField("Field 1"), setOf(1, 2),false)
                        )).setDecisions(Collections.emptySet()).build()
                    )
                )
            )).build(),
            new ProfileFields(
                new ArrayList<Field>() {{ add(createField("Field 1")); }}
            )
        );
        DecisionTreeSimplifier simplifier = new DecisionTreeSimplifier();

        final DecisionTree result = simplifier.simplify(tree);

        Assert.assertEquals(result.rootNode.getAtomicConstraints(), tree.getRootNode().getAtomicConstraints());
        Assert.assertTrue(result.rootNode.getDecisions().isEmpty());
    }

    @Test
    void simplify_decisionContainsSingleOptionWithDifferingConstraintOnRootNode_simplifiesDecision() {
        DecisionTree tree = new DecisionTree(
            new ConstraintNodeBuilder().addAtomicConstraints(SetUtils.setOf(
                new InSetConstraint(createField("Field 1"), setOf(1, 2),false),
                new IsNullConstraint(createField("Field 1")).negate()
            )).setDecisions(Collections.singleton(
                new DecisionNode(
                    Collections.singleton(
                        new ConstraintNodeBuilder().addAtomicConstraints(Collections.singleton(
                            new InSetConstraint(createField("Field 2"), setOf("A", "B"),false)
                        )).setDecisions(Collections.emptySet()).build()
                    )
                )
            )).build(),
            new ProfileFields(
                new ArrayList<Field>() {{ add(createField("Field 1")); }}
            )
        );
        DecisionTreeSimplifier simplifier = new DecisionTreeSimplifier();

        final DecisionTree result = simplifier.simplify(tree);

        final List<AtomicConstraint> expectedConstraints = Arrays.asList(
            new InSetConstraint(createField("Field 1"), setOf(1, 2),false),
            new IsNullConstraint(createField("Field 1")).negate(),
            new InSetConstraint(createField("Field 2"), setOf("A", "B"),false)
        );
        Assert.assertTrue(result.rootNode.getAtomicConstraints().containsAll(expectedConstraints));
        Assert.assertTrue(result.rootNode.getDecisions().isEmpty());
    }
}
