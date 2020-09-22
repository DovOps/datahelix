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

package com.scottlogic.datahelix.generator.core.fieldspecs.relations;

import com.scottlogic.datahelix.generator.common.profile.Field;
import com.scottlogic.datahelix.generator.common.profile.FieldType;
import com.scottlogic.datahelix.generator.common.profile.Granularity;
import com.scottlogic.datahelix.generator.common.whitelist.WeightedElement;
import com.scottlogic.datahelix.generator.core.fieldspecs.*;
import com.scottlogic.datahelix.generator.common.whitelist.DistributedList;
import com.scottlogic.datahelix.generator.core.generation.databags.DataBagValue;
import com.scottlogic.datahelix.generator.core.profile.constraints.Constraint;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictions;

import java.util.List;
import java.util.stream.Collectors;

public class EqualToOffsetRelation<T extends Comparable<T>> implements FieldSpecRelation {
    private final Field main;
    private final Field other;
    private final Granularity<T> offsetGranularity;
    private final int offset;

    public EqualToOffsetRelation(Field main,
                                 Field other,
                                 Granularity<T> offsetGranularity,
                                 int offset) {
        this.main = main;
        this.other = other;
        this.offsetGranularity = offsetGranularity;
        this.offset = offset;
    }

    @Override
    public FieldSpec createModifierFromOtherFieldSpec(FieldSpec otherFieldSpec) {
        if (otherFieldSpec instanceof NullOnlyFieldSpec) {
            return FieldSpecFactory.nullOnly();
        }
        if (otherFieldSpec instanceof WhitelistFieldSpec) {
            WhitelistFieldSpec whitelistFieldSpec = (WhitelistFieldSpec) otherFieldSpec;
            List<WeightedElement<T>> modified = whitelistFieldSpec
                .getWhitelist()
                .distributedList()
                .stream()
                .map(x -> new WeightedElement<>(offsetGranularity.getNext((T) x.getElement(), offset), x.getWeight()))
                .collect(Collectors.toList());
            return FieldSpecFactory.fromList((DistributedList) new DistributedList<>(modified));
        }

        LinearRestrictions<T> otherRestrictions = (LinearRestrictions) ((RestrictionsFieldSpec) otherFieldSpec).getRestrictions();

        if (otherRestrictions.isContradictory()) {
            return FieldSpecFactory.nullOnly();
        }

        T min = otherRestrictions.getMin();
        T offsetMin = offsetGranularity.getNext(min, offset);
        T max = otherRestrictions.getMax();
        T offsetMax = offsetGranularity.getNext(max, offset);

        return FieldSpecFactory.fromRestriction(new LinearRestrictions(offsetMin, offsetMax, offsetGranularity));
    }

    @Override
    public FieldSpec createModifierFromOtherValue(DataBagValue otherFieldGeneratedValue) {
        T value = (T) otherFieldGeneratedValue.getValue();
        if (value == null) {
            return FieldSpecFactory.fromType(FieldType.DATETIME);
        }
        T offsetValue = offsetGranularity.getNext(value, offset);
        return FieldSpecFactory.fromList(DistributedList.singleton(offsetValue));
    }

    @Override
    public FieldSpecRelation inverse() {
        return new EqualToOffsetRelation(other, main, offsetGranularity, -offset);
    }

    @Override
    public Field main() {
        return main;
    }

    @Override
    public Field other() {
        return other;
    }

    @Override
    public Constraint negate() {
        throw new UnsupportedOperationException("Negating relations with an offset is not supported");
    }
}
