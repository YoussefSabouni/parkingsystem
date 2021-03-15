package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.SECONDS;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {

        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        Instant inHour  = ticket.getInTime().toInstant();
        Instant outHour = ticket.getOutTime().toInstant();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration        = SECONDS.between(inHour, outHour);
        double durationToHours = duration / 60 / 60;


        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(durationToHours * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationToHours * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}