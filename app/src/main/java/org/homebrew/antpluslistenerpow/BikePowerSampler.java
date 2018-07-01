package org.homebrew.antpluslistenerpow;

import android.util.Log;

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.AutoZeroStatus;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalculatedWheelDistanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalculatedWheelSpeedReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalibrationMessage;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.DataSource;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IAutoZeroStatusReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedCrankCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedPowerReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedTorqueReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalibrationMessageReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IInstantaneousCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IPedalPowerBalanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawPowerOnlyDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawWheelTorqueDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestStatus;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IBatteryStatusReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IManufacturerIdentificationReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IProductInformationReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AsyncScanController;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * Connects to Bike Power Plugin and display all the event data.
 */
public class BikePowerSampler {

    //NOTE: We're using 2.07m as the wheel circumference to pass to the calculated events
    BigDecimal wheelCircumferenceInMeters = new BigDecimal("1.0");

    AntPlusBikePowerPcc pwrPcc = null;
    PccReleaseHandle<AntPlusBikePowerPcc> releaseHandle = null;
    AsyncScanController mAsyncScanController = null;

    public HashMap<String, String> resultsMap = new HashMap<>();

    android.app.Activity activity = new android.app.Activity();
    android.content.Context cont = new android.app.Activity();

    public double convertValue = 0;
    public long powerEstTimestamp;
    public double powerSysTimestamp = ((double)System.currentTimeMillis())/1000;

    public BigDecimal calculatedPower;
    public BigDecimal calculatedTorque;
    public BigDecimal calculatedCrankCadence;
    public BigDecimal calculatedWheelSpeed;
    public BigDecimal calculatedWheelDistance;

    public int instantaneousCadence;
    public long pwrOnlyEventCount;
    public int instantaneousPower;
    public long accumulatedPower;
    public double pedalPowerPercentage;
//    public boolean rightPedalPowerIndicator;

    public long wheelTorqueEventCount;
    public long accumulatedWheelTicks;
    public BigDecimal accumulatedWheelPeriod;
    public BigDecimal accumulatedWheelTorque;

    public BikePowerSampler(android.app.Activity activity_local, android.content.Context cont_local){
    activity = activity_local;
    cont = cont_local;

}

    private static final String TAG = BikePowerSampler.class.getSimpleName();

    IPluginAccessResultReceiver<AntPlusBikePowerPcc> mResultReceiver = new IPluginAccessResultReceiver<AntPlusBikePowerPcc> ()
    {
        @Override
        public void onResultReceived(AntPlusBikePowerPcc result,
            RequestAccessResult resultCode, DeviceState initialDeviceState)
        {
            if (result != null) {
                resultsMap.put("powerDeviceName", result.getDeviceName());
                resultsMap.put("powerDeviceState", initialDeviceState.name());
                String StrResultCode = resultCode.name();
                resultsMap.put("powerResultCode", StrResultCode);
                Log.d(TAG, "On results received: resultCode.getIntValue" + resultCode.getIntValue());
                Log.d(TAG, "On results received: resultCode.toString" + resultCode.toString());
                Log.d(TAG, "On results received: result.deviceState" + result.getCurrentDeviceState());


                if (StrResultCode.equals("SUCCESS")) {
                    pwrPcc = result;
                    subscribeToEvents();
                    if (mAsyncScanController != null) {
                        mAsyncScanController.closeScanController();
                    }
                }
            }
        }};


