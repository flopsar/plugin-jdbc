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
                params = c.getDeclaredField("currentRowBinders");
            }
            catch(Throwable t){
                c = c.getSuperclass();
            }
            try {
                params = c.getDeclaredField("currentRowBinders");
            }
            catch(Throwable t){
                sb.append(PARAMETER_SEPARATOR);
                sb.append("ERROR");
                sb.append(PARAMETER_SEPARATOR);
                sb.append(t.getMessage());
                return sb.toString();
            }

            params.setAccessible(true);
            Class currentRowBindersClass = params.getClass();
            Object[] currentRowBinders = (Object[])params.get(_this);

            if (currentRowBinders != null){
                for(int i=0;i<currentRowBinders.length;i++){
                    Object binder = currentRowBinders[i];
                    if(binder == null)
                        continue;

                    sb.append(PARAMETER_SEPARATOR);
                    sb.append(String.format("P_%d",(i+1)));
                    sb.append(PARAMETER_SEPARATOR);

                    Class<?> binderClass = binder.getClass();
                    try {
                        getValue(sb, binder, binderClass);
                        continue;
                    } catch(Throwable tx) {
                        binderClass = binderClass.getSuperclass();
                    }
                    try {
                        getValue(sb, binder, binderClass);
                    } catch (Throwable x) {
                        sb.append("No paramVal: "+ x.getMessage());
                    }
                }
            }
            return sb.toString();
        }
        catch(Throwable ex){
            return "EXCEPTION"+PARAMETER_SEPARATOR+ex.getMessage();
        }
    }

    private static void getValue(StringBuilder sb, Object binder, Class binderClass) throws Exception {
        Field paramVal = binderClass.getDeclaredField("paramVal");
        paramVal.setAccessible(true);
        Object paramValObj = paramVal.get(binder);
        sb.append(paramValObj);
    }

}
