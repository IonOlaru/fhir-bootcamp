package com.luminatehealth.fhir.convertors;

public interface EntityConverter<IN, OUT> {
    OUT convert(final IN in);
    IN revert(final OUT out);
}
