package com.scottlogic.deg.generator.constraints;

import com.scottlogic.deg.generator.Field;

public class IsStringShorterThanConstraint implements IConstraint {
    public final Field field;
    public final int referenceValue;

    public IsStringShorterThanConstraint(Field field, int referenceValue) {

        this.referenceValue = referenceValue;
        this.field = field;
    }

    @Override
    public String toString(){
        return String.format("%s is shorter than '%s'", field.name, referenceValue);
    }
}
