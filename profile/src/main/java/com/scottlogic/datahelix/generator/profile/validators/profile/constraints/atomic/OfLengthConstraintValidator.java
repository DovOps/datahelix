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

import com.scottlogic.datahelix.generator.common.profile.FieldType;
import com.scottlogic.datahelix.generator.common.util.Defaults;
import com.scottlogic.datahelix.generator.common.validators.ValidationResult;
import com.scottlogic.datahelix.generator.profile.dtos.FieldDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.atomic.integer.OfLengthConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.atomic.integer.StringLengthConstraintDTO;

import java.math.BigDecimal;
import java.util.List;

public class OfLengthConstraintValidator extends AtomicConstraintValidator<OfLengthConstraintDTO>
{
    private final FieldType expectedFieldType;

    public OfLengthConstraintValidator(List<FieldDTO> fields, FieldType expectedFieldType)
    {
        super(fields);
        this.expectedFieldType = expectedFieldType;
    }

    @Override
    public final ValidationResult validate(OfLengthConstraintDTO dto)
    {
        ValidationResult fieldMustBeValid = fieldMustBeValid(dto);
        if(!fieldMustBeValid.isSuccess) return fieldMustBeValid;

        ValidationResult stringLengthMustBeValid = stringLengthMustBeValid(dto);
        if(!stringLengthMustBeValid.isSuccess) return stringLengthMustBeValid;

        return fieldTypeMustMatchValueType(dto, expectedFieldType);
    }

    private ValidationResult stringLengthMustBeValid(StringLengthConstraintDTO dto)
    {
        BigDecimal integer = BigDecimal.valueOf(dto.stringLength());
        BigDecimal max = BigDecimal.valueOf(Defaults.MAX_STRING_LENGTH);
        BigDecimal min = BigDecimal.ZERO;
        if (integer.compareTo(min) < 0)
        {
            return ValidationResult.failure("String length must have a value >= %s, currently is %s".formatted(min.toPlainString(), integer.toPlainString()));
        }
        if (integer.compareTo(max) > 0)
        {
            return ValidationResult.failure("String length must have a value <= %s, currently is %s".formatted(max.toPlainString(), integer.toPlainString()));
        }
        return ValidationResult.success();
    }
}
