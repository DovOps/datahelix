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

package com.scottlogic.datahelix.generator.orchestrator.validator;

import com.scottlogic.datahelix.generator.common.ValidationException;
import com.scottlogic.datahelix.generator.common.util.FileUtils;
import com.scottlogic.datahelix.generator.profile.validators.ConfigValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConfigValidatorTests {
    @Mock
    private File mockProfileFile = mock(File.class);

    @Test
    public void checkProfileInputFile_withValidFile_returnsNoErrorMessages() {
        ConfigValidator configValidator = new ConfigValidator(new FileUtils());

        when(mockProfileFile.getPath()).thenReturn("path");
        when(mockProfileFile.exists()).thenReturn(true);
        when(mockProfileFile.isDirectory()).thenReturn(false);
        when(mockProfileFile.length()).thenReturn(1L);

        assertDoesNotThrow(() -> configValidator.validate(mockProfileFile),"Expected no exception, but one was thrown.");
    }

    @Test
    public void checkProfileInputFile_profileFilePathContainsInvalidChars_throwsException() {
        ConfigValidator configValidator = new ConfigValidator(new FileUtils());

        when(mockProfileFile.getPath()).thenReturn("path?");

        assertThrows(ValidationException.class, () -> configValidator.validate(mockProfileFile),"Expected ValidationException to throw, but didn't");
    }

    @Test
    public void checkProfileInputFile_profileFileDoesNotExist_throwsException() {
        ConfigValidator configValidator = new ConfigValidator(new FileUtils());

        when(mockProfileFile.getPath()).thenReturn("path");
        when(mockProfileFile.exists()).thenReturn(false);

        assertThrows(ValidationException.class, () -> configValidator.validate(mockProfileFile),"Expected ValidationException to throw, but didn't");
    }

    @Test
    public void checkProfileInputFile_profileFileIsDir_throwsException() {
        ConfigValidator configValidator = new ConfigValidator(new FileUtils());

        when(mockProfileFile.getPath()).thenReturn("path");
        when(mockProfileFile.exists()).thenReturn(true);
        when(mockProfileFile.isDirectory()).thenReturn(true);

        assertThrows(ValidationException.class, () -> configValidator.validate(mockProfileFile),"Expected ValidationException to throw, but didn't");
    }

    @Test
    public void checkProfileInputFile_profileFileIsEmpty_throwsException() {
        ConfigValidator configValidator = new ConfigValidator(new FileUtils());

        when(mockProfileFile.getPath()).thenReturn("path");
        when(mockProfileFile.exists()).thenReturn(true);
        when(mockProfileFile.isDirectory()).thenReturn(false);
        when(mockProfileFile.length()).thenReturn(0L);

        assertThrows(ValidationException.class, () -> configValidator.validate(mockProfileFile),"Expected ValidationException to throw, but didn't");
    }
}