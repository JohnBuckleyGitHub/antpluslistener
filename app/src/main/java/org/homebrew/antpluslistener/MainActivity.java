package org.homebrew.antpluslistener;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    BikeSpeedDistanceSampler instanceObj = new BikeSpeedDistanceSampler(this, this);
    TextView tv_status;
    TextView tv_state;
    TextView tv_code;
    TextView tv_timeStamp;
    TextView tv_eventFlags;
    TextView tv_timeStampLast;
    TextView tv_cumrev;
    TextView tv_cadTimeStamp;
    TextView tv_cadTimeStampLast;
    TextView tv_cadCumrev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_status = (TextView)findViewById(R.id.textView_Status);
        tv_state = (TextView)findViewById(R.id.textView_Status2);
        tv_code = (TextView)findViewById(R.id.textView_Status3);
        tv_timeStamp = (TextView)findViewById(R.id.textView_Status4);
        tv_eventFlags = (TextView)findViewById(R.id.textView_Status5);
        tv_timeStampLast = (TextView)findViewById(R.id.textView_Status6);
        tv_cumrev = (TextView)findViewById(R.id.textView_Status7);
        tv_cadTimeStamp = (TextView)findViewById(R.id.textView_Status8);
        tv_cadTimeStampLast = (TextView)findViewById(R.id.textView_Status9);
        tv_cadCumrev = (TextView)findViewById(R.id.textView_Status10);

        tv_status.setText("Name here");
    }

    /** Called when the user clicks the connect button */
    public void activateSearch(View view) {
        instanceObj.resetPcc();

    }

    public void closeThreads(View view) {
        instanceObj.closePcc();

    }

    /** Called when the user clicks the Send button */
    public void momentaryListen(View view) {
        tv_status.setText(instanceObj.resultsMap.get("spdDeviceName"));
        tv_state.setText(instanceObj.resultsMap.get("spdDeviceState"));
        tv_code.setText(instanceObj.resultsMap.get("spdResultCode"));
        tv_timeStamp.setText(String.valueOf(instanceObj.spdEstTimestamp));
        tv_eventFlags.setText(String.valueOf(instanceObj.spdEventFlags));
        tv_timeStampLast.setText(String.valueOf(instanceObj.spdTimestampOfLastEvent));
        tv_cumrev.setText(String.valueOf(instanceObj.spdCumulativeRevolutions));
        tv_cadTimeStamp.setText(String.valueOf(instanceObj.cadEstTimestamp));
        tv_cadTimeStampLast.setText(String.valueOf(instanceObj.cadTimestampOfLastEvent));
        tv_cadCumrev.setText(String.valueOf(instanceObj.cadCumulativeRevolutions));

    }

}