    private void subscribeToEvents()
    {

        pwrPcc.subscribeCalculatedPowerEvent(new ICalculatedPowerReceiver()
        {
            @Override
            public void onNewCalculatedPower(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedPower)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        calculatedPower = FcalculatedPower;
                        resultsMap.put("calculatedPower", String.valueOf(calculatedPower)); // Value in Watts
                        String source;

                        // NOTE: The calculated power event will send an
                        // initial value code if it needed
                        // to calculate a NEW average. This is important if
                        // using the calculated power event to record user
                        // data, as an
                        // initial value indicates an average could not be
                        // guaranteed.
                        // The event prioritizes calculating with torque
                        // data over power only data.
                        switch (dataSource)
                        {
                            case POWER_ONLY_DATA:
                            case INITIAL_VALUE_POWER_ONLY_DATA:
                                // New data calculated from initial value data source
                            case WHEEL_TORQUE_DATA:
                            case INITIAL_VALUE_WHEEL_TORQUE_DATA:
                                // New data calculated from initial value data source
                            case CRANK_TORQUE_DATA:
                            case INITIAL_VALUE_CRANK_TORQUE_DATA:
                                // New data calculated from initial value data source
                            case CTF_DATA:
                            case INITIAL_VALUE_CTF_DATA:
//                                    source = dataSource.toString();
                                break;
                            case INVALID_CTF_CAL_REQ:
                                // The event cannot calculate power from CTF until a zero offset is collected from the sensor.
                            case COAST_OR_STOP_DETECTED:
                                //A coast or stop condition detected by the ANT+ Plugin.
                                //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
//                                    source = dataSource.toString();
                                break;
                            case UNRECOGNIZED:
                                resultsMap.put("toastMessage", "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
                            default:
                                calculatedPower = new BigDecimal("0.0");
                                source = "N/A";
                                break;
                        }
                        resultsMap.put("calculatedPowerSource", dataSource.toString());
                    }
                });
            }
        });

        pwrPcc.subscribeCalculatedTorqueEvent(new ICalculatedTorqueReceiver()
        {
            @Override
            public void onNewCalculatedTorque(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedTorque)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.d(TAG, "Calculated Torque Event");
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        calculatedTorque = FcalculatedTorque;
                        resultsMap.put("calculatedTorque", String.valueOf(calculatedTorque)); // Torque in Nm
                        String source;
                        // NOTE: The calculated torque event will send an initial value code if it needed
                        // to calculate a NEW average. This is important if using the calculated
                        // torque event to record user data, as an initial value indicates an average could
                        // not be guaranteed.
                        switch (dataSource)
                        {
                            case WHEEL_TORQUE_DATA:
                            case INITIAL_VALUE_WHEEL_TORQUE_DATA:
                                // New data calculated from initial value data source
                            case CRANK_TORQUE_DATA:
                            case INITIAL_VALUE_CRANK_TORQUE_DATA:
                                // New data calculated from initial value data source
                            case CTF_DATA:
                            case INITIAL_VALUE_CTF_DATA:
                                // New data calculated from initial value data source
//                                    source = dataSource.toString();
                                break;
                            case INVALID_CTF_CAL_REQ:
                                // The event cannot calculate torque from CTF until a zero offset is collected from the sensor.
                            case COAST_OR_STOP_DETECTED:
                                //A coast or stop condition detected by the ANT+ Plugin.
                                //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
//                                    source = dataSource.toString();
                                break;
                            case UNRECOGNIZED:
                                resultsMap.put("toastMessage", "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
                            default:
                                calculatedTorque = new BigDecimal("0.0");
//                                    textView_CalculatedTorque.setText("N/A");
//                                    source = "N/A";
                                break;
                        }
                        resultsMap.put("calculatedTorqueSource", dataSource.toString());
                    }
                });
            }
        });

        pwrPcc.subscribeCalculatedCrankCadenceEvent(new ICalculatedCrankCadenceReceiver()
        {
            @Override
            public void onNewCalculatedCrankCadence(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedCrankCadence)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.d(TAG, "Calculated Cadence Event");
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                        calculatedCrankCadence = FcalculatedCrankCadence;
                        resultsMap.put("calculatedCrankCadence", String.valueOf(calculatedCrankCadence));
//                            textView_CalculatedCrankCadence.setText(calculatedCrankCadence
//                                .toString() + "RPM");
                        String source;

                        // NOTE: The calculated crank cadence event
                        // will send an initial value code if it
                        // needed to calculate a NEW average.
                        // This is important if using the calculated
                        // crank cadence event to record user data,
                        // as an initial value indicates an average
                        // could not be guaranteed.
                        switch (dataSource)
                        {
                            case CRANK_TORQUE_DATA:
                            case INITIAL_VALUE_CRANK_TORQUE_DATA:
                                // New data calculated from initial
                                // value data source
                            case CTF_DATA:
                            case INITIAL_VALUE_CTF_DATA:
                                // New data calculated from initial
                                // value data source
                                source = dataSource.toString();
                                break;
                            case INVALID_CTF_CAL_REQ:
                                // The event cannot calculate
                                // cadence from CTF until a zero
                                // offset is collected from the
                                // sensor.
                            case COAST_OR_STOP_DETECTED:
                                //A coast or stop condition detected by the ANT+ Plugin.
                                //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
                                source = dataSource.toString();
                                break;
                            case UNRECOGNIZED:
                                resultsMap.put("toastMessage", "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
                            default:
                                calculatedCrankCadence = new BigDecimal("0.0");
                                source = "N/A";
                                break;
                        }
                        resultsMap.put("calculatedCrankCadenceSource", source);
                    }
                });
            }
        });

        pwrPcc.subscribeCalculatedWheelSpeedEvent(new CalculatedWheelSpeedReceiver(
                wheelCircumferenceInMeters)
        {
            @Override
            public void onNewCalculatedWheelSpeed(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedWheelSpeed)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        calculatedWheelSpeed = FcalculatedWheelSpeed;
                        resultsMap.put("calculatedWheelSpeed", String.valueOf(calculatedWheelSpeed));
                        String source;

                        // NOTE: The calculated speed event will
                        // send an initial value code if it needed
                        // to calculate a NEW average.
                        // This is important if using the calculated
                        // speed event to record user data, as an
                        // initial value indicates an average could
                        // not be guaranteed.
                        switch (dataSource)
                        {
                            case WHEEL_TORQUE_DATA:
                            case INITIAL_VALUE_WHEEL_TORQUE_DATA:
                                // New data calculated from initial
                                // value data source
                            case COAST_OR_STOP_DETECTED:
                                //A coast or stop condition detected by the ANT+ Plugin.
                                //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
                                source = dataSource.toString();
                                break;
                            case UNRECOGNIZED:
                                resultsMap.put("toastMessage", "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
                            default:
                                calculatedWheelSpeed = new BigDecimal("0.0");
                                source = "N/A";
                                break;
                        }
                        resultsMap.put("calculatedWheelSpeedSource", source);
                    }
                });
            }
        });

        pwrPcc.subscribeCalculatedWheelDistanceEvent(new CalculatedWheelDistanceReceiver(
                wheelCircumferenceInMeters)
        {
            @Override
            public void onNewCalculatedWheelDistance(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedWheelDistance)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        calculatedWheelDistance = FcalculatedWheelDistance;
                        resultsMap.put("calculatedWheelDistance", String.valueOf(calculatedWheelSpeed));
                        String source;

                        // NOTE: The calculated distance event will
                        // send an initial value code if it needed
                        // to calculate a NEW average.
                        // This is important if using the calculated
                        // distance event to record user data, as an
                        // initial value indicates an average could
                        // not be guaranteed.
                        switch (dataSource)
                        {
                            case WHEEL_TORQUE_DATA:
                                source = dataSource.toString();
                                break;
                            case INITIAL_VALUE_WHEEL_TORQUE_DATA:
                                // New data calculated from initial
                                // value data source
                            case COAST_OR_STOP_DETECTED:
                                //A coast or stop condition detected by the ANT+ Plugin.
                                //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
                                source = dataSource.toString();
                                break;
                            case UNRECOGNIZED:
                                resultsMap.put("toastMessage", "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
                            default:
                                calculatedWheelDistance = new BigDecimal("0.0");
                                source = "N/A";
                                break;
                        }
                        resultsMap.put("calculatedWheelDistanceSource", source);
                    }
                });
            }
        });

        pwrPcc.subscribeInstantaneousCadenceEvent(new IInstantaneousCadenceReceiver()
        {
            @Override
            public void onNewInstantaneousCadence(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final int FinstantaneousCadence)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        String source = "";
                        switch (dataSource)
                        {
                            case POWER_ONLY_DATA:
                            case WHEEL_TORQUE_DATA:
                            case CRANK_TORQUE_DATA:
                                source = " from Pg " + dataSource.getIntValue();
                                break;
                            case COAST_OR_STOP_DETECTED:
                                //A coast or stop condition detected by the ANT+ Plugin.
                                //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
                                source = dataSource.toString();
                                break;
                            case UNRECOGNIZED:
                                resultsMap.put("toastMessage", "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
                            default:
                                break;
                        }
                        // Check if the instantaneous cadence field is valid
                        if (FinstantaneousCadence >= 0)
                            instantaneousCadence = FinstantaneousCadence;
                        else
                            instantaneousCadence = 0;
                        resultsMap.put("instantaneousCadenceSource", source);
                        resultsMap.put("instantaneousCadence", String.valueOf(instantaneousCadence));
                    }
                });
            }
        });

        pwrPcc.subscribeRawPowerOnlyDataEvent(new IRawPowerOnlyDataReceiver()
        {
            @Override
            public void onNewRawPowerOnlyData(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long FpowerOnlyUpdateEventCount,
                    final int FinstantaneousPower,
                    final long FaccumulatedPower)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        pwrOnlyEventCount = FpowerOnlyUpdateEventCount;
                        instantaneousPower = FinstantaneousPower;
                        accumulatedPower = FaccumulatedPower;
                    }
                });
            }
        });

        pwrPcc.subscribePedalPowerBalanceEvent(new IPedalPowerBalanceReceiver()
        {
            @Override
            public void onNewPedalPowerBalance(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final boolean FrightPedalIndicator,
                    final int FpedalPowerPercentage)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        resultsMap.put("rightPedalIndicator", String.valueOf(FrightPedalIndicator));
                        pedalPowerPercentage = FpedalPowerPercentage;
                        resultsMap.put("pedalPowerPercentage", String.valueOf(FpedalPowerPercentage));
                    }
                });
            }
        });

        pwrPcc.subscribeRawWheelTorqueDataEvent(new IRawWheelTorqueDataReceiver()
        {
            @Override
            public void onNewRawWheelTorqueData(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long FwheelTorqueUpdateEventCount,
                    final long FaccumulatedWheelTicks,
                    final BigDecimal FaccumulatedWheelPeriod,
                    final BigDecimal FaccumulatedWheelTorque)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        wheelTorqueEventCount = FwheelTorqueUpdateEventCount;
                        resultsMap.put("wheelTorqueUpdateEventCount", String.valueOf(FwheelTorqueUpdateEventCount));
                        accumulatedWheelTicks = FaccumulatedWheelTicks;
                        resultsMap.put("accumulatedWheelTicks", String.valueOf(accumulatedWheelTicks));
                        accumulatedWheelPeriod = FaccumulatedWheelPeriod;
                        resultsMap.put("accumulatedWheelPeriod", String.valueOf(accumulatedWheelPeriod));
                        accumulatedWheelTorque = FaccumulatedWheelTorque;
                        resultsMap.put("accumulatedWheelTorque", String.valueOf(accumulatedWheelTorque));
                    }
                });
            }
        });


        pwrPcc.subscribeCalibrationMessageEvent(new ICalibrationMessageReceiver()
        {
            @Override
            public void onNewCalibrationMessage(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final CalibrationMessage calibrationMessage)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        resultsMap.put("calibrationId", String.valueOf(calibrationMessage));

                        switch (calibrationMessage.calibrationId)
                        {
                            case GENERAL_CALIBRATION_FAIL:
                            case GENERAL_CALIBRATION_SUCCESS:
                                resultsMap.put("ctfOffset", "N/A");
                                resultsMap.put("manufacturerSpecificBytes", "N/A");
                                resultsMap.put("calibrationData", String.valueOf(calibrationMessage.calibrationData));
                                break;
                            case CUSTOM_CALIBRATION_RESPONSE:
                            case CUSTOM_CALIBRATION_UPDATE_SUCCESS:
                                resultsMap.put("ctfOffset", "N/A");
                                resultsMap.put("calibrationData", "N/A");
                                String bytes = "";
                                for (byte manufacturerByte : calibrationMessage.manufacturerSpecificData)
                                    bytes += "[" + manufacturerByte + "]";
                                resultsMap.put("manufacturerSpecificBytes", String.valueOf(bytes));
                                break;
                            case CTF_ZERO_OFFSET:
                                resultsMap.put("manufacturerSpecificBytes", "N/A");
                                resultsMap.put("calibrationData", "N/A");
                                resultsMap.put("ctfOffset", String.valueOf(calibrationMessage.ctfOffset));
                                break;
                            case UNRECOGNIZED:
                                resultsMap.put("toastMessage", "Failed: UNRECOGNIZED. PluginLib Upgrade Required?");
                            default:
                                break;
                        }
                    }
                });
            }
        });

        pwrPcc.subscribeAutoZeroStatusEvent(new IAutoZeroStatusReceiver()
        {
            @Override
            public void onNewAutoZeroStatus(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final AutoZeroStatus autoZeroStatus)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        String autoZeroUiString;
                        switch (autoZeroStatus)
                        {
                            case NOT_SUPPORTED:
                            case ON:
                            case OFF:
                                autoZeroUiString = autoZeroStatus.toString();
                                break;
                            default:
                                autoZeroUiString = "N/A";
                                break;
                        }
                        resultsMap.put("autoZeroStatus", String.valueOf(autoZeroUiString));
                    }
                });
            }
        });

        pwrPcc
                .subscribeManufacturerIdentificationEvent(new IManufacturerIdentificationReceiver()
                {
                    @Override
                    public void onNewManufacturerIdentification(final long estTimestamp,
                                                                final EnumSet<EventFlag> eventFlags, final int hardwareRevision,
                                                                final int manufacturerID, final int modelNumber)
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                powerEstTimestamp = estTimestamp;
                                resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                                resultsMap.put("hardwareRevision", String.valueOf(hardwareRevision));
                                resultsMap.put("manufacturerID", String.valueOf(manufacturerID));
                                resultsMap.put("modelNumber", String.valueOf(modelNumber));
                            }
                        });
                    }
                });

        pwrPcc.subscribeProductInformationEvent(new IProductInformationReceiver()
        {
            @Override
            public void onNewProductInformation(final long estTimestamp,
                                                final EnumSet<EventFlag> eventFlags, final int mainSoftwareRevision,
                                                final int supplementalSoftwareRevision, final long serialNumber)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        powerEstTimestamp = estTimestamp;
                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                        resultsMap.put("mainSoftwareRevision", String.valueOf(mainSoftwareRevision));

                        if (supplementalSoftwareRevision == -2)
                            // Plugin Service installed does not support supplemental revision
                            resultsMap.put("supplementalSoftwareRevision", "?");
                        else if (supplementalSoftwareRevision == 0xFF)
                            // Invalid supplemental revision
                            resultsMap.put("supplementalSoftwareRevision", "");
                        else
                            // Valid supplemental revision
                            resultsMap.put("mainSoftwareRevision", String.valueOf(supplementalSoftwareRevision));

                        resultsMap.put("serialNumber", String.valueOf(serialNumber));
                    }
                });
            }
        });

        pwrPcc.subscribeBatteryStatusEvent(
                new IBatteryStatusReceiver()
                {
                    @Override
                    public void onNewBatteryStatus(final long estTimestamp,
                                                   EnumSet<EventFlag> eventFlags, final long cumulativeOperatingTime,
                                                   final BigDecimal batteryVoltage, final BatteryStatus batteryStatus,
                                                   final int cumulativeOperatingTimeResolution, final int numberOfBatteries,
                                                   final int batteryIdentifier)
                    {
                        activity.runOnUiThread(
                                new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        powerEstTimestamp = estTimestamp;
                                        resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                                        resultsMap.put("cumulativeOperatingTime", String.valueOf(cumulativeOperatingTime));
                                        resultsMap.put("batteryVoltage", String.valueOf(batteryVoltage));
                                        resultsMap.put("batteryStatus", String.valueOf(batteryStatus));
                                        resultsMap.put("cumulativeOperatingTimeResolution", String.valueOf(cumulativeOperatingTimeResolution));
                                        resultsMap.put("numberOfBatteries", String.valueOf(numberOfBatteries));
                                        resultsMap.put("batteryIdentifier", String.valueOf(batteryIdentifier));
                                    }
                                });
                    }
                });
    }
