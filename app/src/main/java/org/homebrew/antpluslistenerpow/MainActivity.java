package org.homebrew.antpluslistenerpow;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    BikeSpeedDistanceSampler spdCadInstanceObj = new BikeSpeedDistanceSampler(this, this);
    BikePowerSampler powerInstanceObj = new BikePowerSampler(this, this);
    TextView tv_spdCadStatus;
    TextView tv_spdCadState;
    TextView tv_powerSource;
    TextView tv_torqueSource;
    TextView tv_powerStatus;
    TextView tv_powerState;
    TextView tv_torque;
    TextView tv_power;
//    TextView tv_timeStamp;
//    TextView tv_eventFlags;
    TextView tv_timeStampLast;
    TextView tv_cumrev;
    TextView tv_cadTimeStamp;
    TextView tv_cadTimeStampLast;
    TextView tv_cadCumrev;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable,1000);
        tv_spdCadStatus = (TextView)findViewById(R.id.textView_Status);
        tv_spdCadState = (TextView)findViewById(R.id.textView_Status2);
        tv_powerSource = (TextView)findViewById(R.id.textView_Status3);
        tv_powerStatus = (TextView)findViewById(R.id.textView_Status4);
        tv_powerState = (TextView)findViewById(R.id.textView_Status5);
        tv_power = (TextView)findViewById(R.id.textView_Status6);
//        tv_timeStampLast = (TextView)findViewById(R.id.textView_Status6);
        tv_torque = (TextView)findViewById(R.id.textView_Status7);
//        tv_cumrev = (TextView)findViewById(R.id.textView_Status7);
//        tv_cadTimeStamp = (TextView)findViewById(R.id.textView_Status8);
        tv_torqueSource = (TextView)findViewById(R.id.textView_Status9);
        tv_cadCumrev = (TextView)findViewById(R.id.textView_Status10);
//        tv_cadCumrev = (TextView)findViewById(R.id.textView_Status10);
        tv_spdCadStatus.setText("Name here");
    }

    @Override
    public void onDestroy()
    {
        closeThreads();
        super.onDestroy();
    }

    private void closeThreads(){
        spdCadInstanceObj.closePcc();
        powerInstanceObj.closePcc();
    }

    /** Called when the user clicks the connect button */
    public void activateSearch(View view) {
        spdCadInstanceObj.resetPcc2();
    }
    /** Called when the user clicks the connect button */
    public void activatePowerSearch(View view) {
        powerInstanceObj.resetPcc2();
    }
    public void closeButton(View view) {
        closeThreads();
    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()
        {
            displayResults();
            mHandler.postDelayed(m_Runnable, 100);
        }
    };//runnable


    /** Called when the user clicks the Send button */
    public void momentaryListen(View view) {
        displayResults();
    }

    public void displayResults(){
        tv_spdCadStatus.setText(spdCadInstanceObj.resultsMap.get("spdDeviceName"));
        tv_spdCadState.setText(spdCadInstanceObj.resultsMap.get("spdDeviceState"));
        tv_powerSource.setText(powerInstanceObj.resultsMap.get("calculatedPowerSource"));
        tv_powerStatus.setText(powerInstanceObj.resultsMap.get("powerDeviceName"));
        tv_powerState.setText(powerInstanceObj.resultsMap.get("powerDeviceState"));
        tv_torque.setText(String.valueOf(powerInstanceObj.calculatedTorque));
        tv_power.setText(String.valueOf(powerInstanceObj.calculatedPower));
//        tv_cumrev.setText(String.valueOf(spdCadInstanceObj.spdCumulativeRevolutions));
//        tv_cadTimeStamp.setText(String.valueOf(spdCadInstanceObj.cadEstTimestamp));
//        tv_torqueSource.setText(String.valueOf(spdCadInstanceObj.cadTimestampOfLastEvent));
        tv_torqueSource.setText(String.valueOf(spdCadInstanceObj.cadCumulativeRevolutions));
        tv_cadCumrev.setText(powerInstanceObj.resultsMap.get("calculatedTorqueSource"));
        Toast.makeText(this, powerInstanceObj.resultsMap.get("toastMessage"), Toast.LENGTH_SHORT);
    }
}
