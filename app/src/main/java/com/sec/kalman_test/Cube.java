package com.sec.kalman_test;

import static android.content.Context.SENSOR_SERVICE;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.util.Log;
import android.widget.TextView;

import javax.microedition.khronos.opengles.GL10;
import com.sec.kalman_test.Kalman;

public class Cube extends ShapeBase implements SensorEventListener  {

    private float accX;
    private float accY;
    private float accZ;
    private Context mContext;


    private TextView xText, yText, zText;
    private Sensor mySensor, magnetoMeter, gyroScope;
    private SensorManager SM;
    private float[] rawAccData = new float[3];
    private float[] rawAccFilteredData = new float[3];
    private float[] P_xyz_acc=new float[3]; // for Kalman filter
    /**short array that represents raw magnetometer data from sensor*/
    private float[] rawMagData = new float[3];
    private float[] rawMagFilteredData = new float[3];
    private float[] P_xyz_mag = new float[3]; // for Kalman filter
    private float[] calibratedMagData = new float[3];


    private Kalman mKalmanX;
    private Kalman mKalmanY;
    private float gyroXangle;
    private float gyroYangle;
    private float compAngleX;
    private float compAngleY;
    private float kalAngleX;
    private float kalAngleY;
    private long timer;
    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);


    //private SensorManager sensorManager;

    // private Sensor accelerometer;
    private Sensor head;
    // private Sensor gyro;
    float linear_acc_x = 0;
    float linear_acc_y = 0;
    float linear_acc_z = 0;

    float heading = 0;

    float gyro_x = 0;
    float gyro_y = 0;
    float gyro_z = 0;

    public float getAccX() {
        return accX;
    }
    public float getAccY() {
        return accY;
    }
    public float getAccZ() {
        return accZ;
    }
    public void setAccX(float x) {
        accX = x;
    }
    public void setAccY(float y) {
        accY = y;
    }
    public void setAccZ(float z) {
        accY = z;
    }

    public Cube(Context context) {
        this(context, 2.0f, 2.0f, 2.0f);
        //this.context = getApplication;



        //Assign text View
        //xText = (TextView)findViewById(R.id.xText);
       // yText = (TextView)findViewById(R.id.yText);
        //zText = (TextView) findViewById(R.id.zText);
    }

    public Cube(Context context, float width, float height, float depth) {
        this.mContext = context;
        SM  = (SensorManager)mContext.getSystemService(SENSOR_SERVICE);

        //Accelerometer sensor
        mySensor  = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // magnetoMeter = SM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroScope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        head = SM.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //register Sensor Listener

        SM.registerListener(this,mySensor,SensorManager.SENSOR_DELAY_NORMAL);
        //SM.registerListener(this,magnetoMeter,SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this,gyroScope,SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this,head,SensorManager.SENSOR_DELAY_NORMAL);

        mKalmanX = new Kalman();
        mKalmanY = new Kalman();

        width  /= 2;
        height /= 2;
        depth  /= 2;

        float vertex[] = {
                -width, -height, -depth, // 0
                width, -height, -depth, // 1
                width,  height, -depth, // 2
                -width,  height, -depth, // 3
                -width, -height,  depth, // 4
                width, -height,  depth, // 5
                width,  height,  depth, // 6
                -width,  height,  depth, // 7
        };


        byte index[] = {
                0, 1, 2, 2, 3, 0, // back face
                4, 5, 6, 6, 7, 4, // front face
                0, 3, 7, 7, 4, 0, // left face
                1, 2, 6, 6, 5, 1, // right face
                0, 4, 5, 5, 1, 0, // bottom face
                3, 7, 6, 6, 2, 3, // up face
        };

        float color [] = {
                1.0f,  0.0f,  0.0f,  1.0f,
                0.0f,  1.0f,  0.0f,  1.0f,
                0.0f,  0.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  0.0f,  1.0f,
                0.0f,  1.0f,  0.0f,  1.0f,
                0.0f,  0.0f,  1.0f,  1.0f,
                1.0f,  0.0f,  0.0f,  1.0f,
                0.0f,  1.0f,  1.0f,  1.0f
        };

        setIndices(index);
        setVertices(vertex);
        setColors(color);

        // Set kalman and gyro starting angle
        accX = 0;
        accY = 0;
        accZ = 0;

        //float roll = (float) (atan2(accY, accZ) * RAD_TO_DEG);
        //float pitch = (float) (atan(-accX/ sqrt(accY * accY + accZ * accZ)) * RAD_TO_DEG);
        float roll = (float) (atan(linear_acc_y/ sqrt(linear_acc_x * linear_acc_x + linear_acc_z * linear_acc_z)) * RAD_TO_DEG);
        float pitch = (float) (atan2(-linear_acc_x, linear_acc_z) * RAD_TO_DEG);

        mKalmanX.setAngle(roll);
        mKalmanY.setAngle(pitch);
        gyroXangle = roll;
        gyroYangle = pitch;
        compAngleX = roll;
        compAngleY = pitch;

        timer = System.currentTimeMillis();
    }



    @Override
    void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CCW);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

        gl.glRotatef(mRotateAngle, 1, 0, 0);
        gl.glRotatef(mRotateAngle, 0, 1, 0);

        //Draw the vertices as triangles, based on the Index Buffer information
        gl.glDrawElements(GL10.GL_TRIANGLES, mNumOfIndex, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);

        mRotateAngle += 0.5f;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            linear_acc_x = event.values[0];
            linear_acc_y = event.values[1];
            linear_acc_z = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyro_x = event.values[0];
            gyro_y = event.values[1];
            gyro_z = event.values[2];
        }

        Log.i("kalman", "aX = " + linear_acc_x + ", aY = " + linear_acc_y);
        Log.i("kalman", "gX = " + gyro_x + ", gY = " + gyro_y);

        float rawData[] = new float[3];
        rawData[0] = linear_acc_x;
        rawData[1] = linear_acc_y;
        rawData[2] = linear_acc_z;
        float[] accData = new float[3];
        ///filtered accelerometer data using kalman filter
