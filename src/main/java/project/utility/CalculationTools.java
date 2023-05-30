package project.utility;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class CalculationTools {

    //Dividerer tids estimatet med antallet af hverdage mellem projekt start og deadline.
    public static int projectHoursPerDay(int timeEstimate, LocalDate start, LocalDate deadline) {

        LocalDate today = LocalDate.now();
        LocalDate calculationDate;
        int weekdayCounter = 0;

        //Hvis deadline er før start eller er overskredet, så giver den 0
        if (deadline.isBefore(start) || deadline.isBefore(today)) {
            return 0;
        }

        //Hvis idag er en senere dato end projekt start, så bruger den idag i stedet
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
