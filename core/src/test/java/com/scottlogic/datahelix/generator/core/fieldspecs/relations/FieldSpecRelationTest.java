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

import com.scottlogic.datahelix.generator.common.profile.*;
import com.scottlogic.datahelix.generator.common.util.defaults.DateTimeDefaults;
import com.scottlogic.datahelix.generator.common.whitelist.DistributedList;
import com.scottlogic.datahelix.generator.core.fieldspecs.FieldSpec;
import com.scottlogic.datahelix.generator.core.fieldspecs.FieldSpecFactory;
import com.scottlogic.datahelix.generator.core.fieldspecs.RestrictionsFieldSpec;
import com.scottlogic.datahelix.generator.core.fieldspecs.WhitelistFieldSpec;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.datahelix.generator.common.profile.FieldBuilder.createField;
import static com.scottlogic.datahelix.generator.common.util.Defaults.ISO_MAX_DATE;
import static com.scottlogic.datahelix.generator.common.util.Defaults.ISO_MIN_DATE;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.YEARS;
import static org.junit.Assert.assertEquals;

class FieldSpecRelationTest {
    private Field main = createField("main", StandardSpecificFieldType.DATETIME.toSpecificFieldType());
    private Field other = createField("other", StandardSpecificFieldType.DATETIME.toSpecificFieldType());