//    };




    IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new IDeviceStateChangeReceiver()
    {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState)
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    resultsMap.put("powerStatus", pwrPcc.getDeviceName() + ": " + newDeviceState);
//                    textView_status.setText(pwrPcc.getDeviceName() + ": " + newDeviceState);
                }
            });
        }
    };


    public void resetPcc()
    {
        if (releaseHandle != null) {
            releaseHandle.close();
        }
        releaseHandle = AntPlusBikePowerPcc.requestAccess(activity, cont, mResultReceiver,
                mDeviceStateChangeReceiver);
    }



    AsyncScanController.IAsyncScanResultReceiver scanResultReceiver = new AsyncScanController.IAsyncScanResultReceiver()
    {
        @Override
        public void onSearchStopped(RequestAccessResult resultCode){
            switch(resultCode){
                case SUCCESS:
                    Log.v(TAG, "USER_CANCELLED");
                case USER_CANCELLED:
                    Log.v(TAG, "USER_CANCELLED");
                case CHANNEL_NOT_AVAILABLE:
                    Log.v(TAG, "CHANNEL_NOT_AVAILABLE");
                case OTHER_FAILURE:
                    Log.v(TAG, "OTHER_FAILURE");
            }
        }

        public void onSearchResult(AsyncScanController.AsyncScanResultDeviceInfo deviceInfo){
            Log.v(TAG, deviceInfo.getDeviceDisplayName());
            Log.v(TAG, "Device number: " + deviceInfo.getAntDeviceNumber());
            releaseHandle = mAsyncScanController.requestDeviceAccess(deviceInfo, mResultReceiver, mDeviceStateChangeReceiver);
//                mAsyncScanController.closeScanController();
            }
    };

    public void resetPcc2()
    {
        if (releaseHandle != null) {
            releaseHandle.close();
        }
        mAsyncScanController = AntPlusBikePowerPcc.requestAsyncScanController(cont, 0,
                scanResultReceiver);
        Log.v(TAG, scanResultReceiver.toString());

    }

    final AntPlusCommonPcc.IRequestFinishedReceiver requestFinishedReceiver =
            new AntPlusCommonPcc.IRequestFinishedReceiver()
            {
                @Override
                public void onNewRequestFinished(final RequestStatus requestStatus)
                {
                    activity.runOnUiThread(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    switch(requestStatus)
                                    {
                                        case SUCCESS:
                                            resultsMap.put("toastMessage", "Request Successfully Sent");
                                            break;
                                        case FAIL_PLUGINS_SERVICE_VERSION:
                                            resultsMap.put("toastMessage", "Plugin Service Upgrade Required?");
                                            break;
                                        default:
                                            resultsMap.put("toastMessage", "Request Failed to be Sent");
                                            break;
                                    }
                                }
                            });
                }
            };

    public void closePcc() {
        //Close all threads
        if (pwrPcc != null) {
            pwrPcc.releaseAccess();
        }
        if (releaseHandle != null) {
            releaseHandle.close();
        }
        if (mAsyncScanController != null) {
            mAsyncScanController .closeScanController();
        }
        }

    public boolean requestManualCalibration() {
        // returns False if request could not be submitted
        return pwrPcc.requestManualCalibration(requestFinishedReceiver);
    }

    public boolean setAutoZero(boolean setter) {
        return pwrPcc.requestSetAutoZero(setter, requestFinishedReceiver);
    }
}




