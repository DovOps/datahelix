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

package com.scottlogic.datahelix.generator.core.fieldspecs;

import com.scottlogic.datahelix.generator.common.profile.Field;
import com.scottlogic.datahelix.generator.common.profile.FieldBuilder;
import com.scottlogic.datahelix.generator.common.whitelist.DistributedList;
import com.scottlogic.datahelix.generator.common.whitelist.UniformList;
import com.scottlogic.datahelix.generator.core.generation.databags.DataBagValue;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class FieldSpecHelperTests {
    private FieldSpecHelper fieldSpecHelper = new FieldSpecHelper();
    private Field field = FieldBuilder.createField("test");

    @Test
    void getFieldSpecForValue() {
        DataBagValue input = new DataBagValue("value");

        FieldSpec actual = fieldSpecHelper.getFieldSpecForValue(input);

        FieldSpec expected = FieldSpecFactory.fromList(UniformList.singleton("value"))
            .withNotNull();

        assertEquals(actual, expected);
    }

    @Test
    void getFieldSpecForNullValue() {
        DataBagValue input = new DataBagValue(null);

        FieldSpec actual = fieldSpecHelper.getFieldSpecForValue(input);

        FieldSpec expected = FieldSpecFactory.nullOnly();

        assertEquals(actual, expected);
    }
}