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

package com.scottlogic.datahelix.generator.core.fieldspecs;

import com.scottlogic.datahelix.generator.common.whitelist.DistributedList;
import com.scottlogic.datahelix.generator.common.whitelist.WeightedElement;
import com.scottlogic.datahelix.generator.core.restrictions.bool.BooleanRestrictionsMerger;
import com.scottlogic.datahelix.generator.core.restrictions.string.StringRestrictionsMerger;
import com.scottlogic.datahelix.generator.core.restrictions.TypedRestrictions;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictionsMerger;
import com.scottlogic.datahelix.generator.common.SetUtils;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Returns a FieldSpec that permits only data permitted by all of its inputs
 */
public class FieldSpecMerger {
    private final RestrictionsMergeOperation restrictionMergeOperation =
        new RestrictionsMergeOperation(new LinearRestrictionsMerger(), new StringRestrictionsMerger(), new BooleanRestrictionsMerger());

    /**
     * Null parameters are permitted, and are synonymous with an empty FieldSpec
     * <p>
     * Returning an empty Optional conveys that the fields were unmergeable.
     */
    public Optional<FieldSpec> merge(FieldSpec left, FieldSpec right, boolean useFinestGranularityAvailable) {
        if (nullOnly(left) || nullOnly(right)){
            return nullOnlyOrEmpty(bothAreNullable(left, right));
        }

        if (hasSet(left) && hasSet(right)) {
            return mergeSets((WhitelistFieldSpec) left, (WhitelistFieldSpec)right);
        }
        if (hasSet(left)) {
            return combineSetWithRestrictions((WhitelistFieldSpec)left, right);
        }
        if (hasSet(right)) {
            return combineSetWithRestrictions((WhitelistFieldSpec)right, left);
        }

        if (isGenerator(left)) {
            if (isGenerator(right)){
                throw new UnsupportedOperationException("generators cannot be combined");
            }
            return addNullability(left.isNullable(), right.isNullable(), left);
        }
        if (isGenerator(right)){
            return addNullability(right.isNullable(), left.isNullable(), right);
        }
        return combineRestrictions((RestrictionsFieldSpec)left, (RestrictionsFieldSpec)right, useFinestGranularityAvailable);
    }

    private static WeightedElement<Object> mergeElements(WeightedElement<Object> left,
                                                         WeightedElement<Object> right) {
        return new WeightedElement<>(left.getElement(), left.getWeight() + right.getWeight());
    }

    //TODO try a performance test with this replaced with combineSetWithRestrictions()

    private Optional<FieldSpec> mergeSets(WhitelistFieldSpec left, WhitelistFieldSpec right) {
        DistributedList<Object> set = new DistributedList<>(left.getWhitelist().distributedList().stream()
            .flatMap(leftHolder -> right.getWhitelist().distributedList().stream()
                .filter(rightHolder -> elementsEqual(leftHolder, rightHolder))
                .map(rightHolder -> mergeElements(leftHolder, rightHolder)))
            .distinct()
            .collect(Collectors.toList()));

        FieldSpec newFieldSpec = set.isEmpty() ? FieldSpecFactory.nullOnly() : FieldSpecFactory.fromList(set);
        return addNullability(left.isNullable(), right.isNullable(), newFieldSpec);
    }
    private static <T> boolean elementsEqual(WeightedElement<T> left, WeightedElement<T> right) {
        return left.getElement().equals(right.getElement());
    }

    private Optional<FieldSpec> combineSetWithRestrictions(WhitelistFieldSpec set, FieldSpec restrictions) {
        DistributedList<Object> newSet = new DistributedList<>(
            set.getWhitelist().distributedList().stream()
                .filter(holder -> restrictions.canCombineWithWhitelistValue(holder.getElement()))
                .distinct()
                .collect(Collectors.toList()));

        FieldSpec newSpec = newSet.isEmpty() ? FieldSpecFactory.nullOnly() : FieldSpecFactory.fromList(newSet);
        return addNullability(set.isNullable(), restrictions.isNullable(), newSpec);
    }

    private Optional<FieldSpec> addNullability(boolean leftIsNullable, boolean rightIsNullable, FieldSpec newFieldSpec) {
        if (leftIsNullable && rightIsNullable) {
            return Optional.of(newFieldSpec);
        }

        if (nullOnly(newFieldSpec)) {
            return Optional.empty();
        }

        return Optional.of(newFieldSpec.withNotNull());
    }

    private boolean nullOnly(FieldSpec fieldSpec) {
        return (fieldSpec instanceof NullOnlyFieldSpec);
    }

    private boolean isGenerator(FieldSpec fieldSpec) {
        return fieldSpec instanceof GeneratorFieldSpec;
    }

    private boolean hasSet(FieldSpec fieldSpec) {
        return fieldSpec instanceof WhitelistFieldSpec;
    }

    private boolean bothAreNullable(FieldSpec left, FieldSpec right) {
        return left.isNullable() && right.isNullable();
    }

    private Optional<FieldSpec> combineRestrictions(RestrictionsFieldSpec left, RestrictionsFieldSpec right, boolean useFinestGranularityAvailable) {
        Optional<TypedRestrictions> restrictions = restrictionMergeOperation.applyMergeOperation(left.getRestrictions(), right.getRestrictions(), useFinestGranularityAvailable);

        if (!restrictions.isPresent()){
            return nullOnlyOrEmpty(bothAreNullable(left, right));
        }

        RestrictionsFieldSpec merged = FieldSpecFactory.fromRestriction(restrictions.get());
        merged = merged.withBlacklist(SetUtils.union(left.getBlacklist(), right.getBlacklist()));

        return addNullability(left.isNullable(), right.isNullable(), merged);
    }

    private Optional<FieldSpec> nullOnlyOrEmpty(boolean nullable) {
        return nullable ? Optional.of(FieldSpecFactory.nullOnly()) : Optional.empty();
    }
}
