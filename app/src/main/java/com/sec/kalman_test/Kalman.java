package com.sec.kalman_test;

public class Kalman {
    private float Q_angle;
    private float Q_bias;
    private float R_measure;

    private float angle;
    private float bias;
    private float rate;
    private float [][] P;

    public Kalman() {
        Q_angle = 0.001f;
        Q_bias = 0.003f;
        R_measure = 0.03f;

        angle = 0.0f; // Reset the angle
        bias = 0.0f; // Reset bias

        P = new float[2][2];
        P[0][0] = 0.0f;
        P[0][1] = 0.0f;
        P[1][0] = 0.0f;
        P[1][1] = 0.0f;
    }

    public float getAngle(float newAngle, float newRate, float dt) {
        // KasBot V2  -  Kalman filter module - http://www.x-firm.com/?page_id=145
        // Modified by Kristian Lauszus
        // See my blog post for more information: http://blog.tkjelectronics.dk/2012/09/a-practical-approach-to-kalman-filter-and-how-to-implement-it

        // Discrete Kalman filter time update equations - Time Update ("Predict")
        // Update xhat - Project the state ahead
        /* Step 1 */
        rate = newRate - bias;
        angle += dt * rate;

        // Update estimation error covariance - Project the error covariance ahead
        /* Step 2 */
        P[0][0] += dt * (dt*P[1][1] - P[0][1] - P[1][0] + Q_angle);
        P[0][1] -= dt * P[1][1];
        P[1][0] -= dt * P[1][1];
        P[1][1] += Q_bias * dt;

        // Discrete Kalman filter measurement update equations - Measurement Update ("Correct")
        // Calculate Kalman gain - Compute the Kalman gain
        /* Step 4 */
        float S = P[0][0] + R_measure; // Estimate error
        /* Step 5 */
        //float K[2]; // Kalman gain - This is a 2x1 vector
        float [] K;
        K = new float[2];

        K[0] = P[0][0] / S;
        K[1] = P[1][0] / S;

        // Calculate angle and bias - Update estimate with measurement zk (newAngle)
        /* Step 3 */
        float y = newAngle - angle; // Angle difference
        /* Step 6 */
        angle += K[0] * y;
        bias += K[1] * y;

        // Calculate estimation error covariance - Update the error covariance
        /* Step 7 */
        float P00_temp = P[0][0];
        float P01_temp = P[0][1];

        P[0][0] -= K[0] * P00_temp;
        P[0][1] -= K[0] * P01_temp;
        P[1][0] -= K[1] * P00_temp;
        P[1][1] -= K[1] * P01_temp;

        return angle;
    };

    public void setAngle(float _angle) {
        angle = _angle;
    }

    public float getRate() {
        return rate;
    }

    public void setQangle(float _Q_angle) {
        Q_angle = _Q_angle;
    }

    public void setQbias(float _Q_bias) {
        Q_bias = _Q_bias;
    }

    public void setRmeasure(float _R_measure) {
        R_measure = _R_measure;
    }

    public float getQangle() {
        return Q_angle;
    }

    public float getQbias() {
        return Q_bias;
    }

    public float getRmeasure() {
        return R_measure;
    }

}
