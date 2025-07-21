package com.garage.reservation.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;

/**
 * Utilitaires pour manipuler les dates et heures
 */
public class DateTimeUtil {
    
    /**
     * Obtient le début du jour (00:00:00) pour un instant donné en UTC
     */
    public static Instant getStartOfDay(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    
    /**
     * Obtient la fin du jour (23:59:59.999999999) pour un instant donné en UTC
     */
    public static Instant getEndOfDay(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        return date.atStartOfDay(ZoneOffset.UTC).plusDays(1).minusNanos(1).toInstant();
    }
    
    /**
     * Obtient le début du jour suivant (00:00:00 du jour d+1) pour un instant donné en UTC
     */
    public static Instant getStartOfNextDay(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        return date.atStartOfDay(ZoneOffset.UTC).plusDays(1).toInstant();
    }
    
    /**
     * Obtient le début de la semaine (lundi 00:00:00) pour un instant donné en UTC
     */
    public static Instant getStartOfWeek(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate mondayOfWeek = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        return mondayOfWeek.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    
    /**
     * Obtient la fin de la semaine (dimanche 23:59:59.999999999) pour un instant donné en UTC
     */
    public static Instant getEndOfWeek(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate sundayOfWeek = date.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        return sundayOfWeek.atStartOfDay(ZoneOffset.UTC).plusDays(1).minusNanos(1).toInstant();
    }
    
    /**
     * Obtient le début de la semaine suivante (lundi 00:00:00 de la semaine suivante) pour un instant donné en UTC
     */
    public static Instant getStartOfNextWeek(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate mondayOfWeek = date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        return mondayOfWeek.atStartOfDay(ZoneOffset.UTC).plusWeeks(1).toInstant();
    }
} 