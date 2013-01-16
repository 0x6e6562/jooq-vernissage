package net.lshift.jooq;

import static net.lshift.jooq.schema.tables.SimpleTable.SIMPLE_TABLE;

import net.lshift.jooq.schema.tables.SimpleTable;
import net.lshift.jooq.schema.tables.daos.SimpleTableDao;
import net.lshift.jooq.schema.tables.records.SimpleTableRecord;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.jooq.*;
import org.jooq.impl.Factory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

public class JooqTest extends AbstractDatabaseTest {

  static Logger log = LoggerFactory.getLogger(JooqTest.class);

  @Test
  public void simpleExample() throws Exception {
    Connection connection = getConnection();
    Factory db = getFactory(connection);

    final DateTime startDate = new DateTime();
    final long startVersion = System.currentTimeMillis();

    int rows = 10;

    for (int i = 0; i < rows; i++) {
      db.insertInto(SIMPLE_TABLE).
          set(SIMPLE_TABLE.ID, RandomStringUtils.randomAlphanumeric(10)).
          set(SIMPLE_TABLE.VERSION, startVersion + i).
          set(SIMPLE_TABLE.ENTRY_DATE, new Date(startDate.getMillis())).
          execute();
    }

    // Run the same query twice to show the difference between the eager and lazy fetches

    Result<Record> hydratedResults = db.select().from(SIMPLE_TABLE).fetch();

    Cursor<Record> resultPointer = db.select().from(SIMPLE_TABLE).fetchLazy();

    // Manual way of binding results

    for (Record record : resultPointer) {
      Long fetchedVersion = record.getValue(SIMPLE_TABLE.VERSION);
    }

    // A little bit less manual way of binding results

    List<SimpleTableRecord> simpleTableRecords = db.fetch(SIMPLE_TABLE).into(SimpleTableRecord.class);

    // Even more enterprisey (but not quite enterprise ready (TM))

    SimpleTableDao dao = new SimpleTableDao(db);
    List<net.lshift.jooq.schema.tables.pojos.SimpleTable> pojos = dao.findAll();

    // JPA annotations are also possible, but let's look at some useful stuff instead .....

    // Simple WHERE clause

    final Date cutoff = new Date(startDate.plusWeeks(1).getMillis());

    db.select().from(SIMPLE_TABLE).where(SIMPLE_TABLE.ENTRY_DATE.lt(cutoff)).execute();

    // Add a second predicate to the where clause

    Condition c1 = SIMPLE_TABLE.ENTRY_DATE.lt(cutoff);
    Condition c2 = SIMPLE_TABLE.VERSION.eq(startVersion);

    db.select().from(SIMPLE_TABLE).where(c1, c2).execute();

    // Now do some aggregation

    SelectHavingStep average =
        db.select(SIMPLE_TABLE.VERSION.sum().as("Total"),
                SIMPLE_TABLE.VERSION.avg().as("Some Common Name"), // USe this to control what the result set column is named
                SIMPLE_TABLE.ENTRY_DATE).
           from(SIMPLE_TABLE).
           groupBy(SIMPLE_TABLE.ENTRY_DATE);

    average.execute();

    // Stack some query parts together in a union

    SelectHavingStep stdDev =
        db.select(SIMPLE_TABLE.VERSION.sum().as("Total"),
                  SIMPLE_TABLE.VERSION.stddevSamp().as("Some Common Name"), // USe this to control what the result set column is named
                  SIMPLE_TABLE.ENTRY_DATE).
           from(SIMPLE_TABLE).
           groupBy(SIMPLE_TABLE.ENTRY_DATE);

    Select<Record> union = average.union(stdDev);

    db.select(union.getFields()).from(union).execute();

    // Server side merge

    db.insertInto(SIMPLE_TABLE).
          set(SIMPLE_TABLE.ID, "possible duplicate primary key").
          set(SIMPLE_TABLE.VERSION, startVersion).
          set(SIMPLE_TABLE.ENTRY_DATE, new Date(startDate.getMillis())).
       onDuplicateKeyUpdate().
          set(SIMPLE_TABLE.VERSION, startVersion).
          set(SIMPLE_TABLE.ENTRY_DATE, new Date(startDate.getMillis())).
       execute();

    // JOOQ doesn't concern itself with commits or connection management -> you have to do this yourself

    connection.commit();
    connection.close();

  }
}
