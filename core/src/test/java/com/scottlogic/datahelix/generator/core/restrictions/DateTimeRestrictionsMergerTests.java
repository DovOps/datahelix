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

package com.scottlogic.datahelix.generator.core.restrictions;
import com.scottlogic.datahelix.generator.common.profile.DateTimeGranularity;
import com.scottlogic.datahelix.generator.core.restrictions.linear.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.scottlogic.datahelix.generator.common.util.Defaults.ISO_MAX_DATE;
import static com.scottlogic.datahelix.generator.core.utils.GeneratorDefaults.*;
import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

class DateTimeRestrictionsMergerTests {
    private LinearRestrictionsMerger merger = new LinearRestrictionsMerger();

    private static final OffsetDateTime REFERENCE_TIME = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void merge_leftHasMinAndRightHasMax_returnsDateTimeRestrictionsWithMergedMinAndMax() {
        Limit<OffsetDateTime> minDateTimeLimit = new Limit<>(
            REFERENCE_TIME.minusDays(7), true
        );
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            REFERENCE_TIME, true
        );
        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getMin(), equalTo(REFERENCE_TIME.minusDays(7)));
        assertThat(result.get().getMax(), equalTo(REFERENCE_TIME));
    }

    @Test
    void merge_leftHasMaxAndRightHasMin_returnsDateTimeRestrictionsWithMergedMinAndMax() {
        Limit <OffsetDateTime>minDateTimeLimit = new Limit<>(
            REFERENCE_TIME.minusDays(7), true
        );
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            REFERENCE_TIME, true
        );
        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getMin(), equalTo(REFERENCE_TIME.minusDays(7)));
        assertThat(result.get().getMax(), equalTo(REFERENCE_TIME));
    }

    @Test
    void merge_leftHasMinGreaterThanRightMax_returnsNull() {
        Limit <OffsetDateTime>minDateTimeLimit = new Limit<>(
            OffsetDateTime.now(), false
        );
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            OffsetDateTime.now().minusDays(10), false
        );
        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(false));
    }

    @Test
    void merge_rightHasMinGreaterThanLeftMax_returnsNull() {
        Limit <OffsetDateTime>minDateTimeLimit = new Limit<>(
            OffsetDateTime.now(), false
        );
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            OffsetDateTime.now().minusDays(10), false
        );
        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(false));
    }

    @Test
    void merge_rightAndLeftHaveNoMax_shouldNotReturnNull() {
        Limit <OffsetDateTime>minDateTimeLimit = new Limit<>(
            REFERENCE_TIME, true
        );

        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getMin(), equalTo(REFERENCE_TIME));
        assertThat(result.get().getMax(), equalTo(ISO_MAX_DATE));
    }

    @Test
    void merge_rightAndLeftHaveNoMin_shouldNotReturnNull() {
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            REFERENCE_TIME, true
        );

        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        Assertions.assertNotEquals(result, nullValue());

        LinearRestrictions<OffsetDateTime> restrictions = result.get();
        Assertions.assertNotEquals(restrictions, nullValue());


        Assertions.assertNotEquals(restrictions.getMin(), nullValue());
        Assertions.assertEquals(restrictions.getMax(), REFERENCE_TIME);

    }

    @Test
    void merge_minExclusiveSameAsMaxInclusive_shouldReturnAsUnsuccessful() {
        OffsetDateTime referenceTime = OffsetDateTime.now();

        Limit <OffsetDateTime>minDateTimeLimit = new Limit<>(
            referenceTime, false
        );
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            referenceTime, true
        );

        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(false));
    }

    @Test
    void merge_minExclusiveSameAsMaxExclusive_shouldReturnAsUnsuccessful() {
        OffsetDateTime referenceTime = OffsetDateTime.now();

        Limit <OffsetDateTime>minDateTimeLimit = new Limit<>(
            referenceTime, false
        );
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            referenceTime, false
        );

        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);


        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(false));
    }

    @Test
    void merge_minInclusiveSameAsMaxExclusive_shouldReturnAsUnsuccessful() {
        OffsetDateTime referenceTime = OffsetDateTime.now();

        Limit <OffsetDateTime>minDateTimeLimit = new Limit<>(
            referenceTime, true
        );
        Limit <OffsetDateTime>maxDateTimeLimit = new Limit<>(
            referenceTime, false
        );
        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(DATETIME_MIN_LIMIT, maxDateTimeLimit);
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(minDateTimeLimit, DATETIME_MAX_LIMIT);

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);

        assertThat(result, not(nullValue()));
        assertThat(result.isPresent(), is(false));
    }

    @Test
    void merge_minOfDifferentGranularity_shouldReturnMostCoarse() {
        OffsetDateTime laterTime = REFERENCE_TIME.plusSeconds(1);

        Limit <OffsetDateTime>lowerDateTimeLimit = new Limit<>(
            REFERENCE_TIME, true
        );
        Limit <OffsetDateTime>upperDateTimeLimit = new Limit<>(
            laterTime, false
        );
        
        LinearRestrictions<OffsetDateTime> left = LinearRestrictionsFactory.createDateTimeRestrictions(lowerDateTimeLimit, DATETIME_MAX_LIMIT, new DateTimeGranularity(HOURS));
        LinearRestrictions<OffsetDateTime> right = LinearRestrictionsFactory.createDateTimeRestrictions(upperDateTimeLimit, DATETIME_MAX_LIMIT, new DateTimeGranularity(MILLIS));

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(left, right, false);
        LinearRestrictions<OffsetDateTime> restrictions = result.get();

        assertThat(result, not(nullValue()));
        Assertions.assertEquals(true, result.isPresent());

        assertThat(restrictions, not(nullValue()));

        Assertions.assertEquals(restrictions.getMin(), REFERENCE_TIME.plusHours(1));
    }


    @Test
    void merge_minOfDifferentGranularity_shouldReturnMostCoarseAndBeInclusiveWhenWeWouldOtherwiseLoseAValidResult() {
        // edge case example
        // constraint later than 00:00:00 (exclusive / inclusive = false, granularity = HOURS)
        // constraint later than 00:00:01 (exclusive / inclusive = false, granularity =  SECONDS)
        //
        //Should Return: equal to or later than 01:00:00 (inclusive, granularity = HOURS / inclusive true)
        // if inclusive were false, we would exclude 01:00:00. as 01:00:00 meets both original constraints, it should be included

        OffsetDateTime earlyTime = REFERENCE_TIME;
        OffsetDateTime laterTime = REFERENCE_TIME.plusSeconds(1);

        Limit <OffsetDateTime>lowerDateTimeLimit = new Limit<>(earlyTime, false);
        Limit <OffsetDateTime>upperDateTimeLimit = new Limit<>(laterTime, false);

        LinearRestrictions<OffsetDateTime> early = LinearRestrictionsFactory.createDateTimeRestrictions(lowerDateTimeLimit, DATETIME_MAX_LIMIT, new DateTimeGranularity(HOURS));
        LinearRestrictions<OffsetDateTime> later = LinearRestrictionsFactory.createDateTimeRestrictions(upperDateTimeLimit, DATETIME_MAX_LIMIT, new DateTimeGranularity(SECONDS));

        Optional<LinearRestrictions<OffsetDateTime>> result = merger.merge(early, later, false);
        LinearRestrictions<OffsetDateTime> restrictions = result.get();

        // assert that we get the correct level of granularity
        Assertions.assertNotNull(restrictions);

        // assert that we return an inclusive restriction for this edge case.
        Assertions.assertEquals(restrictions.getMin(), REFERENCE_TIME.plusHours(1));
    }
}
