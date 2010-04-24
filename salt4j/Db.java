package salt4j;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/** You must use a database that supports transactions: e.g MySQL innodb, sqlite, Postgresql, JavaDB. */
public class Db {
    Connection connection;

    long lastActiveMillis = System.currentTimeMillis();
    public void updateLastActive() {lastActiveMillis = System.currentTimeMillis();}

    final LinkedHashMap<String, PreparedStatement> statements;

    final String url; //for reconnecting
    public Db(String url) throws SQLException {
        this.url = url;
        statements = new LinkedHashMap<String, PreparedStatement>(16, 0.75f, true) {
            protected boolean removeEldsestEntry(Entry<String, PreparedStatement> eldest) {
                try {
                    if (size() <= 16) return false;
                    else { eldest.getValue().close(); return true; }
                } catch (SQLException e) { throw new RuntimeException(e.getMessage(), e); }
            }
        };
    }

    Connection connectTo(String url) throws SQLException {
        final Connection newConnection = DriverManager.getConnection(url);
        newConnection.setAutoCommit(false);
        newConnection.setTransactionIsolation(newConnection.TRANSACTION_SERIALIZABLE);
        return newConnection;
    }

    /** Close the connection and all associated PreparedStatement objects. */
    void close() throws SQLException {
        try {connection.close();} catch (Exception e) {}
        for (PreparedStatement s: statements.values()) s.close();
        statements.clear();
        connection = null;
    }

    boolean dirty = false;

    /**
     * Pings the connection if it's been inactive for 15 seconds or more.
     * If it's no longer valid, creates a new connection after disposing this one.
     */
    public void begin() throws SQLException {
        if (connection == null) connection = connectTo(url);
        else if (System.currentTimeMillis() - lastActiveMillis > 15000) {
            if (connection.isValid(0)) updateLastActive(); 
            else { close(); connection = connectTo(url); }
        }
    }

    public void commit() throws SQLException {
        try { if (dirty) connection.commit(); }
        finally { dirty = false; updateLastActive(); }
    }

    public void rollback() throws SQLException {
        try { if (dirty) connection.rollback(); }
        finally { dirty = false; updateLastActive(); }
    }

    PreparedStatement getPreparedStatement(String query) throws SQLException {
        PreparedStatement p = statements.get(query);
        if (p == null) {
            p = connection.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY,
                    ResultSet.CLOSE_CURSORS_AT_COMMIT);
            statements.put(query, p);
        }
        return p;
    }

    static void setParams(PreparedStatement p, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) p.setObject(i+1, params[i]);
    }

    static void setParams(PreparedStatement p, List params) throws SQLException {
        int l = params.size();
        for (int i = 0; i < l; i++) p.setObject(i+1, params.get(i));
    }

    public ResultSet query(String sql, Object... params) throws SQLException {
        PreparedStatement p = getPreparedStatement(sql);
        setParams(p, params); dirty = true; updateLastActive();
        return p.executeQuery();
    }

    public ResultSet query(String sql, List params) throws SQLException {
        PreparedStatement p = getPreparedStatement(sql);
        setParams(p, params); dirty = true; updateLastActive();
        return p.executeQuery();
    }
    
    public int exec(String sql, Object... params) throws SQLException {
        PreparedStatement p = getPreparedStatement(sql);
        setParams(p, params); dirty = true; updateLastActive();
        return p.executeUpdate();
    }

    public int exec(String sql, List params) throws SQLException {
        PreparedStatement p = getPreparedStatement(sql);
        setParams(p, params); dirty = true; updateLastActive();
        return p.executeUpdate();
    }

    private Lazy<ResultSet> lazyQuery(final PreparedStatement p) {
        return new Lazy<ResultSet>() {
            ResultSet compute() throws SQLException {
                dirty = true; updateLastActive();
                p.executeQuery(); return p.getResultSet();
            }
        };
    }
    public Lazy<ResultSet> lazyQuery(String sql, Object... params) throws SQLException {
        final PreparedStatement p = getPreparedStatement(sql);
        setParams(p, params);
        return lazyQuery(p);
    }

    public Lazy<ResultSet> lazyQuery(String sql, List<Object> params) throws SQLException {
        final PreparedStatement p = getPreparedStatement(sql);
        setParams(p, params);
        return lazyQuery(p);
    }
     
    public static class Pool {
        private final ArrayList<Db> connections;

        public Pool(String jdbcUrl, int size) throws SQLException {
            connections = new ArrayList(size);
            for (int i = 0; i<size; i++) connections.add(new Db(jdbcUrl));
        }

        public static Pool mysql(String host, int port, String user, String password, String db)
        throws Exception {
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db +"?autoReconnect=true" +
                               "&zeroDateTimeBehavior=convertToNull" +
                               "&tinyInt1isBit=false" +
                               "&user=" + user +
                               "&password=" + password;
            System.out.println(url);
            int size = Runtime.getRuntime().availableProcessors() * 2;
            return new Pool(url, size);
        }

        /** Retrieve a database connection from the pool and begin a new transaction. */
        public synchronized Db take() throws SQLException {
            while(connections.size() == 0) {
                try { this.wait(); }
                catch (InterruptedException e) {}
            }
            Db db = connections.remove(connections.size() - 1); //LIFO
            db.begin();
            return db;
        }

        public Lazy<Db> lazyTake() {
            return new Lazy<Db>() {
                Db compute() throws Exception { return take(); }
            };
        }

        /** Return the database connection to pool and commit it if necessary. */
        public synchronized void putBack (Db db) throws SQLException {
            db.rollback(); // a no-op if the transaction was committed
            connections.add(db); if (connections.size() <= 1) this.notify();
        }

        /** Return the database connection if and only if one was taken. */
        public void putBack (Lazy<Db> db) throws SQLException {
            if (db.computed()) putBack(db.get());
        }
    }
    static Charset UTF8 = Charset.forName("UTF-8");
    
    /*
     * Usage: Db.getUTF(rs, column) instead of rs.getString(column);
     */
    public static String getUTF8(java.sql.ResultSet rs, String fieldName) throws java.sql.SQLException {
        return new String(rs.getBytes(fieldName), UTF8);
    }

    public static String getUTF8(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        return new String(rs.getBytes(i), UTF8);
    }

    /** 
     * db.exec(query, setUTF("name")) instead of db.exec(query, "name")
     */
    public static byte[] setUTF8(String string) { return string.getBytes(UTF8); }
}