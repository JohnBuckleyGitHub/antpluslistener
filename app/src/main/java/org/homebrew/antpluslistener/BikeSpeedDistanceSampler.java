package org.homebrew.antpluslistener;

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc.IRawSpeedAndDistanceDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * Connects to Bike Speed Plugin and display all the event data.
 */

public class BikeSpeedDistanceSampler {
    AntPlusBikeSpeedDistancePcc bsdPcc = null;
    PccReleaseHandle<AntPlusBikeSpeedDistancePcc> bsdReleaseHandle = null;
    AntPlusBikeCadencePcc bcPcc = null;
    PccReleaseHandle<AntPlusBikeCadencePcc> bcReleaseHandle = null;
    public HashMap<String, String> resultsMap = new HashMap<>();
    public long estTimestamp = 0;
    public EnumSet<EventFlag> eventFlags = null;
//    public String eventFlags = "";
    public double timestampOfLastEvent = 0;
    public long cumulativeRevolutions = 0;

    android.app.Activity activity = new android.app.Activity();
    android.content.Context cont = new android.app.Activity();

    public BikeSpeedDistanceSampler(android.app.Activity activity_local, android.content.Context cont_local){
        activity = activity_local;
        cont = cont_local;

    }
    public IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> mResultReceiver = new IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc>() {
        // Handle the result, connecting to events on success or reporting
        // failure to user.
        @Override
        public void onResultReceived(AntPlusBikeSpeedDistancePcc result,
                                     RequestAccessResult resultCode, DeviceState initialDeviceState) {
            resultsMap.put("deviceName", result.getDeviceName());
            resultsMap.put("deviceState", initialDeviceState.name());
            String StrResultCode = resultCode.name();
            resultsMap.put("resultCode", StrResultCode);

            if (StrResultCode.equals("SUCCESS")) {
                bsdPcc = result;
                subscribeToEvents();
            }
        }

        private void subscribeToEvents() {
            bsdPcc.subscribeRawSpeedAndDistanceDataEvent(new IRawSpeedAndDistanceDataReceiver() {
                @Override
                public void onNewRawSpeedAndDistanceData(final long FestTimestamp,
                                                         final EnumSet<EventFlag> FeventFlags,
                                                         final BigDecimal FtimestampOfLastEvent, final long FcumulativeRevolutions) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            estTimestamp = FestTimestamp;
                            eventFlags = FeventFlags;
                            timestampOfLastEvent = FtimestampOfLastEvent.doubleValue();
                            cumulativeRevolutions = FcumulativeRevolutions;
                            resultsMap.put("estTimestamp", String.valueOf(estTimestamp));
                            resultsMap.put("eventFlags", eventFlags.toString());
                            resultsMap.put("timestampOfLastEvent", String.valueOf(timestampOfLastEvent));
                            resultsMap.put("cumulativeRevolutions", String.valueOf(cumulativeRevolutions));
                        }
                    });
                }
            });


        }
    };
    IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new AntPluginPcc.IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //                    tv_status.setText(bsdPcc.getDeviceName() + ": " + newDeviceState);
                    if (newDeviceState == DeviceState.DEAD)
                        bsdPcc = null;
                }
            });
        }
    };

    public void resetPcc() {
        //Release the old access if it exists
        if (bsdReleaseHandle != null) {
            bsdReleaseHandle.close();
        }
        if (bcReleaseHandle != null) {
            bcReleaseHandle.close();
        }

        bsdReleaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(activity, cont,
                mResultReceiver, mDeviceStateChangeReceiver);
    }

}




