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
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.ushnisha.JobShop.JobShop.DEBUG_LEVELS;
import static com.ushnisha.JobShop.JobShop.LOG;

/**
 * A utility class for Calendar and CalendarShift based calculations
 */
public class CalendarUtils {

    private static double ZEROPLUS = 0.000001;

    /**
     * Calculates a start date given an end date and the lead time in minutes
     * It simulates an "End-On-Or-Before" behaviour
     * @param end LocalDateTime representing an end date/time
     * @param tm long value representing the time in minutes between a start and end
     * @return LocalDateTime value representing a start date/time given
     *          by end - tm
     */
    public static LocalDateTime calcStart(LocalDateTime end, long tm) {
        return end.minusMinutes(tm);
    }

    /**
     * Calculates a start date given an end date and the lead time in minutes
     * It simulates an "End-On-Or-Before" behaviour while representing the
     * calendar availability
     * @param cal a Calendar that specifies the working shifts and efficiency
     *            that must be used for the calculation of the start date/time
     * @param end LocalDateTime representing an end date/time
     * @param tm long value representing the *working* time in minutes between
     *           the start and end
     * @return LocalDateTime value representing a start date/time given
     *          an end date/time and a lead time in minutes and a calendar
     *          The calculation will account for working shifts, efficicencies etc.
     */
    public static DateRange calcEndBefore(Calendar cal, LocalDateTime end,
                                          long tm) {

        List<CalendarShift> shifts = cal.getShifts();
        LocalDateTime validStart = null;
        LocalDateTime validEnd = calcValidDateOnOrBefore(cal, end);

        long remaining_tm = tm;
        LocalDateTime currentEnd = validEnd;
        int currentShiftIdx = getShiftIndex(shifts, currentEnd);

        JobShop.LOG("Base LT from calcStart: " + remaining_tm, DEBUG_LEVELS.MAXIMAL);

        while (remaining_tm > 0) {
            CalendarShift cshift = shifts.get(currentShiftIdx);
            long time_in_currentShift = ChronoUnit.MINUTES.between(cshift.getStart(), currentEnd);
            time_in_currentShift = (long) Math.ceil(time_in_currentShift * cshift.getValue());

            JobShop.LOG(cshift.getStart() + "-" + currentEnd + "; = " +
                        time_in_currentShift + "; Remaining Time: " + remaining_tm,
                        DEBUG_LEVELS.MAXIMAL);

            if (time_in_currentShift >= remaining_tm) {
                validStart = currentEnd.minusMinutes((long) Math.ceil(remaining_tm/cshift.getValue()));
                remaining_tm -= time_in_currentShift;
            }
            else {
                remaining_tm -= time_in_currentShift;
                currentShiftIdx--;
                if (currentShiftIdx < 0) {
                    return calcStartAfter(cal, shifts.get(0).getStart(), tm);
                }
                currentEnd = shifts.get(currentShiftIdx).getEnd();
            }
        }
        return new DateRange(validStart, validEnd);
    }

    /**
     * Calculates an end date given a start date and the lead time in minutes
     * It simulates a "Start-On-Or-After" behaviour
     * @param start LocalDateTime representing an end date/time
     * @param tm long value representing the time in minutes between a start and end
     * @return LocalDateTime value representing an end date/time given
     *          by start + tm
     */
    public static LocalDateTime calcEnd(LocalDateTime start, long tm) {
        return start.plusMinutes(tm);
    }

    /**
     * Calculates an end date given a start date and the lead time in minutes
     * It simulates a "Start-On-Or-After" behaviour while representing the
     * calendar availability
     * @param cal a Calendar that specifies the working shifts and efficiency
     *            that must be used for the calculation of the end date/time
     * @param start LocalDateTime representing a start date/time
     * @param tm long value representing the *working* time in minutes between
     *           the start and end
     * @return LocalDateTime value representing an end date/time given
     *          a start date/time and a lead time in minutes and a calendar
     *          The calculation will account for working shifts, efficicencies etc.
     */
    public static DateRange calcStartAfter(Calendar cal, LocalDateTime start,
                                        long tm) {
        List<CalendarShift> shifts = cal.getShifts();
        LocalDateTime validStart = calcValidDateOnOrAfter(cal, start);
        LocalDateTime validEnd = null;

        long remaining_tm = tm;
        LocalDateTime currentStart = validStart;
        int currentShiftIdx = getShiftIndex(shifts, currentStart);

        JobShop.LOG("Base LT from calcEnd: " + remaining_tm, DEBUG_LEVELS.MAXIMAL);

        while (remaining_tm > 0) {
            CalendarShift cshift = shifts.get(currentShiftIdx);
            long time_in_currentShift = ChronoUnit.MINUTES.between(currentStart, cshift.getEnd());
            time_in_currentShift = (long) Math.ceil(time_in_currentShift * cshift.getValue());

			JobShop.LOG(currentStart + "-" + cshift.getEnd() + "; = " +
                        time_in_currentShift + "; Remaining Time: " + remaining_tm,
                        DEBUG_LEVELS.MAXIMAL);

            if (time_in_currentShift >= remaining_tm) {
                validEnd = currentStart.plusMinutes((long) Math.ceil(remaining_tm/cshift.getValue()));
                remaining_tm -= time_in_currentShift;
            }
            else {
                remaining_tm -= time_in_currentShift;
                currentShiftIdx++;
                if (currentShiftIdx == shifts.size()) {
                    return calcEndBefore(cal, shifts.get(shifts.size() - 1).getEnd(), tm);
                }
                currentStart = shifts.get(currentShiftIdx).getStart();
            }
        }
        return new DateRange(validStart, validEnd);
    }

