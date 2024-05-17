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

package com.scottlogic.datahelix.generator.core.generation.fieldvaluesources;

import com.scottlogic.datahelix.generator.common.profile.NumericGranularity;
import com.scottlogic.datahelix.generator.common.util.NumberUtils;
import com.scottlogic.datahelix.generator.core.restrictions.linear.Limit;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictions;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictionsMerger;
import com.scottlogic.datahelix.generator.core.utils.JavaUtilRandomNumberGenerator;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictionsFactory.createNumericRestrictions;
import static com.scottlogic.datahelix.generator.core.utils.GeneratorDefaults.NUMERIC_MAX_LIMIT;
import static com.scottlogic.datahelix.generator.core.utils.GeneratorDefaults.NUMERIC_MIN_LIMIT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class RealNumberFieldValueSourceTests {
    @ParameterizedTest
    @CsvSource({
        "-1,  1,    1, -1;-0.9;-0.8;-0.7;-0.6;-0.5;-0.4;-0.3;-0.2;-0.1;0.0;0.1;0.2;0.3;0.4;0.5;0.6;0.7;0.8;0.9;1",
        "0.65,1.1,  1, 0.7;0.8;0.9;1;1.1",
        "5,   35,  -1, 10;20;30",
        "10,  35,  -1, 10;20;30",
        "10,  40,  -1, 10;20;30;40",
        "5,   40,  -1, 10;20;30;40",
        "11,  40,  -1, 20;30;40",
        "10,  41,  -1, 10;20;30;40",
        "9,   41,  -1, 10;20;30;40",
        "11,  41,  -1, 20;30;40",
        "11,  49,  -1, 20;30;40",
        "-4,  5,    0, -4;-3;-2;-1;0;1;2;3;4;5",
        "0.9, 2.1,  0, 1;2",
        "0.1, 5.9,  0, 1;2;3;4;5",
        "1,   3,    0, 1;2;3"
    })
    void expectValuesInclusiveOfBounds(BigDecimal lowerBound, BigDecimal upperBound, int scale, String expectedResults) {
        givenLowerBound(lowerBound, true);
        givenUpperBound(upperBound, true);
        givenScale(scale);

        expectAllValues((Object[])expectedResults.split(";"));
    }

    @ParameterizedTest
    @CsvSource({
        "-1,   1,    1, -0.9;-0.8;-0.7;-0.6;-0.5;-0.4;-0.3;-0.2;-0.1;0.0;0.1;0.2;0.3;0.4;0.5;0.6;0.7;0.8;0.9",
        "0.65, 1.1,  1, 0.7;0.8;0.9;1",
        "5,    35,  -1, 10;20;30",
        "10,   35,  -1, 20;30",
        "10,   40,  -1, 20;30",
        "5,    40,  -1, 10;20;30",
        "11,   40,  -1, 20;30",
        "10,   41,  -1, 20;30;40",
        "9,    41,  -1, 10;20;30;40",
        "11,   41,  -1, 20;30;40",
        "11,   49,  -1, 20;30;40",
        "-4,   5,    0, -3;-2;-1;0;1;2;3;4",
        "0.9,  2.1,  0, 1;2",
        "0.1,  5.9,  0, 1;2;3;4;5",
        "1,    3,    0, 2"
    })
    void expectValuesExclusiveOfBounds(BigDecimal lowerBound, BigDecimal upperBound, int scale, String expectedResults) {
        givenLowerBound(lowerBound, false);
        givenUpperBound(upperBound, false);
        givenScale(scale);

        expectAllValues((Object[])expectedResults.split(";"));
    }

    @Test
    void whenBlacklistHasNoValuesInRange() {
        givenLowerBound(3, true);
        givenUpperBound(5, true);

        givenBlacklist(1);

        expectAllValues(3, 4, 5);
    }

    @Test
    void whenBlacklistContainsAllValuesInRange() {
        givenLowerBound(3, true);
        givenUpperBound(5, true);

        givenBlacklist(3, 4, 5);

        expectNoValues();
    }

    @Test
    void whenBlacklistContainsAllValuesInExclusiveRange() {
        givenLowerBound("0.9", false);
        givenUpperBound("2.1", false);
        givenScale(0);

        givenBlacklist(1, 2);

        expectNoValues();
    }

    @Test
    void whenSmallWithBlacklist() {
        givenLowerBound("-0.05", false);
        givenUpperBound("0.05", false);
        givenScale(2);

        givenBlacklist("-0.03", "-0", "0.021", 4);

        expectAllValues("-0.04", "-0.02", "-0.01", "0.01", "0.03", "0.04");
    }

    @Test
    void shouldGenerateRandomValues() {
        givenLowerBound(-10, true);
        givenUpperBound(10, true);
        givenScale(5);

        expectCorrectRandomValues();
    }

    @Test
    void shouldGenerateLargeInclusiveRandomValues() {
        givenLowerBound(-100, true);
        givenUpperBound(100, true);
        givenScale(-1);

        expectCorrectRandomValues();
    }

    @Test
    void shouldGenerateLargeExclusiveRandomValues() {
        givenLowerBound(-100, false);
        givenUpperBound(100, false);
        givenScale(-1);

        expectCorrectRandomValues();
    }

    @Test
    void shouldGenerateNonBlacklistedValues() {
        givenLowerBound(5, true);
        givenUpperBound(10, false);
        givenScale(0);

        givenBlacklist(6, 8);

        expectCorrectRandomValues();
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesMatch(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));

        assertThat(a, equalTo(b));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesMatchBlacklistInDifferentOrder(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(2, 1)));

        assertThat(a, equalTo(b));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesMatchBlacklistEmpty(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            Collections.emptySet());
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            Collections.emptySet());

        assertThat(a, equalTo(b));
        assertThat(a.hashCode(), equalTo(b.hashCode()));
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesExceptMinMatch(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(5, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));

        assertThat(a, not(equalTo(b)));
    }

    private Set<BigDecimal> toBlacklist(Collection<Object> asList) {
        return asList.stream().map(NumberUtils::coerceToBigDecimal).collect(Collectors.toSet());
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesExceptMaxMatch(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(1, 20, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));

        assertThat(a, not(equalTo(b)));
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesExceptBlacklistMatch(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(3, 4)));

        assertThat(a, not(equalTo(b)));
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesExceptScaleMatch(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 2),
            toBlacklist(Arrays.asList(1, 2)));

        assertThat(a, not(equalTo(b)));
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesExceptMinAndMaxMatch(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(5, 20, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 1),
            toBlacklist(Arrays.asList(1, 2)));

        assertThat(a, not(equalTo(b)));
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesDontMatch(){
        LinearFieldValueSource<BigDecimal> a = new LinearFieldValueSource<>(
            numericRestrictions(5, 20, 1),
            toBlacklist(Arrays.asList(1, 2)));
        LinearFieldValueSource<BigDecimal> b = new LinearFieldValueSource<>(
            numericRestrictions(1, 10, 2),
            toBlacklist(Arrays.asList(3, 4)));

        assertThat(a, not(equalTo(b)));
    }

    @Test
    public void exhaustiveValuesInclusively_UpperLimitLargerThanConfig_IncludesConfigMax() {
        givenLowerBound(new BigDecimal("99999999999999999995"), true);
        givenUpperBound(1e30, true);
        givenScale(0);

        expectAllValues( "100000000000000000000", "99999999999999999999",
            "99999999999999999998", "99999999999999999997", "99999999999999999996", "99999999999999999995");
    }

    @Test
    public void exhaustiveValuesExclusively_UpperLimitLargerThanConfig_IncludesConfigMaxMinusOne() {
        givenLowerBound(new BigDecimal("99999999999999999995"), false);
        givenUpperBound(1e30, false);
        givenScale(0);

        expectAllValues("100000000000000000000", "99999999999999999999", "99999999999999999998", "99999999999999999997",
            "99999999999999999996");
    }

    @Test
    public void exhaustiveValuesInclusively_LowerLimitSmallerThanConfig_IncludesConfigMin() {
        givenLowerBound(-1e30, true);
        givenUpperBound(new BigDecimal("-99999999999999999995"), true);
        givenScale(0);

        expectAllValues(new BigDecimal("-1e20"), "-99999999999999999999",
            "-99999999999999999998", "-99999999999999999997", "-99999999999999999996", "-99999999999999999995");
    }

    @Test
    public void exhaustiveValuesExclusively_LowerLimitSmallerThanConfig_IncludesConfigMinPlusOne() {
        givenLowerBound(-1e30, false);
        givenUpperBound(new BigDecimal("-99999999999999999995"), false);
        givenScale(0);

        expectAllValues( "-100000000000000000000", "-99999999999999999999", "-99999999999999999998", "-99999999999999999997",
            "-99999999999999999996");
    }

    private LinearRestrictions<BigDecimal> numericRestrictions(Integer min, Integer max, int scale){
        return createNumericRestrictions(
            min == null ? null : new Limit<>(BigDecimal.valueOf(min), true),
            max == null ? null : new Limit<>(BigDecimal.valueOf(max), true),
            new NumericGranularity(scale));
    }

    private Limit<BigDecimal> upperLimit = NUMERIC_MAX_LIMIT;
    private Limit<BigDecimal> lowerLimit = NUMERIC_MIN_LIMIT;
    private int scale;
    private Set<Object> blacklist = new HashSet<>();
    private LinearFieldValueSource<BigDecimal> objectUnderTest;

    private void givenLowerBound(Object limit, boolean isInclusive) {
        givenLowerBound(NumberUtils.coerceToBigDecimal(limit), isInclusive);
    }
    private void givenLowerBound(BigDecimal limit, boolean isInclusive) {
        this.lowerLimit = new Limit<>(limit, isInclusive);
    }

    private void givenUpperBound(Object limit, boolean isInclusive) {
        givenUpperBound(NumberUtils.coerceToBigDecimal(limit), isInclusive);
    }
    private void givenUpperBound(BigDecimal limit, boolean isInclusive) {
        this.upperLimit = new Limit<>(limit, isInclusive);
    }

    private void givenScale(int scale) {
        this.scale = scale;
    }

    private void givenBlacklist(Object... values) {
        blacklist = new HashSet<>(Arrays.asList(values));
    }
    private void expectAllValues(Object... expectedValuesArray) {
        expectValues(getObjectUnderTest().generateAllValues(), expectedValuesArray);
    }

    private void expectValues(Stream<Object> values, Object... expectedValuesArray) {
        Collection<Matcher<? super BigDecimal>> expectedValuesMatchers = Stream.of(expectedValuesArray)
            .map(NumberUtils::coerceToBigDecimal)
            .map(Matchers::comparesEqualTo) // we have to use compare otherwise it fails if the scale is different
            .collect(Collectors.toList());

        BigDecimal[] actualValues = values.toArray(BigDecimal[]::new);

        // ASSERT
        assertThat(actualValues, arrayContainingInAnyOrder(expectedValuesMatchers));
    }

    private void expectNoValues() {
        expectAllValues();
    }

    private FieldValueSource getObjectUnderTest() {
        if (objectUnderTest == null) {
            LinearRestrictions<BigDecimal> restrictions = createNumericRestrictions(lowerLimit, upperLimit, new NumericGranularity(scale));
            restrictions = (LinearRestrictions<BigDecimal>) new LinearRestrictionsMerger().merge(restrictions, createNumericRestrictions(NUMERIC_MIN_LIMIT, NUMERIC_MAX_LIMIT), false).get();
            objectUnderTest = new LinearFieldValueSource<>(restrictions, toBlacklist(blacklist));
        }

        return objectUnderTest;
    }

    private void expectCorrectRandomValues() {
        Stream<Object> resultsIterable = getObjectUnderTest().generateRandomValues(new JavaUtilRandomNumberGenerator(0));

        Set<BigDecimal> decimalBlacklist = blacklist
            .stream()
            .map(NumberUtils::coerceToBigDecimal)
            .map(value -> value.setScale(scale, RoundingMode.HALF_UP))
            .collect(Collectors.toCollection(TreeSet::new));

        resultsIterable.limit(1000)
            .map(value -> (BigDecimal)value)
            .forEach(value ->
            {
                // Not sure if this is the most efficient way to test all these values,
                // I think it'll do for now though.
                assertThat(
                    lowerLimit.getValue(),
                    lowerLimit.isInclusive()
                        ? lessThanOrEqualTo(value)
                        : lessThan(value));

                assertThat(
                    value,
                    upperLimit.isInclusive()
                        ? lessThanOrEqualTo(upperLimit.getValue())
                        : lessThan(upperLimit.getValue()));

                if (decimalBlacklist.size() != 0) {
                    Assertions.assertFalse(decimalBlacklist.contains(value));
                }

                assertThat(value.scale(), equalTo(scale));
            });
    }
}

