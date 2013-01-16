package net.lshift.jooq;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import org.jooq.SQLDialect;
import org.jooq.impl.Factory;
import org.junit.Before;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDatabaseTest {

  protected DataSource dataSource;
  protected SQLDialect dialect;

  @Before
  public void setUp() {
    dialect = getDialect();
    dataSource = getDataSource();
  }

  protected DataSource getDataSource() {
    BoneCPDataSource ds = new BoneCPDataSource();
    ds.setDriverClass(System.getProperty("jdbcDriverClass", "org.hsqldb.jdbc.JDBCDriver"));
    ds.setJdbcUrl(System.getProperty("jdbcUrl", "jdbc:hsqldb:target/temp;hsqldb.lock_file=false"));
    ds.setUsername(System.getProperty("jdbcUser", "SA"));
    ds.setPassword(System.getProperty("jdbcPass", ""));
    return ds;
  }

  protected SQLDialect getDialect() {
    return SQLDialect.valueOf("HSQLDB");
  }

  protected Factory getFactory(Connection c) {
    return new Factory(c, dialect);
  }

  protected void closeConnection(Connection connection) {
    closeConnection(connection, false);
  }

  protected void closeConnection(Connection connection, boolean shouldCommit) {
    try {
      if (shouldCommit) {
        connection.commit();
      }
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected Connection getConnection() {
    Connection c;

    try {
      c = dataSource.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return c;
  }
}
