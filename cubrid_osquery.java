import java.sql.*;
import java.io.*;

public class cubrid_osquery
{
	private static Connection getConnection() throws SQLException, ClassNotFoundException
	{
		Class.forName("cubrid.jdbc.driver.CUBRIDDriver");
		Connection c = DriverManager.getConnection("jdbc:default:connection:");
		c.setAutoCommit(false);
		return c;
	}

	private static void truncateTable(Connection c, String table) throws SQLException
	{
		Statement delStmt = c.createStatement();
		delStmt.executeUpdate("DELETE FROM " + table);
		delStmt.close();	
	}

	public static void osquery_load_processes() throws SQLException
	{
		try
		{
			Connection c = getConnection();
			truncateTable(c, "_osquery_processes");

			// Prepare insert statement
			PreparedStatement stmt = c.prepareStatement("INSERT INTO _osquery_processes (pid, name) VALUES (?, ?)");

			// Prepare shell call
			Process shell = Runtime.getRuntime().exec("ps -eo pid,user,comm");
			BufferedReader input = new BufferedReader(new InputStreamReader(shell.getInputStream()));

			boolean headerLine = true;
			String line;

			while ((line = input.readLine()) != null)
			{
				if (headerLine)
				{
					// Skip header line
					headerLine = false;
					continue;
				}

				// Normalize separators
				line = line.replaceAll("^\\s+", "");
				line = line.replaceAll("\\s+", " ");

				// Split string
				String[] split = line.split(" ");

				// Add insert
				stmt.setInt(1, Integer.parseInt(split[0]));
				stmt.setString(2, split[2]);
				stmt.addBatch();
			}

			stmt.executeBatch();
			stmt.close();
			c.commit();
			c.close();
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
}
