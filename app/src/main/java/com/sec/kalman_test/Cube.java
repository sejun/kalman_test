package com.sec.kalman_test;

import static android.content.Context.SENSOR_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.widget.TextView;

import javax.microedition.khronos.opengles.GL10;

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

        float rawData[] = new float[3];
        rawData[0] = linear_acc_x;
        rawData[1] = linear_acc_y;
        rawData[2] = linear_acc_z;
        float[] accData = new float[3];
///filtered accelerometer data using kalman filter
        kalmanFilter(rawData, accData, P_xyz_mag, 0.2f, 1f);

        setAccX(accData[0]);
        setAccY(accData[1]);
        setAccZ(accData[2]);

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
