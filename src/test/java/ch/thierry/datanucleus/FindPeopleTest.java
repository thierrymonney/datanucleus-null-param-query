package ch.thierry.datanucleus;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.hsqldb.jdbc.JDBCDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FindPeopleTest {

    private PersistenceManagerFactory pmf;

    @Rule
    public ExpectedException expected = none();

    @Before
    public void setUp() throws SQLException {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("datanucleus.autoCreateSchema", true);

        pmf = new JDOPersistenceManagerFactory(props);
        pmf.setConnectionDriverName(JDBCDriver.class.getName());
        pmf.setConnectionURL("jdbc:hsqldb:mem:testdb");
        pmf.setConnectionUserName("SA");
        pmf.setConnectionPassword("");

        save(new Person(1L, "John"));
        save(new Person(2L, "Jack"));
        save(new Person(3L, "Jane"));
    }

    @After
    public void tearDown() {
        pmf.close();
    }

    @Test
    public void findPeopleWithName() {
        //expected.expectCause(isA(SQLSyntaxErrorException.class));

        Collection<Person> johns = find("John");
        assertThat(johns, hasSize(1));
        assertThat(johns.iterator().next().getName(), is("John"));
    }

    @Test
    public void findPeopleWithNameByJDBC() throws SQLException {
        PersistenceManager pm = pmf.getPersistenceManager();
        Connection connection = (Connection) pm.getDataStoreConnection()
                .getNativeConnection();
        PreparedStatement stmt = null;
        try {
            stmt = connection
                    .prepareStatement("SELECT 'ch.thierry.datanucleus.Person' AS NUCLEUS_TYPE,A0.ID,A0.\"NAME\",A0.PERSON_ID FROM PERSON A0 WHERE (? IS NULL) OR (A0.\"NAME\" = ?)");
            stmt.setString(1, "John");
            stmt.setString(2, "John");
            ResultSet result = stmt.executeQuery();
            assertThat(result.next(), is(true));
            assertThat(result.getString("NAME"), is("John"));
        } finally {
            close(stmt);
            close(connection);
        }
    }

    private void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass()).error(
                        "Could not close JDBC statement", e);
            }
        }
    }

    private void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass()).error(
                        "Could not close JDBC connection", e);
            }
        }
    }

    private void save(Person person) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            pm.makePersistent(person);
        } finally {
            pm.close();
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Person> find(String name) {
        PersistenceManager pm = pmf.getPersistenceManager();
        try {
            Query query = pm.newQuery(Person.class);
            query.setFilter("pName == null || name == pName");
            query.declareParameters("java.lang.String pName");
            return (Collection<Person>) query.execute(name);
        } finally {
            pm.close();
        }
    }

}
