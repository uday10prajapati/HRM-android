package com.example.hrm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationUpdateService";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // --- Filtering constants ---
    private static final long UPDATE_INTERVAL = 1 * 60 * 1000; // 1 minute
    private static final long FASTEST_INTERVAL = 30 * 1000;    // 30 seconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_CHANGE_FOR_UPDATES = 1 * 60 * 1000; // 1 minute

    private static final String CHANNEL_ID = "LocationUpdateServiceChannel";

    // **NEW**: Constants for SharedPreferences persistence
    private static final String SERVICE_PREFS = "LocationServicePrefs";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_TOKEN = "USER_TOKEN";

    private String userId, userToken;
    private final List<LocationPoint> locationPoints = new ArrayList<>();
    private Location lastLocation;
    private long lastLocationTimeMillis;

    private Handler uploadHandler;
    private Runnable periodicUploadTask;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();

        // **NEW**: Restore credentials when service is created
        restoreCredentials();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();
                if (currentLocation != null) {
                    if (shouldStoreLocation(currentLocation)) {
                        lastLocation = currentLocation;
                        lastLocationTimeMillis = System.currentTimeMillis();
                        
                        LocationPoint point = new LocationPoint(
                                userId,
                                currentLocation.getLatitude(),
                                currentLocation.getLongitude()
                        );
                        locationPoints.add(point);
                        Log.d(TAG, "New location added: " + point.getLatitude() + ", " + point.getLongitude());

                        if (locationPoints.size() >= 5) {
                            Log.d(TAG, "Auto-uploading 5 collected points...");
                            uploadPointsSafely();
                        }
                    } else {
                        Log.d(TAG, "Skipping save. Not enough time passed or distance moved.");
                    }
                }
            }
        };

        uploadHandler = new Handler(Looper.getMainLooper());
        periodicUploadTask = () -> {
            if (!locationPoints.isEmpty()) {
                Log.d(TAG, "Periodic upload triggered with " + locationPoints.size() + " points");
                uploadPointsSafely();
            }
            uploadHandler.postDelayed(periodicUploadTask, 10 * 60 * 1000);
        };
        uploadHandler.postDelayed(periodicUploadTask, 10 * 60 * 1000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("USER_ID")) {
            // **NEW**: Persist credentials every time the service is explicitly started
            userId = intent.getStringExtra("USER_ID");
            userToken = intent.getStringExtra("USER_TOKEN");
            persistCredentials(userId, userToken);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("HRM App")
                .setContentText("Live location tracking is active.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1, notification);
        startLocationUpdates();

        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted, stopping service");
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private boolean shouldStoreLocation(Location location) {
        if (lastLocation == null) {
            return true; 
        }
        float distance = location.distanceTo(lastLocation);
        boolean movedEnough = distance >= MIN_DISTANCE_CHANGE_FOR_UPDATES;
        long timeSinceLastSave = System.currentTimeMillis() - lastLocationTimeMillis;
        boolean timePassedEnough = timeSinceLastSave >= MIN_TIME_CHANGE_FOR_UPDATES;
        Log.d(TAG, "Distance: " + distance + "m, Time since last save: " + (timeSinceLastSave / 1000) + "s");
        return movedEnough || timePassedEnough;
    }

    private void uploadPointsSafely() {
        if (userId == null || userToken == null || locationPoints.isEmpty()) {
            Log.w(TAG, "No userId/token or no points to upload. Token: " + userToken);
            return;
        }
        List<LocationPoint> uploadList = new ArrayList<>(locationPoints);
        LocationUploader.uploadLocationData(
                getApplicationContext(),
                uploadList,
                userId,
                userToken
        );
        locationPoints.removeAll(uploadList);
    }

    // **NEW**: Methods to save and load credentials
    private void persistCredentials(String userId, String userToken) {
        SharedPreferences prefs = getSharedPreferences(SERVICE_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TOKEN, userToken);
        editor.apply();
        Log.d(TAG, "User credentials persisted for service.");
    }

    private void restoreCredentials() {
        SharedPreferences prefs = getSharedPreferences(SERVICE_PREFS, MODE_PRIVATE);
        userId = prefs.getString(KEY_USER_ID, null);
        userToken = prefs.getString(KEY_USER_TOKEN, null);
        if(userId != null) {
            Log.d(TAG, "User credentials restored for service.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        uploadHandler.removeCallbacks(periodicUploadTask);
        Log.d(TAG, "Service destroyed, uploading remaining " + locationPoints.size() + " points");
        uploadPointsSafely();

        // **NEW**: Clear service-specific credentials on final destruction
        getSharedPreferences(SERVICE_PREFS, MODE_PRIVATE).edit().clear().apply();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Live Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
