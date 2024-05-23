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

package com.scottlogic.datahelix.generator.profile.serialisation;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.ConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.InvalidConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.atomic.EqualToConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.atomic.IsNullConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.atomic.numeric.GreaterThanConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.atomic.numeric.LessThanConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.grammatical.AllOfConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.grammatical.AnyOfConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.grammatical.ConditionalConstraintDTO;
import com.scottlogic.datahelix.generator.profile.dtos.constraints.grammatical.NotConstraintDTO;
import com.scottlogic.datahelix.generator.profile.reader.CsvInputStreamReaderFactory;
import com.scottlogic.datahelix.generator.profile.reader.FileReader;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

public class GrammaticalConstraintDeserialiserTests {
    @Test
    public void shouldDeserialiseAnyOfWithoutException() throws IOException {
        // Arrange
        final String json =
            """
            { "anyOf": [\
                { "field": "foo", "equalTo": "0" },\
                { "field": "foo", "isNull": "true" }\
              ]\
            }\
            """;

        // Act
        ConstraintDTO actual = deserialiseJsonString(json);

        // Assert
        EqualToConstraintDTO expectedEqualsTo = new EqualToConstraintDTO();
        expectedEqualsTo.field = "foo";
        expectedEqualsTo.value = "0";
        IsNullConstraintDTO expectedNull = new IsNullConstraintDTO();
        expectedNull.field = "foo";
        expectedNull.isNull = true;

        AnyOfConstraintDTO expected = new AnyOfConstraintDTO();
        expected.constraints = Arrays.asList(expectedEqualsTo, expectedNull);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void shouldDeserialiseAnyOfAndThrowInvalidFieldException() {
        // Arrange
        final String json =
            """
            { "anyOf": [\
                { "fild": "foo", "equalTo": "0" },\
                { "field": "foo", "isNull": "true" }\
              ]\
            }\
            """;

        // Assert
        Assertions.assertThrows(UnrecognizedPropertyException.class, () -> deserialiseJsonString(json));
    }

    @Test
    public void shouldDeserialiseAnyOfAndReturnInvalidConstraint() throws IOException {
        // Arrange
        final String json =
            """
            { "ayOf": [\
                { "field": "foo", "equalTo": "0" },\
                { "field": "foo", "isNull": "true" }\
              ]\
            }\
            """;

        ConstraintDTO constraintDTO = deserialiseJsonString(json);
        Assert.assertTrue(constraintDTO instanceof InvalidConstraintDTO);
    }

    @Test
    public void shouldDeserialiseAllOfWithoutException() throws IOException {
        // Arrange
        final String json =
            """
            { "allOf": [\
                { "field": "foo", "greaterThan": "0" },\
                { "field": "foo", "lessThan": "100" }\
              ]\
            }\
            """;

        // Act
        ConstraintDTO actual = deserialiseJsonString(json);

        // Assert
        GreaterThanConstraintDTO expectedGreaterThan = new GreaterThanConstraintDTO();
        expectedGreaterThan.field = "foo";
        expectedGreaterThan.value = 0;
        LessThanConstraintDTO expectedLessThan = new LessThanConstraintDTO();
        expectedLessThan.field = "foo";
        expectedLessThan.value = 100;

        AllOfConstraintDTO expected = new AllOfConstraintDTO();
        expected.constraints = Arrays.asList(expectedGreaterThan, expectedLessThan);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void shouldDeserialiseNotWithoutException() throws IOException {
        // Arrange
        final String json = "{ \"not\": { \"field\": \"foo\", \"isNull\": \"true\" } }";

        // Act
       ConstraintDTO actual = deserialiseJsonString(json);

        // Assert
        IsNullConstraintDTO expectedNull = new IsNullConstraintDTO();
        expectedNull.field = "foo";
        expectedNull.isNull = true;

        NotConstraintDTO expected = new NotConstraintDTO();
        expected.constraint = expectedNull;

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void shouldDeserialiseIfWithoutException() throws IOException {
        // Arrange
        final String json =
            """
            { "if":{ "field": "foo", "lessThan": "100" },\
             "then":{ "field": "bar", "greaterThan": "0" },\
             "else":{ "field": "bar", "equalTo": "500" }}\
            """;

        // Act
        ConstraintDTO actual = deserialiseJsonString(json);

        // Assert
        LessThanConstraintDTO expectedLessThan = new LessThanConstraintDTO();
        expectedLessThan.field = "foo";
        expectedLessThan.value = 100;
        GreaterThanConstraintDTO expectedGreaterThan = new GreaterThanConstraintDTO();
        expectedGreaterThan.field = "bar";
        expectedGreaterThan.value = 0;
        EqualToConstraintDTO expectedEqualTo = new EqualToConstraintDTO();
        expectedEqualTo.field = "bar";
        expectedEqualTo.value = "500";

        ConditionalConstraintDTO expected= new ConditionalConstraintDTO();

        expected.ifConstraint = expectedLessThan;
        expected.thenConstraint = expectedGreaterThan;
        expected.elseConstraint = expectedEqualTo;

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void shouldDeserialiseIfAndThrowMissingColonException() throws IOException {
        // Arrange
        final String json =
            """
            { "if"{ "field": "foo", "lessThan": "100" },\
             "then"{ "field": "bar", "greaterThan": "0" },\
             "else"{ "field": "bar", "equalTo": "500" }}\
            """;

        try {
            deserialiseJsonString(json);
            Assert.fail("should have thrown an exception");
        } catch (JsonParseException e) {
            String expectedMessage = "Unexpected character ('{' (code 123)): was expecting a colon to separate field name and value\n at [Source: (String)\"{ \"if\"{ \"field\": \"foo\", \"lessThan\": \"100\" }, \"then\"{ \"field\": \"bar\", \"greaterThan\": \"0\" }, \"else\"{ \"field\": \"bar\", \"equalTo\": \"500\" }}\"; line: 1, column: 8]";
            assertThat(e.getMessage(), sameBeanAs(expectedMessage));
        }
    }

    private ConstraintDTO deserialiseJsonString(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(
            ConstraintDTO.class,
            new ConstraintDeserializer(
                new FileReader(new CsvInputStreamReaderFactory()),
                Paths.get("test")));
        mapper.registerModule(module);

        return mapper
            .readerFor(ConstraintDTO.class)
            .readValue(json);
    }
}
