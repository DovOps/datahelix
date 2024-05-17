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

import com.scottlogic.datahelix.generator.core.restrictions.linear.Limit;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictions;
import com.scottlogic.datahelix.generator.core.restrictions.linear.LinearRestrictionsFactory;
import com.scottlogic.datahelix.generator.core.restrictions.string.StringRestrictions;
import com.scottlogic.datahelix.generator.core.restrictions.string.StringRestrictionsFactory;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;

public class RestrictionTest {
    @Test
    public void shouldFilterNumeric() {
        LinearRestrictions<BigDecimal> restriction = LinearRestrictionsFactory.createNumericRestrictions(
            new Limit<>(new BigDecimal("5"), true),
            new Limit<>(new BigDecimal("10"), false));

        assertThat(restriction.match(BigDecimal.valueOf(4)), Is.is(false));
        assertThat(restriction.match(BigDecimal.valueOf(5)), Is.is(true));
        assertThat(restriction.match(BigDecimal.valueOf(9)), Is.is(true));
        assertThat(restriction.match(BigDecimal.valueOf(10)), Is.is(false));
    }

    @Test
    public void shouldFilterString() {
        StringRestrictions restriction = new StringRestrictionsFactory().forStringMatching(Pattern.compile("H(i|ello) World"), false);

        assertThat(restriction.match("Hello World"), Is.is(true));
        assertThat(restriction.match("Hi World"), Is.is(true));
        assertThat(restriction.match("Goodbye"), Is.is(false));

        assertThat(restriction.match("5"), Is.is(false));
    }

}
