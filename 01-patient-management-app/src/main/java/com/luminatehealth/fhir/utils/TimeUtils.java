package com.luminatehealth.fhir.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtils {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String toString(Date date) {
        if (date == null) return null;
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.format(formatter);
    }

    public static String toString(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.format(formatter);
    }
}
