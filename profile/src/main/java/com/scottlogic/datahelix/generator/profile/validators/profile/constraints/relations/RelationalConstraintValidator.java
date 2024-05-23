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

package com.scottlogic.datahelix.generator.profile.validators.profile.constraints.relations;

import com.scottlogic.datahelix.generator.common.profile.FieldType;
import com.scottlogic.datahelix.generator.common.validators.ValidationResult;
import com.scottlogic.datahelix.generator.profile.dtos.FieldDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.relations.RelationalConstraintDTO;
import com.scottlogic.datahelix.generator.profile.validators.profile.ConstraintValidator;
import com.scottlogic.datahelix.generator.profile.validators.profile.FieldValidator;
import com.scottlogic.datahelix.generator.profile.validators.profile.constraints.capabilities.DateTimeGranularityValidator;
import com.scottlogic.datahelix.generator.profile.validators.profile.constraints.capabilities.NumericGranularityValidator;

import java.util.List;
import java.util.Optional;

import static com.scottlogic.datahelix.generator.common.validators.ValidationResult.quote;

abstract public class RelationalConstraintValidator<T extends RelationalConstraintDTO> extends ConstraintValidator<T>
{
    protected static final String FIELD_DESCRIPTION = "Field";
    protected static final String RELATED_FIELD_DESCRIPTION = "Related field";

    public RelationalConstraintValidator(List<FieldDTO> fields)
    {
        super(fields);
    }

    protected ValidationResult offsetMustBeValid(T dto)
    {
        boolean withoutOffsetUnit = dto.offsetUnit == null || dto.offsetUnit.isEmpty();
        Optional<FieldType> fieldType = findField(dto.field)
            .map(f -> FieldValidator.getSpecificFieldType(f).getFieldType());

        if (fieldType.isEmpty() || withoutOffsetUnit) return ValidationResult.success();

        switch (fieldType.get()) {
            case BOOLEAN:
                return ValidationResult.failure("Offset is not supported for boolean fields%s".formatted(getErrorInfo(dto)));
            case STRING:
                return ValidationResult.failure("Offset is not supported for string fields%s".formatted(getErrorInfo(dto)));
            case DATETIME:
                return new DateTimeGranularityValidator(getErrorInfo(dto)).validate(dto.offsetUnit);
            case NUMERIC:
                return new NumericGranularityValidator(getErrorInfo(dto)).validate(dto.offsetUnit);
        }
        return ValidationResult.success();
    }

    protected ValidationResult fieldMustBeValid(T dto, String fieldName, String fieldDescription)
    {
        if (fieldName == null || fieldName.isEmpty()) {
            return ValidationResult.failure("%s must be specified%s".formatted(fieldDescription, getErrorInfo(dto)));
        }

        return findField(fieldName).isPresent()
            ? ValidationResult.success()
            : ValidationResult.failure("%s must be defined in fields%s".formatted(quote(fieldName), getErrorInfo(dto)));
    }
}
