package com.lost2found.service;

import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

public class MatchingUtils {

    public static double cosine(double[] a, double[] b) {
        if (a==null || b==null || a.length != b.length) return 0.0;
        double dot=0, na=0, nb=0;
        for (int i=0;i<a.length;i++){
            dot += a[i]*b[i];
            na += a[i]*a[i];
            nb += b[i]*b[i];
        }
        if (na==0 || nb==0) return 0.0;
        return dot / (Math.sqrt(na)*Math.sqrt(nb));
    }

    public static double locationSim(String locA, String locB) {
        if (locA==null || locB==null) return 0.5;
        String a = locA.trim().toLowerCase();
        String b = locB.trim().toLowerCase();
        if (a.equals(b)) return 1.0;
        if (a.contains(b) || b.contains(a)) return 0.8;
        // token overlap heuristic
        for (String token : a.split("\\s+")) {
            if (b.contains(token)) return 0.6;
        }
        return 0.2;
    }

    public static double dateProx(LocalDate d1, LocalDate d2) {
        if (d1 == null || d2 == null) return 0.5;
        long days = Math.abs(ChronoUnit.DAYS.between(d1, d2));
        if (days == 0) return 1.0;
        if (days <= 3) return 0.8;
        if (days <= 7) return 0.5;
        return 0.0;
    }

    public static double score(double textSim, double imgDescSim, double loc, double date) {
        return 0.4*textSim + 0.3*imgDescSim + 0.2*loc + 0.1*date;
    }
}