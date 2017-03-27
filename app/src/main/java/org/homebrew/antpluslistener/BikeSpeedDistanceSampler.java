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
    public double convertValue = 0;
    public long spdEstTimestamp = 0;
    public double spdSysTimestamp = ((double)System.currentTimeMillis())/1000;
    public EnumSet<EventFlag> spdEventFlags = null;
    public double spdTimestampOfLastEvent = 0;
    public long spdCumulativeRevolutions = 0;
    public double cadSysTimestamp = ((double)System.currentTimeMillis())/1000;
    public long cadEstTimestamp = 0;
    public EnumSet<EventFlag> cadEventFlags = null;
    public double cadTimestampOfLastEvent = 0;
    public long cadCumulativeRevolutions = 0;

    android.app.Activity activity = new android.app.Activity();
    android.content.Context cont = new android.app.Activity();

    public BikeSpeedDistanceSampler(android.app.Activity activity_local, android.content.Context cont_local){
        activity = activity_local;
        cont = cont_local;

    }
    public IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> mResultReceiver = new IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc>()
    {
        // Handle the result, connecting to events on success or reporting
        // failure to user.
        @Override
        public void onResultReceived(AntPlusBikeSpeedDistancePcc result,
                                     RequestAccessResult resultCode, DeviceState initialDeviceState)
        {
            resultsMap.put("spdDeviceName", result.getDeviceName());
            resultsMap.put("spdDeviceState", initialDeviceState.name());
            String StrResultCode = resultCode.name();
            resultsMap.put("spdResultCode", StrResultCode);

            if (StrResultCode.equals("SUCCESS")) {
                bsdPcc = result;
                subscribeToEvents();
            }
        }

        private void subscribeToEvents()
        {
            bsdPcc.subscribeRawSpeedAndDistanceDataEvent(new IRawSpeedAndDistanceDataReceiver()
            {
                @Override
                public void onNewRawSpeedAndDistanceData(final long FestTimestamp,
                                                         final EnumSet<EventFlag> FeventFlags,
                                                         final BigDecimal FtimestampOfLastEvent, final long FcumulativeRevolutions) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            convertValue = (double) System.currentTimeMillis();
                            spdSysTimestamp = convertValue/1000;
//                            spdSysTimestamp = (System.currentTimeMillis()).doubleValue().longValue();
                            spdEstTimestamp = FestTimestamp;
                            spdEventFlags = FeventFlags;
                            spdTimestampOfLastEvent = FtimestampOfLastEvent.doubleValue();
                            spdCumulativeRevolutions = FcumulativeRevolutions;
                            resultsMap.put("spdSysTimestamp", String.valueOf(spdSysTimestamp));
                            resultsMap.put("spdEstTimestamp", String.valueOf(spdEstTimestamp));
                            resultsMap.put("spdEventFlags", spdEventFlags.toString());
                            resultsMap.put("spdTimestampOfLastEvent", String.valueOf(spdTimestampOfLastEvent));
                            resultsMap.put("spdCumulativeRevolutions", String.valueOf(spdCumulativeRevolutions));
                        }
                    });
                }
            });
            if (bsdPcc.isSpeedAndCadenceCombinedSensor())
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        bcReleaseHandle = AntPlusBikeCadencePcc.requestAccess(
                                activity,
                                bsdPcc.getAntDeviceNumber(), 0, true,
                                new IPluginAccessResultReceiver<AntPlusBikeCadencePcc>()
                                {
                                    // Handle the result, connecting to events
                                    // on success or reporting failure to user.
                                    @Override
                                    public void onResultReceived(AntPlusBikeCadencePcc result,
                                                                 RequestAccessResult resultCode,
                                                                 DeviceState initialDeviceStateCode)
                                    {
                                        String StrResultCode = resultCode.name();
                                        resultsMap.put("cadResultCode", StrResultCode);
                                        if (StrResultCode.equals("SUCCESS")) {
                                            bcPcc = result;
                                            bcPcc.subscribeRawCadenceDataEvent(new AntPlusBikeCadencePcc.IRawCadenceDataReceiver() {
                                                @Override
                                                public void onNewRawCadenceData(final long FestTimestamp,
                                                                                final EnumSet<EventFlag> FeventFlags, final BigDecimal FtimestampOfLastEvent,
                                                                                final long FcumulativeRevolutions) {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            convertValue = (double)(System.currentTimeMillis());
                                                            cadSysTimestamp = convertValue/1000;
//                                                            cadSysTimestamp = System.currentTimeMillis();
                                                            cadEstTimestamp = FestTimestamp;
                                                            cadEventFlags = FeventFlags;
                                                            cadTimestampOfLastEvent = FtimestampOfLastEvent.doubleValue();
                                                            cadCumulativeRevolutions = FcumulativeRevolutions;
                                                            resultsMap.put("cadSysTimestamp", String.valueOf(cadEstTimestamp));
                                                            resultsMap.put("cadEstTimestamp", String.valueOf(cadEstTimestamp));
                                                            resultsMap.put("cadEventFlags", cadEventFlags.toString());
                                                            resultsMap.put("cadTimestampOfLastEvent", String.valueOf(cadTimestampOfLastEvent));
                                                            resultsMap.put("cadCumulativeRevolutions", String.valueOf(cadCumulativeRevolutions));
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                },
                                // Receives state changes and shows it on the
                                // status display line
                                new IDeviceStateChangeReceiver()
                                {
                                    @Override
                                    public void onDeviceStateChange(final DeviceState newDeviceState)
                                    {
                                        activity.runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                if (newDeviceState != DeviceState.TRACKING)
                                                    resultsMap.put("cadDeviceState", newDeviceState.toString());
                                                if (newDeviceState == DeviceState.DEAD)
                                                    bcPcc = null;
                                            }
                                        });

                                    }
                                });
                    }
                                // Receives state changes and shows it on the
                                // status display line



                });
            }
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

    public void closePcc() {
        //Close all threads
        if (bsdPcc != null) {
            bsdPcc.releaseAccess();
        }
        if (bcPcc != null) {
            bcPcc.releaseAccess();
        }
        if (bsdReleaseHandle != null) {
            bsdReleaseHandle.close();
        }
        if (bcReleaseHandle != null) {
            bcReleaseHandle.close();
        }
    }

}