    /**
     * Calculates the earliest date on or before a given input date when
     * we have a working shift for the specified input calendar
     * @param cal A Calendar object with specified working shifts
     * @param dt LocalDateTime representing a date/time
     * @return LocalDateTime value representing the earliest date/time that
     *          falls on or before dt, and lies in a working shift
     */
     public static LocalDateTime calcValidDateOnOrBefore(Calendar cal, LocalDateTime dt) {

        LocalDateTime date = dt;

        List<CalendarShift> shifts = cal.getShifts();
        int shiftIndex = getShiftIndex(shifts, date);

        if (date.isEqual(shifts.get(shiftIndex).getStart())) {
            shiftIndex--;
            if (shiftIndex < 0) {
                shiftIndex = 0;
            }
            date = shifts.get(shiftIndex).getEnd();
        }

        int workingIndex = getPrevWorkingShiftIndex(shifts, shiftIndex);
        if (workingIndex < 0) {
            workingIndex = 0;
        }
        if (shiftIndex == workingIndex) {
            return date;
        }
        else {
            return shifts.get(workingIndex).getEnd();
        }
    }

   /**
     * Calculates the earliest date on or after a given input date when
     * we have a working shift for the specified input calendar
     * @param cal A Calendar object with specified working shifts
     * @param dt LocalDateTime representing a date/time
     * @return LocalDateTime value representing the earliest date/time that
     *          falls on or after dt, and lies in a working shift
     */
     public static LocalDateTime calcValidDateOnOrAfter(Calendar cal, LocalDateTime dt) {

        LocalDateTime date = dt;

        List<CalendarShift> shifts = cal.getShifts();
        int shiftIndex = getShiftIndex(shifts, date);

        if (date.isEqual(shifts.get(shiftIndex).getEnd())) {
            shiftIndex++;
            if (shiftIndex == shifts.size()) {
                shiftIndex = shifts.size() - 1;
            }
            date = shifts.get(shiftIndex).getStart();
        }

        int workingIndex = getNextWorkingShiftIndex(shifts, shiftIndex);
        if (workingIndex == shifts.size()) {
            workingIndex--;
        }

        if (shiftIndex == workingIndex) {
            return date;
        }
        else {
            return shifts.get(workingIndex).getStart();
        }
    }

   /**
     * Given a List of CalendarShift objects, identifies the index of the shift
     * that contains an input date. Assumes that there will always be a valid
     * shift that will contain the input date
     * @param shifts a list of CalendarShift objects
     * @param dt LocalDateTime representing a date/time
     * @return int value that represents the index in the list of shifts that
     *          identifies the shift within which the specified date falls
     */
    public static int getShiftIndex(List<CalendarShift> shifts, LocalDateTime dt) {

        int min = 0;
        int max = shifts.size();
        int avg = (min + max)/2;
        boolean found = false;

        while (!found) {
            CalendarShift avgShift = shifts.get(avg);

            JobShop.LOG("Min: " + min + "; Max: " + max + "; Avg: " +
                        avg + "; Shift Start: " + avgShift.getStart() +
                        "; Shift End: " + avgShift.getEnd(),
                        DEBUG_LEVELS.MAXIMAL);

            if (dt.isBefore(avgShift.getStart())) {
                max = avg;
                avg = (min+max)/2;
            }
            else if (dt.isEqual(avgShift.getEnd()) || dt.isAfter(avgShift.getEnd())) {
                min = avg;
                avg = (min + max)/2;
            }
            else {
                found = true;
            }
        }

        JobShop.LOG("Date: " + dt + " is in shift index: " + avg, DEBUG_LEVELS.MAXIMAL);

        return avg;
    }

   /**
     * Given a List of CalendarShift objects, and an index, identifies the index
     * of the nearest working shift that is lesser than or equal to the input index value
     * @param shifts a list of CalendarShift objects
     * @param index integer value of a shift index that we should start searching from
     * @return int value that represents the index in the list of shifts that
     *          is a working shift that is lesser than or equal to the input index value
     */
    public static int getPrevWorkingShiftIndex(List<CalendarShift> shifts, int index) {
        int idx = index;
        while (!shifts.get(idx).isWorking()) {
            idx--;
            if (idx < 0) {
                return idx;
            }
        }
        return idx;
    }

   /**
     * Given a List of CalendarShift objects, and an index, identifies the index
     * of the nearest working shift that is greater than or equal to the input index value
     * @param shifts a list of CalendarShift objects
     * @param index integer value of a shift index that we should start searching from
     * @return int value that represents the index in the list of shifts that
     *          is a working shift that is greater than or equal to the input index value
     */
    public static int getNextWorkingShiftIndex(List<CalendarShift> shifts, int index) {
        int idx = index;
        while (!shifts.get(idx).isWorking()) {
            idx++;
            if (idx == shifts.size()) {
                return idx;
            }
        }
        return idx;
    }
}
