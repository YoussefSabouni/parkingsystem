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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String VEHICLE_REG_NUMBER = "ABCDEF";

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

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

        dataBasePrepareService.clearDataBaseEntries();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(VEHICLE_REG_NUMBER);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    @DisplayName("Vehicle entry test in DB")
    public void testParkingACar() {

        when(inputReaderUtil.readSelection()).thenReturn(1);
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpot);
        assertThat(ticketDAO.getTicket(VEHICLE_REG_NUMBER).getVehicleRegNumber()).isEqualTo(VEHICLE_REG_NUMBER);
        assertThat(ticketDAO.getTicket(VEHICLE_REG_NUMBER).getInTime()).isNotNull();
        assertThat(ticketDAO.getTicket(VEHICLE_REG_NUMBER).getOutTime()).isNull();
        assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).isEqualTo(2);
    }

    @Test
    @DisplayName("Test of the exit of a vehicle in DB")
    public void testParkingLotExit() {

        ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - 31 * 60 * 1000);
        ticket.setInTime(inTime);
        ticket.setVehicleRegNumber(VEHICLE_REG_NUMBER);
        ticketDAO.saveTicket(ticket);
        parkingSpotDAO.updateParking(parkingSpot);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        assertThat(ticketDAO.getTicket(VEHICLE_REG_NUMBER)
                            .getOutTime()).isAfter(ticketDAO.getTicket(VEHICLE_REG_NUMBER).getInTime());
        assertThat(ticketDAO.getTicket(VEHICLE_REG_NUMBER).getPrice()).isGreaterThan(0);
    }

}
