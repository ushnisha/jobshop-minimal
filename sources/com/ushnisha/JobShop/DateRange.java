package com.ushnisha.JobShop;

import java.time.LocalDateTime;

/**
 *  A utility class that represents a start and end date - a Date Range
 */
public class DateRange {

    private LocalDateTime start;
    private LocalDateTime end;

    /**
     * Constructor for a DateRange object
     * @param st LocalDateTime value that represents the start of the date range
     * @param en LocalDateTime value that represnets the end of the date range
     */
    public DateRange(LocalDateTime st, LocalDateTime en) {
        this.start = st;
        this.end = en;
    }

    /**
     * Returns the start of the date range
     * @return LocalDateTime representing the start of the date range
     */
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Returns the end of the date range
     * @return LocalDateTime representing the end of the date range
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

}
