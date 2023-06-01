package project.utility;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class CalculationTools {

    //Dividerer tids estimatet med antallet af hverdage mellem projekt start og deadline.
    public static int projectHoursPerDay(int timeEstimate, LocalDate start, LocalDate deadline) {
        LocalDate today = LocalDate.now();
        LocalDate calculationDate;
        int weekdayCounter = 0;

        //Via thymeleaf bruger vi disse værdier til at generere en respons
        //hvis deadline er overskredet eller fejlagtig fx. deadline før start
        if (deadline.isBefore(start)) {
            return -1;}
        if (deadline.isBefore(today)) {
            return -2;
        }

        //Hvis "today" er en senere dato end projekt start, så bruger den dato i stedet
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
