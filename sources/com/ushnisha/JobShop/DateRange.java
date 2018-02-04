/**
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
     * A string representation of the Date Range
     * @return String value representing the Date Range
     */
     public String toString() {
		 return "[ " + this.start + " - " + this.end + " ]";
	 } 

}
