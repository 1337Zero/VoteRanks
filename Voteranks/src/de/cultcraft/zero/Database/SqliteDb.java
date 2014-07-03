package de.cultcraft.zero.Database;

public class SqliteDb extends Db
{
String sDriverForClass = "org.sqlite.JDBC"; 
public SqliteDb(String sUrlKey) throws Exception{ 
init(sDriverForClass, sUrlKey);
}
}