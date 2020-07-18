package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import model.SinhVien;

public class Staging {

	public static ArrayList<SinhVien> getAllSinhVien() {
		Connection con = ConnectDB.getConnection();

		ArrayList<SinhVien> stagings = new ArrayList<SinhVien>();
		String sqlWasehouse = "select * from ssinhvien";
		Statement statement;

		try {
			statement = con.createStatement();
			ResultSet rs = statement.executeQuery(sqlWasehouse);
			while (rs.next()) {
				stagings.add(new SinhVien("",rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9),
						rs.getString(10), rs.getString(11), ""));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stagings;
	}
	public static void main(String[] args) {
//		System.out.println(Staging.getAllSinhVien().toString());
	}
}