package de.cultcraft.zero.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

abstract class Db
{
  public String sDriver = "";
  public String sUrl = null;
  public int iTimeout = 30;
  public Connection conn = null;
  public Statement statement = null;

  public Db()
  {
  }

  public Db(String sDriverToLoad, String sUrlToLoad)
    throws Exception
  {
    init(sDriverToLoad, sUrlToLoad);
  }

  public void init(String sDriverVar, String sUrlVar) throws Exception
  {
    setDriver(sDriverVar);
    setUrl(sUrlVar);
    setConnection();
    setStatement();
  }

  private void setDriver(String sDriverVar)
  {
    this.sDriver = sDriverVar;
  }

  private void setUrl(String sUrlVar)
  {
    this.sUrl = sUrlVar;
  }

  public void setConnection() throws Exception {
    Class.forName(this.sDriver);
    this.conn = DriverManager.getConnection(this.sUrl);
  }

  public Connection getConnection()
  {
    return this.conn;
  }

  public void setStatement() throws Exception {
    if (this.conn == null) {
      setConnection();
    }
    this.statement = this.conn.createStatement();
    this.statement.setQueryTimeout(this.iTimeout);
  }

  public Statement getStatement() {
    return this.statement;
  }

  public void executeStmt(String instruction) throws SQLException {
    this.statement.executeUpdate(instruction);
  }

  public void executeStmt(String[] instructionSet)
    throws SQLException
  {
    for (int i = 0; i < instructionSet.length; i++)
      executeStmt(instructionSet[i]);
  }

  public ResultSet executeQry(String instruction) throws SQLException
  {
    return this.statement.executeQuery(instruction);
  }
  public void closeConnection() {
    try {
      this.conn.close();
    }
    catch (Exception localException)
    {
    }
  }
}