package org.flopsar.ext.jdbc;

import oracle.jdbc.internal.OraclePreparedStatement;
import java.lang.reflect.Field;


/**
 * Instrument classes which implement oracle.jdbc.internal.OraclePreparedStatement
 * Instrument methods:
 * - boolean execute()
 * - int executeUpdate()
 * - java.sql.ResultSet executeQuery()
 */
public class JDBCOracle {

    private static final char PARAMETER_SEPARATOR = 0x1E;



    public static String psql(Object[] o){
        try {
            OraclePreparedStatement _this = (OraclePreparedStatement)o[0];

            return "URL"+PARAMETER_SEPARATOR+_this.getConnection().getMetaData().getURL()
                    +PARAMETER_SEPARATOR+"SQL"+PARAMETER_SEPARATOR+_this.getOriginalSql();
        }
        catch(Throwable ex){
            return "NA";
        }
    }




    public static String param_psql(Object[] o){
        try {

            OraclePreparedStatement _this = (OraclePreparedStatement)o[0];
            StringBuilder sb = new StringBuilder("URL");
            sb.append(PARAMETER_SEPARATOR);
            sb.append(_this.getConnection().getMetaData().getURL());
            sb.append(PARAMETER_SEPARATOR);
            sb.append("SQL");
            sb.append(PARAMETER_SEPARATOR);
            sb.append(_this.getOriginalSql());

            Class<?> c = _this.getClass();
            Field params = null;

            try {
                params = c.getDeclaredField("parameterString");
            }
            catch(Throwable t){
                c = c.getSuperclass();
            }

            try {
                params = c.getDeclaredField("parameterString");
            }
            catch(Throwable t){
                return sb.toString();
            }

            params.setAccessible(true);
            String[][] pa = (String[][])params.get(_this);

            if(pa != null){
                for(int i=0;i<pa.length;i++){
                    for(int j=0;j<pa[i].length; j++){
                        if(pa[i][j] == null)
                            continue;
                        sb.append(PARAMETER_SEPARATOR);
                        sb.append(String.format("P_%d",(j+1)));
                        sb.append(PARAMETER_SEPARATOR);
                        sb.append(String.format("%s",pa[i][j]));
                    }
                }
            }

            params = c.getDeclaredField("parameterInt");
            params.setAccessible(true);
            int[][] pi = (int[][])params.get(_this);

            if(pi != null){
                for(int i=0;i<pi.length;i++){
                    for(int j=0;j<pi[i].length; j++){
                        if(pi[i][j] == 0)
                            continue;
                        sb.append(PARAMETER_SEPARATOR);
                        sb.append(String.format("P_%d",(j+1)));
                        sb.append(PARAMETER_SEPARATOR);
                        sb.append(String.format("%d",pi[i][j]));
                    }
                }
            }

            return sb.toString();
        }
        catch(Throwable ex){
            return "EXCEPTION"+PARAMETER_SEPARATOR+ex.getMessage();
        }
    }

}
