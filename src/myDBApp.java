import java.io.*;
import java.sql.*;
import java.util.*;

public class myDBApp {

	// NOTE: You will need to change some variables from START to END.
	public static void main(String[] argv) throws SQLException {
		// START
		// Enter your username.
		String user = "wjis132";
		// Enter your database password, NOT your university password.
		String password = "queeba";

		/**
		 * IMPORTANT: If you are using NoMachine, you can leave this as it is.
		 * 
		 * Otherwise, if you are using your OWN COMPUTER with TUNNELLING: 1) Delete the
		 * original database string and 2) Remove the '//' in front of the second
		 * database string.
		 */
		String database = "teachdb.cs.rhul.ac.uk";
		// String database = "localhost";
		// END

		Connection connection = connectToDatabase(user, password, database);
		if (connection != null) {
			System.out.println("SUCCESS: You made it!" + "\n\t You can now take control of your database!\n");
		} else {
			System.out.println("ERROR: \tFailed to make connection!");
			System.exit(1);
		}
		// Now we're ready to use the DB. You may add your code below this line.

		// Exercise 1: Querying the Database
		String query = "SELECT * FROM BRANCH;";
		ResultSet rs = executeQuery(connection, query);
		try {
			while (rs.next()) {
				System.out.println(rs.getString(1) + " " + rs.getString(2) + " " + rs.getString(3));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Exercise 2: Creating and Dropping Tables
		query = "SELECT * FROM customer;";
		System.out.println("Dropping table customer...");
		dropTable(connection, "customer;");
		ResultSet exercise2 = executeQuery(connection, query);
		System.out.println("Creating table customer...");
		createTable(connection,
				"customer (customer_name varchar(15), customer_street varchar(15), customer_city varchar(15), primary key (customer_name));");
		exercise2 = executeQuery(connection, query);

		// Exercise 3: Inserting Values into a Table from a File
		System.out.println(insertIntoTableFromFile(connection, "customer", "customers.txt"));

		// Exercise 4: Prepared Statements
		String SQL = "UPDATE account " + "SET balance = balance + ? " + "WHERE branch_name = ?";
		int affectedRows = 0;
		try {
			PreparedStatement pstmt = connection.prepareStatement(SQL);
			pstmt.setInt(1, 1000);
			pstmt.setString(2, "Brighton");
			affectedRows = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("DEBUG: Updated " + affectedRows + " rows.");

		// Exercise 5: Batch Statements
		String SQL2 = "UPDATE account " + "SET balance = balance + ? " + "WHERE branch_name = ?";
		int[] affectedRows2 = null;
		try {
			PreparedStatement pstmt = connection.prepareStatement(SQL2);
			pstmt.setInt(1, 1000);
			pstmt.setString(2, "Brighton");
			pstmt.addBatch();

			pstmt.setInt(1, 1000);
			pstmt.setString(2, "Redwood");
			pstmt.addBatch();

			pstmt.setInt(1, 1000);
			pstmt.setString(2, "Downtown");
			pstmt.addBatch();

			affectedRows2 = pstmt.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("DEBUG: Updated " + Arrays.toString(affectedRows2) + " rows.");

		executeQuery(connection, "DELETE FROM customer");
		// Using prepared statements and batching, re-write your solution to exercise 3 in a new function
		insertIntoTablePrepared(connection, "customer", "customers.txt");
	}

	// You can write your new methods here.

	// Exercise 1: Querying the Database
	public static ResultSet executeQuery(Connection connection, String query) {
		System.out.println("DEBUG: Executing query...");
		try {
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Exercise 2: Creating and Dropping Tables
	public static void dropTable(Connection connection, String table) {
		System.out.println("DEBUG: Dropping table...");
		try {
			Statement st = connection.createStatement();
			st.execute("DROP TABLE " + table);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void createTable(Connection connection, String tableDescription) {
		System.out.println("DEBUG: Creating table...");
		try {
			Statement st = connection.createStatement();
			st.execute("CREATE TABLE " + tableDescription);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// Exercise 3: Inserting Values into a Table from a File
	public static int insertIntoTableFromFile(Connection connection, String table, String filename) {
		int numRows = 0;
		String currentLine = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			Statement st = connection.createStatement();
			// Read in each line of the file until we reach the end.
			while ((currentLine = br.readLine()) != null) {
				String[] values = currentLine.split(",");
				String composedLine = "INSERT INTO " + table + " VALUES (";
				for (int i = 0; i < values.length; i++) {
					composedLine += "'" + values[i] + "'";
					if ((values.length - i) > 1)
						composedLine += ",";
				}
				composedLine += ");";
				// Finally, execute the entire composed line.
				numRows += st.executeUpdate(composedLine);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return numRows;
	}

	// Exercise 5: Batch Statements
	public static int[] insertIntoTablePrepared(Connection connection, String table, String filename) {
		int[] numRows = null;
		String currentLine = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String SQL = "INSERT INTO " + table + " VALUES ( ? , ? , ? )";
			PreparedStatement pstmt = connection.prepareStatement(SQL);
			// Read in each line of the file until we reach the end.
			while ((currentLine = br.readLine()) != null) {
				String[] values = currentLine.split(",");
				for (int i = 0; i < values.length; i++) {
					pstmt.setString(i + 1, values[i]);
				}
				// Finally, execute the entire composed line.
				pstmt.addBatch();
			}
			numRows = pstmt.executeBatch();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return numRows;
	}

	// ADVANCED: This method is for advanced users only. You should not need to
	// change this!
	public static Connection connectToDatabase(String user, String password, String database) {
		System.out.println("------ Testing PostgreSQL JDBC Connection ------");
		Connection connection = null;
		try {
			String protocol = "jdbc:postgresql://";
			String dbName = "/CS2855/";
			String fullURL = protocol + database + dbName + user;
			connection = DriverManager.getConnection(fullURL, user, password);
		} catch (SQLException e) {
			String errorMsg = e.getMessage();
			if (errorMsg.contains("authentication failed")) {
				System.out.println(
						"ERROR: \tDatabase password is incorrect. Have you changed the password string above?");
				System.out.println("\n\tMake sure you are NOT using your university password.\n"
						+ "\tYou need to use the password that was emailed to you!");
			} else {
				System.out.println("Connection failed! Check output console.");
				e.printStackTrace();
			}
		}
		return connection;
	}
}