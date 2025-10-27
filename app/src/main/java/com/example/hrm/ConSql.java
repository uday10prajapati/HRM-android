package com.example.hrm;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConSql {
    Connection con;

    @SuppressLint("NewApi")
    public Connection conclass() {
        String ip = "172.1.2.0", port = "1433", db = "HRMS", user = "sa", password = "Uday14";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String connectURL = null;

        try {
            // Load jTDS driver
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            connectURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databaseName=" + db + ";user=" + user + ";password=" + password + ";";

            con = DriverManager.getConnection(connectURL);

        } catch (Exception e) {
            Log.e("Error :", "Connection failed", e);
        }

        return con;
    }
}
