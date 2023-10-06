package com.driver.repository;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import org.springframework.stereotype.Repository;

import java.util.*;

import static org.apache.coyote.http11.Constants.a;

@Repository
public class AirportRepository {
    Map<String, Airport> airportMap = new HashMap<>();
    Map<Integer, Flight> flightMap = new HashMap<>();
    Map<Integer, Passenger> passengerMap =  new HashMap<>();
    Map<Integer, List<Integer>> flightPassangersMap = new HashMap<>();
    Map<Integer, List<Integer>> passangerFlightsMap = new HashMap<>();
    public void addAirport(Airport airport) {
        airportMap.put(airport.getAirportName(),airport);
    }

    public String getLargestAirportName() {
        String largestAirport = "";
        int maxTerminal = -1;
        for(Airport airport : airportMap.values()){
            if(maxTerminal < airport.getNoOfTerminals()){
                maxTerminal = airport.getNoOfTerminals();
                largestAirport = airport.getAirportName();
            }
            else if (airport.getNoOfTerminals() == maxTerminal &&
                    airport.getAirportName().compareTo(largestAirport) < 0) {
                largestAirport = airport.getAirportName();
            }
        }
        return largestAirport;
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        double shortestDuration = Double.MAX_VALUE;
        for (Flight flight : flightMap.values()){
            if(flight.getFromCity().equals(fromCity) && flight.getToCity().equals(toCity)){
                shortestDuration = Math.min(flight.getDuration(),shortestDuration);
            }
        }

        if(shortestDuration == Double.MAX_VALUE){
            return -1;
        }
        return shortestDuration;
    }

    public int getNumberOfPeopleOn(Date date, String airportName) {
        int numberOfPeople = 0;
        City airportCity = airportMap.get(airportName).getCity();
        for (Flight flight : flightMap.values()) {
            if (flight.getFlightDate().equals(date)) {
                if (flight.getFromCity().equals(airportCity) || flight.getToCity().equals(airportCity)) {
                    numberOfPeople += flight.getMaxCapacity();
                }
            }
        }
        return numberOfPeople;
    }

    public int calculateFlightFare(Integer flightId) {
        int flightFare = 3000;
        if(flightPassangersMap.containsKey(flightId)){
            int bookedTickets = flightPassangersMap.get(flightId).size();
            flightFare += bookedTickets*50;
            return flightFare;
        }
        else{
            return -1;
        }
    }

    public String bookATicket(Integer flightId, Integer passengerId) {

        //check failure conditions

        Flight flight = flightMap.get(flightId);
        Passenger passenger = passengerMap.get(passengerId);
        // Invalid flightId or passengerId
        try{
        if (flight == null || passenger == null){
            return "FAILURE";
        }
        else if(passangerFlightsMap.containsKey(passengerId) && passangerFlightsMap.get(passengerId).contains(flightId)) {
            // Passenger already booked a flight
            return "FAILURE";
        }
        else if(flightPassangersMap.containsKey(flightId)){
            if(flightPassangersMap.get(flightId).contains(passengerId)){
                return "FAILURE";
            }
        }

        // Max capacity reached
            if (flight != null && flightPassangersMap.containsKey(flightId) &&
                    flight.getMaxCapacity() > flightPassangersMap.get(flightId).size()) {
                flightPassangersMap.get(flightId).add(passengerId);
                List<Integer> flightList = passangerFlightsMap.getOrDefault(passengerId,new ArrayList<>());
                passangerFlightsMap.put(passengerId,flightList);
                return "SUCCESS";
            }else{
                List<Integer> passengerList = new ArrayList<>();
                passengerList.add(passengerId);
                flightPassangersMap.put(flightId,passengerList);
                List<Integer> flightList = passangerFlightsMap.getOrDefault(passengerId,new ArrayList<>());
                passangerFlightsMap.put(passengerId,flightList);
                return "SUCCESS";
            }
        }catch (NullPointerException e){
            return "FAILURE";
        }
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {
        if(flightMap.containsKey(flightId)){
            if(flightPassangersMap.containsKey(flightId)){
                flightPassangersMap.get(flightId).remove(passengerId);
                passangerFlightsMap.get(passengerId).remove(flightId);
                return "SUCCESS";
            }
        }
        else{
            return "SUCCESS";
        }

        return "FAILURE";
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
        return passangerFlightsMap.get(passengerId).size();
    }


    public void addFlight(Flight flight) {
        flightMap.put(flight.getFlightId(),flight);
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        if(flightMap.containsKey(flightId)){
           City airportCity  = flightMap.get(flightId).getFromCity();
           for(Airport airport : airportMap.values()){
               if(airport.getCity().equals(airportCity)){
                   return airport.getAirportName();
               }
           }
        }else{
            return null;
        }
        return null;
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        int n = 0;
        List<Integer> passangerList = new ArrayList<>();
       if(flightPassangersMap.containsKey(flightId)){
           n = flightPassangersMap.get(flightId).size();
           passangerList = flightPassangersMap.get(flightId);
       }else{
           return 0;
       }
       int fare = 0;
       for(int i = 0; i < n; i++){
           fare += 3000 + (50 * i);
       }
        return fare;
    }

    public String addPassenger(Passenger passenger) {
        passengerMap.put(passenger.getPassengerId(), passenger);
        return "SUCCESS";
    }
}
