package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.generation.field_value_sources.FieldValueSource;
import com.scottlogic.deg.generator.utils.IRandomNumberGenerator;
import com.scottlogic.deg.generator.utils.UpCastingIterator;

public interface IStringGenerator {
    IStringGenerator intersect(IStringGenerator stringGenerator);
    IStringGenerator complement();

    boolean isFinite();
    long getValueCount();
    boolean match(String subject);

    Iterable<String> generateInterestingValues();

    Iterable<String> generateAllValues();

    Iterable<String> generateRandomValues(IRandomNumberGenerator randomNumberGenerator);

    default FieldValueSource asFieldValueSource() {
        return new StringGeneratorAsFieldValueSource(this);
    }

    // Adapter
    class StringGeneratorAsFieldValueSource implements FieldValueSource {
        private final IStringGenerator underlyingGenerator;

        StringGeneratorAsFieldValueSource(IStringGenerator underlyingGenerator) {
            this.underlyingGenerator = underlyingGenerator;
        }

        @Override
        public boolean isFinite() {
            return underlyingGenerator.isFinite();
        }

        @Override
        public long getValueCount() {
            return underlyingGenerator.getValueCount();
        }

        @Override
        public Iterable<Object> generateInterestingValues() {
            return () -> new UpCastingIterator<>(
                underlyingGenerator.generateInterestingValues().iterator());
        }

        @Override
        public Iterable<Object> generateAllValues() {
            return () -> new UpCastingIterator<>(
                underlyingGenerator.generateAllValues().iterator());
        }

        @Override
        public Iterable<Object> generateRandomValues(IRandomNumberGenerator randomNumberGenerator) {
            return () -> new UpCastingIterator<>(
                underlyingGenerator.generateRandomValues(randomNumberGenerator).iterator());
        }
    }
}
