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

package com.scottlogic.datahelix.generator.profile.dtos;

import com.scottlogic.datahelix.generator.profile.dtos.constraints.ConstraintType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ConstraintTypeTest{
    @Test
    void fromText() {
        String greaterThanString = ConstraintType.GREATER_THAN_OR_EQUAL_TO.propertyName;
        ConstraintType greaterThanOrEqualTo = ConstraintType.fromName(greaterThanString);

        assertThat(greaterThanOrEqualTo, is(ConstraintType.GREATER_THAN_OR_EQUAL_TO));
    }

    @Test
    void fromTextLowerCase() {
        ConstraintType greaterThanOrEqualTo = ConstraintType.fromName("shorterthan");

        assertThat(greaterThanOrEqualTo, is(ConstraintType.SHORTER_THAN));
    }
}