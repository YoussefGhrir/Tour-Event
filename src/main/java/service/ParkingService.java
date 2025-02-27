package service;

import model.Parking;
import utils.MyDatabse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingService {

    private Connection con;

    public ParkingService() {
        con = MyDatabse.getInstance().getCon();
    }

    public List<Parking> getAllParkings() {
        List<Parking> parkings = new ArrayList<>();
        String query = "SELECT * FROM parking";
        try (Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                Parking parking = new Parking();
                parking.setIdPark(rs.getInt("idPark"));
                parking.setNomPark(rs.getString("nomPark"));
                parkings.add(parking);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des parkings : " + e.getMessage());
        }
        return parkings;
    }

    public List<Parking> searchParkings(String searchText) {
        List<Parking> parkings = new ArrayList<>();
        String query = "SELECT * FROM parking WHERE nomPark LIKE ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + searchText + "%");
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Parking parking = new Parking();
                parking.setIdPark(rs.getInt("idPark"));
                parking.setNomPark(rs.getString("nomPark"));
                parkings.add(parking);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des parkings : " + e.getMessage());
        }
        return parkings;
    }
}