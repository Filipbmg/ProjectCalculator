package project.utility;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class CalculationTools {
    public static int projectHoursPerDay(int timeEstimate, LocalDate start, LocalDate deadline) {
        LocalDate today = LocalDate.now();
        LocalDate calculationDate;
        int weekdayCounter = 0;
        if (today.isAfter(start)) {
            calculationDate = today;
        } else {
            calculationDate = start;
        }
        while(!calculationDate.isAfter(deadline)) {
            if (calculationDate.getDayOfWeek() != DayOfWeek.SATURDAY &&  calculationDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                weekdayCounter++;
            }
            calculationDate = calculationDate.plusDays(1);
        }
        return (int) Math.ceil((double) timeEstimate / weekdayCounter);
    }
}