/*
        kalmanFilter(rawData, accData, P_xyz_mag, 0.2f, 1f);

        setAccX(accData[0]);
        setAccY(accData[1]);
        setAccZ(accData[2]);
*/


        long dt = (System.currentTimeMillis() - timer);
        timer = System.currentTimeMillis();

        //kalAngleX = mKalmanX.getAngle(linear_acc_x, gyro_z, dt);
        //kalAngleY = mKalmanY.getAngle(linear_acc_y, gyro_z, dt);


        //float roll  = (float) (atan2(linear_acc_y, linear_acc_z) * RAD_TO_DEG);
        //float pitch = (float) (atan(-linear_acc_x / sqrt(linear_acc_y * linear_acc_x + linear_acc_z * linear_acc_z)) * RAD_TO_DEG);
        float roll = (float) (atan(linear_acc_y/ sqrt(linear_acc_x * linear_acc_x + linear_acc_z * linear_acc_z)) * RAD_TO_DEG);
        float pitch = (float) (atan2(-linear_acc_x, linear_acc_z) * RAD_TO_DEG);

        float gyroXrate = (float) (gyro_x / 131.0);
        float gyroYrate = (float) (gyro_y / 131.0);

        if ((pitch < -90 && kalAngleY > 90) || (pitch > 90 && kalAngleY < -90)) {
            mKalmanY.setAngle(pitch);
            compAngleY = pitch;
            kalAngleY = pitch;
            gyroYangle = pitch;
        } else {
            kalAngleY = mKalmanY.getAngle(pitch, gyroYrate, dt);
        }

        if (abs(kalAngleY) > 90)
            gyroXrate = -gyroXrate;
        kalAngleX = mKalmanY.getAngle(roll, gyroXrate, dt);

        gyroXangle += gyroXrate * dt;
        gyroYrate += gyroYrate * dt;

        compAngleX = (float) (0.93 * (compAngleX + gyroXrate * dt) + 0.07 * roll);
        compAngleY = (float) (0.93 * (compAngleY + gyroYrate * dt) + 0.07 * pitch);

        // Reset the gyro angle when it has drifted too much
        if (gyroXangle < -180 || gyroXangle > 180)
            gyroXangle = kalAngleX;
        if (gyroYangle < -180 || gyroYangle > 180)
            gyroYangle = kalAngleY;

        //Log.i("kalman", "cX = " + compAngleX + ", cY = " + compAngleY);
        //Log.i("kalman", "kX = " + kalAngleX + ", kY = " + kalAngleY);
        Log.i("kalman", "roll = " + roll + ", pitch = " + pitch);

        setAccX(kalAngleX);
        setAccY(kalAngleY);
        //setAccZ(accData[2]);
        Log.i("kalman", "x = " +  kalAngleX + ", y = " + kalAngleY + " dt = " + dt);




        /*   if(isOrientationUp) {
            accData[0] = (float) ((accData[2])/(sqrt(pow(accData[0],2)+pow(accData[1],2)+pow(accData[2],2))));
        } else{
            accData[0]=(float) ((accData[2])/(sqrt(pow(accData[0],2)+pow(accData[1],2)+pow(accData[2],2))));
        }*/
        //xText.setText("X =" + linear_acc_x + "\nE(X) =" + accData[0]);//+"gx =: " + gyro_x+"heading = "+ heading);
        //yText.setText("Y =" + linear_acc_y + "\nE(Y) =" + accData[1]);
        //zText.setText("Z =" + linear_acc_z + "\nE(Z) =" + accData[2]);// + "gz= " + gyro_z);


    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /*
     * Method performs Kalman filtering on specified data
     * @param float array of length 3 representing 3 axis sensor data
     * @param outputFilteredData in this array output result will be stored. Previous value is used to compute current value
     * @param P_xyz probabilities array of length array which is changed after each iteration
     * @param Q Kalman filter Q parameter
     * @param Delta Kalman filter delta parameter
     */
    public static void kalmanFilter(float[] inpuRawData, float[] outputFilteredData, float[] P_xyz, float Q, float delta){
        float Pn;
        float K;
        for (int i = 0; i < 3; i++) {
            Pn = P_xyz[i] + Q;
            K = Pn / (Pn + delta);
            outputFilteredData[i] = outputFilteredData[i] + K * (inpuRawData[i] - outputFilteredData[i]);
            P_xyz[i] = (1 - K) * Pn;
        }
    }
}
