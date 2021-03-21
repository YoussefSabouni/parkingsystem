package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String VEHICULE_REG_NUMBER = "ABCDEF";

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    @Spy
    private static ParkingSpotDAO parkingSpotDAO;

    private static TicketDAO ticketDAO;

    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    private static FareCalculatorService fareCalculatorService;

    private ParkingSpot parkingSpot;

    @BeforeAll
    private static void setUp() throws Exception {

        fareCalculatorService = new FareCalculatorService();
        parkingSpotDAO                = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO                     = new TicketDAO();
        ticketDAO.dataBaseConfig      = dataBaseTestConfig;
        dataBasePrepareService        = new DataBasePrepareService();
    }

    @AfterAll
    private static void tearDown(){

    }

    @BeforeEach
    private void setUpPerTest() throws Exception {

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICULE_REG_NUMBER);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar() {

        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        //        when(parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
        //        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpot);
        assertThat(ticketDAO.getTicket(VEHICULE_REG_NUMBER).getVehicleRegNumber()).isEqualTo(VEHICULE_REG_NUMBER);
        assertThat(ticketDAO.getTicket(VEHICULE_REG_NUMBER).getInTime()).isNotNull();
        assertThat(ticketDAO.getTicket(VEHICULE_REG_NUMBER).getOutTime()).isNull();
        assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).isEqualTo(2);
    }

    @Test
    public void testParkingLotExit() throws InterruptedException {

        testParkingACar();

        Thread.sleep(1000);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket(VEHICULE_REG_NUMBER);

        Date date = new Date();
        date.setTime(System.currentTimeMillis() + 30 * 60 * 1000);
        ticket.setOutTime(date);
        fareCalculatorService.calculateFare(ticket);
        ticketDAO.updateTicket(ticket);

        //TODO: check that the fare generated and out time are populated correctly in the database
        assertThat(ticketDAO.getTicket(VEHICULE_REG_NUMBER)
                            .getOutTime()).isAfter(ticketDAO.getTicket(VEHICULE_REG_NUMBER).getInTime());
        assertThat(ticketDAO.getTicket(VEHICULE_REG_NUMBER).getPrice()).isGreaterThan(0);

    }

}
