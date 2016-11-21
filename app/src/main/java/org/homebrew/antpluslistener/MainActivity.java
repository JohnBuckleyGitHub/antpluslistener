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
    }

    /** Called when the user clicks the connect button */
    public void activateSearch(View view) {
        instanceObj.resetPcc();

    }


    /** Called when the user clicks the Send button */
    public void momentaryListen(View view) {
        tv_status.setText(instanceObj.resultsMap.get("deviceName"));
        tv_state.setText(instanceObj.resultsMap.get("deviceState"));
        tv_code.setText(instanceObj.resultsMap.get("resultCode"));
        tv_timeStamp.setText(String.valueOf(instanceObj.estTimestamp));
        tv_eventFlags.setText(String.valueOf(instanceObj.eventFlags));
        tv_timeStampLast.setText(String.valueOf(instanceObj.timestampOfLastEvent));
        tv_cumrev.setText(String.valueOf(instanceObj.cumulativeRevolutions));
    }

}
