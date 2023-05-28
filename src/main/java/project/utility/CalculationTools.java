package project.utility;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class CalculationTools {
    public static int projectHoursPerDay(int timeEstimate, LocalDate projectDeadline) {
        LocalDate today = LocalDate.now();
        int weekdayCounter = 0;
        while(!today.isAfter(projectDeadline)) {
            if (today.getDayOfWeek() != DayOfWeek.SATURDAY &&  today.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdayCounter++;
            }
            today = today.plusDays(1);
        }
        return (int) Math.ceil((double) timeEstimate / weekdayCounter);
    }

    public static int subprojectHoursPerDay(int timeEstimate, LocalDate subprojectStart, LocalDate subprojectDeadline) {
        LocalDate today = LocalDate.now();
        LocalDate calculationDate;
        int weekdayCounter = 0;
        if (today.isAfter(subprojectStart)) {
            calculationDate = today;
        } else {
            calculationDate = subprojectStart;
        }
        while(!calculationDate.isAfter(subprojectDeadline)) {
            if (calculationDate.getDayOfWeek() != DayOfWeek.SATURDAY &&  calculationDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdayCounter++;
            }
            calculationDate = calculationDate.plusDays(1);
        }
        return (int) Math.ceil((double) timeEstimate / weekdayCounter);
    }
}
