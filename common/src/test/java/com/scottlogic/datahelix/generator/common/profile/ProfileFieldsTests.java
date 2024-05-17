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

package com.scottlogic.datahelix.generator.common.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import static com.scottlogic.datahelix.generator.common.profile.FieldBuilder.createField;

class ProfileFieldsTests
{
    @Test
    void equals_objIsNull_returnsFalse() {
        Fields fields = new ProfileFields(
            Arrays.asList(
                createField("Test")
            )
        );

        boolean result = fields.equals(null);

        assertFalse(
            result,
            "Expected when other object is null a false value is returned but was true"
        );
    }

    @Test
    void equals_objTypeIsNotProfileFields_returnsFalse() {
        Fields fields = new ProfileFields(
            Arrays.asList(
                createField("Test")
            )
        );

        boolean result = fields.equals("Test");

        assertFalse(
            result,
            "Expected when the other object is a different type a false value is returned but was true"
        );
    }

    @Test
    void equals_rowSpecFieldsLengthNotEqualToOtherObjectFieldsLength_returnsFalse() {
        Fields fields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field")
            )
        );

        boolean result = fields.equals(
            new ProfileFields(
                Arrays.asList(
                    createField("First Field")
                )
            )
        );

        assertFalse(
            result,
            "Expected when the fields length do not match a false value is returned but was true"
        );
    }

    @Test
    void equals_rowSpecFieldsLengthEqualToOterObjectFieldsLengthButValuesDiffer_returnsFalse() {
        Fields fields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field")
            )
        );

        boolean result = fields.equals(
            new ProfileFields(
                Arrays.asList(
                    createField("First Field"),
                    createField("Third Field")
                )
            )
        );

        assertFalse(
            result,
            "Expected when the values of the fields property differs from the fields of the other object a false value is returned but was true"
        );
    }

    @Test
    void equals_rowSpecFieldsAreEqualToTheFieldsOfTheOtherObject_returnsTrue() {
        Fields fields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field")
            )
        );

        boolean result = fields.equals(
            new ProfileFields(
                Arrays.asList(
                    createField("First Field"),
                    createField("Second Field")
                )
            )
        );

        assertTrue(
            result,
            "Expected when the fields of both objects are equal a true value is returned but was false"
        );
    }

    @Test
    void hashCode_valuesinFieldsDifferInSize_returnsDifferentHashCodes() {
        Fields firstFields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field")
            )
        );
        Fields secondFields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field"),
                createField("Third Field")
            )
        );

        int firstHashCode = firstFields.hashCode();
        int secondHashCode = secondFields.hashCode();

        assertNotEquals(
            firstHashCode,
            secondHashCode,
            "Expected that when the profile fields length differ the hash codes should not be the same but were equal"
        );
    }

    @Test
    void hashCode_valuesInFieldsAreEqualSizeButValuesDiffer_returnsDifferentHashCodes() {
        Fields firstFields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field")
            )
        );
        Fields secondFields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Third Field")
            )
        );

        int firstHashCode = firstFields.hashCode();
        int secondHashCode = secondFields.hashCode();

        assertNotEquals(
            firstHashCode,
            secondHashCode,
            "Expected when the fields length are equal but their values differ unique hash codes are returned but were equal"
        );
    }

    @Test
    void hashCode_valuesInFieldsAreEqual_identicalHashCodesAreReturned() {
        Fields firstFields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field")
            )
        );
        Fields secondFields = new ProfileFields(
            Arrays.asList(
                createField("First Field"),
                createField("Second Field")
            )
        );

        int firstHashCode = firstFields.hashCode();
        int secondHashCode = secondFields.hashCode();

        assertEquals(
            firstHashCode,
            secondHashCode,
            "Expected that when the profile fields are equal an equivalent hash code should be returned for both but were different"
        );
    }
}
