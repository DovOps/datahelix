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


package com.scottlogic.datahelix.generator.output.writer.csv;

import com.scottlogic.datahelix.generator.common.output.GeneratedObject;
import com.scottlogic.datahelix.generator.common.profile.*;
import com.scottlogic.datahelix.generator.output.writer.DataSetWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class CsvDataSetWriterTest {
    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    private Field fieldOne = new Field("one", StandardSpecificFieldType.STRING.toSpecificFieldType(),false,null,false, false, null);
    private Field fieldTwo = new Field("two", StandardSpecificFieldType.STRING.toSpecificFieldType(),false,null,false, false, null);

    private Fields fields = new ProfileFields(new ArrayList<>(Arrays.asList(
        fieldOne, fieldTwo
    )));

    @Mock
    GeneratedObject row;

    @Test
    public void open_createsCSVWriterThatCorrectlyOutputsCommasAndQuotes() {
        DataSetWriter dataSetWriter;
        Mockito.when(row.getFormattedValue(fieldOne)).thenReturn(",,");
        Mockito.when(row.getFormattedValue(fieldTwo)).thenReturn(",\"");
        try {
            dataSetWriter = CsvDataSetWriter.open(outputStream, fields);
            dataSetWriter.writeRow(row);
            String output = outputStream.toString(StandardCharsets.UTF_8.toString());
            assertEquals(
                "one,two\r\n\",,\",\",\"\"\"\r\n",
                output,
                "If the actual and expected appear to be identical, check for null characters");
        } catch (IOException e) {
            fail(e.toString());
        }
    }
}