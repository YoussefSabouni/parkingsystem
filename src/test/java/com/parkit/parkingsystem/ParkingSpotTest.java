package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParkingSpotTest {

    private static ParkingSpot parkingSpotCar;

    @BeforeAll
    public static void setUp() {

        parkingSpotCar = new ParkingSpot(1, ParkingType.CAR, true);
    }

    @Test
    public void equals_shouldReturnTrue_forSameParkingSpot() {

        assertTrue(parkingSpotCar.equals(parkingSpotCar));
    }

    @Test
    public void equals_shouldReturnFalse_forDifferentParkingSpot() {

        ParkingSpot parkingSpotBike = new ParkingSpot(4, ParkingType.BIKE, false);

        assertFalse(parkingSpotCar.equals(parkingSpotBike));
    }

}
