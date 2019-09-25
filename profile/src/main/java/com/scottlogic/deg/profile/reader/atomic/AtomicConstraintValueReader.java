package com.scottlogic.deg.profile.reader.atomic;

import com.google.inject.Inject;
import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.common.profile.Types;
import com.scottlogic.deg.common.util.NumberUtils;
import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedSet;
import com.scottlogic.deg.profile.dto.ConstraintDTO;
import com.scottlogic.deg.profile.reader.InvalidProfileException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AtomicConstraintValueReader {

    private final FromFileReader fromFileReader;

    @Inject
    public AtomicConstraintValueReader(FromFileReader fromFileReader) {
        this.fromFileReader = fromFileReader;
    }


    public Object getValue(ConstraintDTO dto, Types type){
        try {
            return tryGetValue(dto, type);
        } catch (IllegalArgumentException | ValidationException e){
            throw new InvalidProfileException(String.format("Field [%s]: %s", dto.field, e.getMessage()));
        }
    }

    public Object tryGetValue(ConstraintDTO dto, Types type){
        if (dto.values != null){
            return getSet(dto.values, type);
        }

        if (dto.file != null){
            return fromFileReader.setFromFile(dto.file);
        }

        return getValue(dto.value, type);
    }

    private DistributedSet getSet(Collection<Object> values, Types type) {
        List collect = values.stream()
            .map(val -> getValue(val, type))
            .collect(Collectors.toList());
        return DistributedSet.uniform(collect);
    }

    private Object getValue(Object value, Types type) {
        if (value instanceof Map){
            return getDate((Map) value);
        }
        if (type == Types.NUMERIC){
            return getBigDecimal(value);
        }

        return value;
    }

    private Object getBigDecimal(Object value) {
        BigDecimal bigDecimal = NumberUtils.coerceToBigDecimal(value);

        if (bigDecimal == null){
            return value;
        }

        return bigDecimal;
    }


    private OffsetDateTime getDate(Map value) {
        return ConstraintReaderHelpers.parseDate((String) (value).get("date"));
    }
}