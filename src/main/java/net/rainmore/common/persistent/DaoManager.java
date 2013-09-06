package net.rainmore.common.persistent;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public class DaoManager {
    private final static Logger logger = LoggerFactory.getLogger(DaoManager.class);

    private DataSource dataSource;
    private Connection connection;

    private DaoManager() throws Exception {
        try {
            InitialContext ctx = new InitialContext();
            this.dataSource = (DataSource) ctx.lookup("jndi/MYSQL");
        } catch (Exception e) {
            throw e;
        }
    }

    public static DaoManager getInstance() {
        return DAOMessageSingleton.instance.get();
    }

    public void open() throws SQLException {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.connection = dataSource.getConnection();
            }
        } catch (SQLException e) {
            logger.error(String.format("Error %s: %s", e.getErrorCode(), e.getMessage()));
            throw e;
        }
    }

    public void close() throws SQLException{

        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            logger.error(String.format("Error %s: %s", e.getErrorCode(), e.getMessage()));
            throw e;
        }
    }

    @Override
    protected void finalize() {
        try {
            this.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                super.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private static class DAOMessageSingleton {
        public static final ThreadLocal<DaoManager> instance;

        static {
            ThreadLocal<DaoManager> daoManager = null;
            try {
                daoManager = new ThreadLocal<DaoManager>() {
                    protected DaoManager initDaoManager() {
                        try {
                            return new DaoManager();
                        } catch (Exception e) {
                            return null;
                        }
                    }
                };
            } catch (Exception e) {
                e.printStackTrace();
            }
            instance = daoManager;
        }
    }

}
