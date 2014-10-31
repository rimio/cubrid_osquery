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

	public static void osquery_snapshot() throws SQLException
	{
		osquery_load_processes();
		osquery_load_etc_hosts();
	}

	public static void osquery_load_processes() throws SQLException
	{
		try
		{
			Connection c = getConnection();
			truncateTable(c, "_osquery_processes");

			// Prepare insert statement
			PreparedStatement stmt = c.prepareStatement(
				"INSERT INTO _osquery_processes (pid, name, `user`, cmdline, path, on_disk, resident_size, phys_footprint, user_time, system_time, start_time, parent)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			// Prepare shell call
			Process shell = Runtime.getRuntime().exec("ps -eo pid,comm,user,rss,vsize,utime,stime,start_time,ppid,args"); // args is last!
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

				// Set values
				stmt.setInt(1, Integer.parseInt(split[0])); // pid
				stmt.setString(2, split[1]); // name
				stmt.setString(3, split[2]); // user
				stmt.setString(5, split[9]); // path == first of args
				stmt.setString(6, "0"); // on_disk
				stmt.setString(7, split[3]); // resident_size
				stmt.setString(8, split[4]); // phys_footprint
				stmt.setString(9, split[5]); // user_time
				stmt.setString(10, split[6]); // system_time
				stmt.setString(11, split[7]); // start_time
				stmt.setInt(12, Integer.parseInt(split[8])); // parent pid

				// Build args
				String args = split[9];
				for (int i = 10; i < split.length; i ++)
				{
					args = args + " " + split[i];
				}
				stmt.setString(4, args); //args

				// Add insert
				stmt.addBatch();
			}

			stmt.executeBatch();
			stmt.close();
			c.commit();
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}

	public static void osquery_load_etc_hosts() throws SQLException
	{
		try
		{
			Connection c = getConnection();
			truncateTable(c, "_osquery_etc_hosts");

			// Prepare insert statement
			PreparedStatement stmt = c.prepareStatement("INSERT INTO _osquery_etc_hosts (address, hostnames) VALUES (?, ?)");

			// Open file and parse lines
			BufferedReader input = new BufferedReader(new FileReader(new File("/etc/hosts")));
			String line;

			while ((line = input.readLine()) != null)
			{
				if (line.length() <= 0)
				{
					// Empty line
					continue;
				}

				// Normalize separators
				line = line.replaceAll("^\\s+", "");
				line = line.replaceAll("\\s+", " ");

				if (line.startsWith("#"))
				{
					// Comment line
					continue;
				}

				// Split
				String[] split = line.split(" ");

				if (split.length < 2)
				{
					// Malformed line
					continue;
				}

				// Set address
				stmt.setString(1, split[0]);

				// Join hostnames
				String hostnames = split[1];
				for (int i = 2; i < split.length; i ++)
				{
					hostnames = hostnames + " " + split[i];
				}

				// Set hostnames
				stmt.setString(2, hostnames);
				stmt.addBatch();
			}

			stmt.executeBatch();
			stmt.close();
			c.commit();

		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
}
