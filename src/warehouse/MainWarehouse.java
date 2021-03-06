package warehouse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;

import db.DBConnection;

public class MainWarehouse {
	String tableNameInWarehouseDb, tableNameInStagingDb, field_define_transform, listField, list_colum_datatype;
	int number_colum;
	GetDataFromDB get;
	int countRow = 0;

	public MainWarehouse() {
		get = new GetDataFromDB();
	}

	//du lieu khac nhau 2 field => khong trung.
	public boolean checkDuplicate(ResultSet rsData, ResultSet rsWareHouse) {
		try {

			int count = 0;
			for (int i = 1; i <= number_colum; i++) {
				try {
//					System.out.println(rsData.getString(i));
//					System.out.println(rsWareHouse.getObject(i + 1));
					if (!rsData.getString(i).equals(rsWareHouse.getObject(i + 1).toString())) {
						count++;
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
					continue;
				}
			}
			if (count > 2) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	public void tranferStagingToWarehouse(int idLog, int config) {
		countRow = 0;
		ResultSet rsdata = get.getDataInConfig(config);
		try {
			// laays các thuộc tín cần thiết từ config
			rsdata.next();
			tableNameInStagingDb = rsdata.getString("table_name_staging");
			tableNameInWarehouseDb = rsdata.getString("table_name_warehouse");
			field_define_transform = rsdata.getString("field_define_transform");
			number_colum = rsdata.getInt("number_column");
			listField = rsdata.getString("list_field_name");
			list_colum_datatype = rsdata.getString("list_colum_datatype");
			rsdata.close();
			// Kiểm tra table warehouse đã tồn tại hay chưa
			if (!get.checkTableExits(tableNameInWarehouseDb)) {
				// chưua tồn tại thì tạo
				if (!get.createTableWarehouse(tableNameInWarehouseDb, listField, number_colum, list_colum_datatype)) {
					return;
				}
			}
			get.doSpecialTaskInLog(idLog);
			rsdata = get.getDataStagingFromDb(tableNameInStagingDb);
			// duyệt từng dòng
			while (rsdata.next()) {
				// kiem tra du lieu co bi rong hay khong, 4 col rong => rong(false)
				if (!check_Colum_In_Row(rsdata))
					continue;
				// get du lieu tu warehouse dua vao id staging(ten cua table nay nam trong config)
				ResultSet rsWarehouse = get.getDataFromWarehouse(tableNameInWarehouseDb, field_define_transform, "=",
						rsdata.getString(field_define_transform));
				// keim tra su ton tai cua du lieu
				if (rsWarehouse.next()) {
					//kiem tra trung lap du lieu
					//neu trung lap thi bo qua
					if (checkDuplicate(rsdata, rsWarehouse)) {
						continue;
					// khac nhau 3 field => khong trung lap(false)
					} else {
//						// cap expired lai ngay hien tai 
						get.intsertDataToWarehouse(tableNameInWarehouseDb, listField, number_colum, rsdata,
								list_colum_datatype, countRow);
						// reopen PreparedStatement neu nno bi dong
						get.updateExpiredInWareHouse(tableNameInWarehouseDb, rsWarehouse.getInt(1), "now()");
					}
					// nguoi lai thi tao moi du lieu cho no
				} else {
					// insert du lieu vao warehouse
					get.intsertDataToWarehouse(tableNameInWarehouseDb, listField, number_colum, rsdata,
							list_colum_datatype, countRow);
					
				}
				//cap nhat so du lieu them,sua doi trong warehouse
				//countRow++;
			}
			get.updateStatus("OK WH", idLog, countRow);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Date tranferDate(int mode, String data) {
		switch (mode) {
		case 1:
			try {
				Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(data);
				return date1;
			} catch (ParseException e) {
				return tranferDate(2, data);
			}
		case 2:
			try {
				Date date1 = new SimpleDateFormat("yyyy/MM/dd").parse(data);
				return date1;
			} catch (ParseException e) {
				return tranferDate(3, data);
			}
		case 3:
			try {
				Date date1 = new SimpleDateFormat("MM/dd/yyyy").parse(data);
				return date1;
			} catch (ParseException e) {
				return tranferDate(4, data);
			}
		case 4:
			try {
				Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(data);
				return date1;
			} catch (ParseException e) {
				return tranferDate(5, data);
			}
		case 5:
			try {
				Date date1 = new SimpleDateFormat("dd-MM-yyyy").parse(data);
				return date1;
			} catch (ParseException e) {
				return tranferDate(6, data);
			}
		case 6:
			try {
				Date date1 = new SimpleDateFormat("MM-dd-yyyy").parse(data);
				return date1;
			} catch (ParseException e) {
				return null;
			}

		default:
			return null;
		}
	}

	//kiem tra du lieu co ton tai hay khong, neu du lieu rong >= 4 thi false
	private boolean check_Colum_In_Row(ResultSet rs) {
		try {
			String tmp;
			int count = 0;
			for (int i = 1; i < number_colum; i++) {
				tmp = rs.getString(i);
				if (tmp == null || tmp.equals("")) {
					count++;
					if (count >= 4) {
						return false;
					}
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {
		MainWarehouse main = new MainWarehouse();
//		Date date = main.tranferDate(1, "30-12-2019");
//		System.out.println(date.toInstant().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate().toString());
//		main.tranferStagingToWarehouse(89, 1);
//		for (int i = 14; i < 37; i++) {
//			main.tranferStagingToWarehouse(i, 3);
//		}
	}
}
