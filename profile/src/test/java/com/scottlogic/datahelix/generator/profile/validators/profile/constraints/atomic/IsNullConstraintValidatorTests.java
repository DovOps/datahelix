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
package com.scottlogic.datahelix.generator.profile.validators.profile.constraints.atomic;

import com.scottlogic.datahelix.generator.common.profile.StandardSpecificFieldType;
import com.scottlogic.datahelix.generator.common.validators.ValidationResult;
import com.scottlogic.datahelix.generator.profile.creation.FieldDTOBuilder;
import com.scottlogic.datahelix.generator.profile.dtos.FieldDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.atomic.IsNullConstraintDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.scottlogic.datahelix.generator.profile.creation.AtomicConstraintDTOBuilder.atomicConstraintDTO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IsNullConstraintValidatorTests
{

    private final List<FieldDTO> fields = Arrays.asList
        (
            FieldDTOBuilder.fieldDTO("text", StandardSpecificFieldType.STRING).build()
        );

    @Test
    public void validateIsNullConstraint_withValidField_succeeds()
    {
        // Arrange
        IsNullConstraintDTO dto = atomicConstraintDTO("text").buildIsNull(false);

        // Act
        ValidationResult validationResult = new IsNullConstraintValidator(fields).validate(dto);

        // Assert
        assertTrue(validationResult.isSuccess);
    }

    @Test
    public void validateIsNullConstraint_withNullField_fails()
    {
        // Arrange
        IsNullConstraintDTO dto = atomicConstraintDTO(null).buildIsNull(false);

        // Act
        ValidationResult validationResult = new IsNullConstraintValidator(fields).validate(dto);

        // Assert
        assertFalse(validationResult.isSuccess);
        assertThat(validationResult.errors, iterableWithSize(1));
        assertThat(validationResult.errors, hasItem("Field must be specified | Field: NULL | Constraint: 'isNull'"));
    }

    @Test
    public void validateIsNullConstraint_withEmptyField_fails()
    {
        // Arrange
        IsNullConstraintDTO dto = atomicConstraintDTO("").buildIsNull(false);

        // Act
        ValidationResult validationResult = new IsNullConstraintValidator(fields).validate(dto);

        // Assert
        assertFalse(validationResult.isSuccess);
        assertThat(validationResult.errors, iterableWithSize(1));
        assertThat(validationResult.errors, hasItem("Field must be specified | Field: '' | Constraint: 'isNull'"));
    }

    @Test
    public void validateIsNullConstraint_withUndefinedField_fails()
    {
        // Arrange
        IsNullConstraintDTO dto = atomicConstraintDTO("unknown").buildIsNull(false);

        // Act
        ValidationResult validationResult = new IsNullConstraintValidator(fields).validate(dto);

        // Assert
        assertFalse(validationResult.isSuccess);
        assertThat(validationResult.errors, iterableWithSize(1));
        assertThat(validationResult.errors, hasItem("'unknown' must be defined in fields | Field: 'unknown' | Constraint: 'isNull'"));
    }
}