    @Test
    public void equalTo_exactValue_returnsSame() {
        FieldSpec fieldSpec = forYears(2018, 2018);
        EqualToRelation relation = new EqualToRelation(main, other);

        FieldSpec actual = relation.createModifierFromOtherFieldSpec(fieldSpec);
        FieldSpec expected = fieldSpec;

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void equalTo_range_returnsSame() {
        FieldSpec fieldSpec = forYears(2018, 2020);
        EqualToRelation relation = new EqualToRelation(main, other);

        FieldSpec actual = relation.createModifierFromOtherFieldSpec(fieldSpec);
        FieldSpec expected = fieldSpec;

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void afterOrAt_exactValue_returnsBetween() {
        FieldSpec fieldSpec = forYears(2018, 2018);
        DateTimeGranularity offsetGranularity = DateTimeGranularity.create("MILLIS");
        AfterRelation relation = new AfterRelation(main, other, true, DateTimeDefaults.get(), offsetGranularity, 0);

        FieldSpec actual = relation.createModifierFromOtherFieldSpec(fieldSpec);
        FieldSpec expected = fromMin(2018);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void afterOrAt_range_returnsFromMin() {
        FieldSpec fieldSpec = forYears(2018, 2020);
        DateTimeGranularity offsetGranularity = DateTimeGranularity.create("MILLIS");
        AfterRelation relation = new AfterRelation(main, other, true, DateTimeDefaults.get(), offsetGranularity, 0);

        FieldSpec actual = relation.createModifierFromOtherFieldSpec(fieldSpec);
        FieldSpec expected = fromMin(2018);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test

    public void after_range_returnsFromMin() {
        int minYear = 2018;
        DateTimeGranularity offsetGranularity = DateTimeGranularity.create("MILLIS");
        FieldSpec fieldSpec = forYears(minYear, minYear + 4);
        AfterRelation relation = new AfterRelation(main, other, false, DateTimeDefaults.get(), offsetGranularity, 0);

        RestrictionsFieldSpec actualFieldSpec = (RestrictionsFieldSpec) relation.createModifierFromOtherFieldSpec(fieldSpec);
        LinearRestrictions actualRestrictions = (LinearRestrictions) actualFieldSpec.getRestrictions();
        OffsetDateTime actualMin = (OffsetDateTime) actualRestrictions.getMin();
        OffsetDateTime actualMax = (OffsetDateTime) actualRestrictions.getMax();

        OffsetDateTime expectedMin = OffsetDateTime.of(minYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
            .plusNanos(1000000);  // Current smallest granularity is milliseconds.
        OffsetDateTime expectedMax = DateTimeDefaults.get().max();

        assertEquals(actualMin.compareTo(expectedMin), 0);
        assertEquals(actualMax.compareTo(expectedMax), 0);
    }

    @Test
    public void beforeOrAt_exactValue_returnsBetween() {
        FieldSpec fieldSpec = forYears(2018, 2018);
        DateTimeGranularity offsetGranularity = DateTimeGranularity.create("MILLIS");
        BeforeRelation relation = new BeforeRelation(main, other, true, DateTimeDefaults.get(), offsetGranularity, 0);

        FieldSpec actual = relation.createModifierFromOtherFieldSpec(fieldSpec);
        FieldSpec expected = fromMax(2018);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void beforeOrAt_range_returnsFromMin() {
        FieldSpec fieldSpec = forYears(2018, 2020);
        DateTimeGranularity offsetGranularity = DateTimeGranularity.create("MILLIS");
        BeforeRelation relation = new BeforeRelation(main, other, true, DateTimeDefaults.get(), offsetGranularity, 0);

        FieldSpec actual = relation.createModifierFromOtherFieldSpec(fieldSpec);
        FieldSpec expected = fromMax(2020);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void before_range_returnsFromMin() {
        int maxYear = 2020;
        DateTimeGranularity offsetGranularity = DateTimeGranularity.create("MILLIS");
        FieldSpec fieldSpec = forYears(maxYear - 3, maxYear);
        BeforeRelation relation = new BeforeRelation(main, other, false, DateTimeDefaults.get(), offsetGranularity, 0);

        RestrictionsFieldSpec actualFieldSpec = (RestrictionsFieldSpec) relation.createModifierFromOtherFieldSpec(fieldSpec);
        LinearRestrictions actualRestrictions = (LinearRestrictions) actualFieldSpec.getRestrictions();
        OffsetDateTime actualMin = (OffsetDateTime) actualRestrictions.getMin();
        OffsetDateTime actualMax = (OffsetDateTime) actualRestrictions.getMax();

        OffsetDateTime expectedMin = DateTimeDefaults.get().min();
        OffsetDateTime expectedMax = OffsetDateTime.of(maxYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
            .minusNanos(1000000);  // Current smallest granularity is milliseconds.

        assertEquals(actualMin.compareTo(expectedMin), 0);
        assertEquals(actualMax.compareTo(expectedMax), 0);
    }

    @Test
    public void equalTo_forSet_returns() {
        DistributedList<Object> expectedValues = DistributedList.uniform(
            Stream.of("2", "3", "4")
                .map(BigDecimal::new)
                .collect(Collectors.toSet()));
        WhitelistFieldSpec expected = FieldSpecFactory.fromList(expectedValues);

        DistributedList<Object> secondValues = DistributedList.uniform(
            Stream.of("1", "2", "3")
                .map(BigDecimal::new)
                .collect(Collectors.toSet()));
        WhitelistFieldSpec second = FieldSpecFactory.fromList(secondValues);

        Field firstField = FieldBuilder.createField("first");
        Field secondField = FieldBuilder.createField("second");

        FieldSpecRelation equalTo = new EqualToOffsetRelation<>(firstField, secondField, NumericGranularity.INTEGER_DEFAULT, 1);

        FieldSpec result = equalTo.createModifierFromOtherFieldSpec(second);

        assertEquals(expected, result);
    }

    private FieldSpec fromMin(int year) {
        OffsetDateTime min = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        LinearRestrictions restrictions = new LinearRestrictions(min, ISO_MAX_DATE, new DateTimeGranularity(MILLIS));
        return FieldSpecFactory.fromRestriction(restrictions);
    }

    private FieldSpec fromMax(int year) {
        OffsetDateTime max = OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        LinearRestrictions restrictions = new LinearRestrictions(ISO_MIN_DATE, max, new DateTimeGranularity(MILLIS));
        return FieldSpecFactory.fromRestriction(restrictions);
    }

    private FieldSpec forYears(int minYear, int maxYear) {
        OffsetDateTime min = OffsetDateTime.of(minYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime max = OffsetDateTime.of(maxYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        LinearRestrictions<OffsetDateTime> restrictions = new LinearRestrictions(min, max, new DateTimeGranularity(YEARS));
        return FieldSpecFactory.fromRestriction(restrictions).withNotNull();
    }
}