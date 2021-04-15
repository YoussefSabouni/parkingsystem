package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.SECONDS;

public class FareCalculatorService {

    private TicketDAO ticketDAO;

    public FareCalculatorService() {

        this.ticketDAO = new TicketDAO();
    }

    public FareCalculatorService(TicketDAO TicketDAO) {

        this.ticketDAO = TicketDAO;
    }

    public void calculateFare(Ticket ticket) {

        double rate;

        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        Instant inHour  = ticket.getInTime().toInstant();
        Instant outHour = ticket.getOutTime().toInstant();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        double duration        = SECONDS.between(inHour, outHour);
        double durationToHours = duration / 60 / 60;

        if (durationToHours <= 0.5) {
            ticket.setPrice(0);
            return;
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    rate = Fare.CAR_RATE_PER_HOUR;
                    break;
                }
                case BIKE: {
                    rate = Fare.BIKE_RATE_PER_HOUR;
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }

        if (ticketDAO.isRecurringUser(ticket.getVehicleRegNumber())) {
            ticket.setPrice((durationToHours * rate) * 0.95);
            System.out.println("You have received a 5% promotion for your loyalty");
        } else {
            ticket.setPrice(durationToHours * rate);
        }
    }
}