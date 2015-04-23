package de.cultcraft.zero.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlDB
{
  private String url = null;
  private String user = null;
  private String password = null;
  private Connection con = null;

  public MySqlDB(String url, String user, String password) throws SQLException {
    this.url = url;
    this.user = user;
    this.password = password;
    initdb();
  }
  private void initdb() throws SQLException {
    this.con = DriverManager.getConnection(this.url, this.user, this.password);
  }

  public ResultSet executeRs(String query) {
    Statement st = null;
    ResultSet rs = null;
    try
    {
      st = this.con.createStatement();
      rs = st.executeQuery(query);
      rs.next();

      return rs;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  public void ExecuteStmt(String query) {
    try {
      Statement st = null;
      st = this.con.createStatement();
      st.execute(query);
      st.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public void closeConnection() {
    try { this.con.close();
    } catch (SQLException e)
    {
      e.printStackTrace();
    }
  }
}