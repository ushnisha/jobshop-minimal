/**
 **********************************************************************
 * JobShop Minimal - A minimal JobShop Scheduler
 **********************************************************************
   Copyright (c) 2017-2018 Arun Kunchithapatham

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   Contributors:
   Arun Kunchithapatham - Initial Contribution
 ***********************************************************************
 *
 */

package com.ushnisha.JobShop;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
    
    /**
     * Returns the length of time in this DateRange in minutes
     * @return long value representing the length of time in this
     *         DateRange, measured in minutes
     */
    public long getLength() {
        return start.until(end, ChronoUnit.MINUTES);
    }

    /**
     * Utility function that returns true if a DateRange intersects
     * another provided DateRange
     * @param dr DateRange with which we check to see if this DateRange intersects
     * @return long value representing the number of minutes they intersect
     */
    public long intersectLength(DateRange dr) {

        // Intersection case 1 - dr within this date range
        if (this.contains(dr.getStart()) && this.contains(dr.getEnd())) {
            return dr.getLength();
        }

        // Intersection case 2 - dr starts within this date range and
        // ends outside
        if (this.contains(dr.getStart()) && !this.contains(dr.getEnd())) {
            DateRange overlap = new DateRange(dr.getStart(), this.getEnd());
            return overlap.getLength();
        }

        // Intersection case 3 - dr starts before this date range and
        // ends inside
        if (!this.contains(dr.getStart()) && this.contains(dr.getEnd())) {
            DateRange overlap = new DateRange(this.getStart(), dr.getEnd());
            return overlap.getLength();
        }

        // Intersection case 4 - dr contains this date range
        if (dr.contains(this.getStart()) && dr.contains(this.getEnd())) {
            return this.getLength();
        }

        // The two date ranges don't intersect
        return 0L;
    }

    /**
     * Utility function that returns true if this DateRange contains the
     * input LocalDateTime
     * @param dt LocalDateTime which we check to see if it falls within this
     * date range
     * @return boolean value true if the LocalDateTime falls within this
     * DateRange
     */
    public boolean contains(LocalDateTime dt) {

        if ( (dt.isEqual(this.getStart()) || dt.isAfter(this.getStart())) &&
             (dt.isEqual(this.getEnd()) || dt.isBefore(this.getEnd()))) {
            return true;
        }

        return false;
    }

    /**
     * A string representation of the Date Range
     * @return String value representing the Date Range
     */
     public String toString() {
		 return "[ " + this.start + " - " + this.end + " ]";
	 } 

}
