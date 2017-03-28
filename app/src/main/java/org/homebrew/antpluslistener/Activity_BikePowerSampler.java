/*
This software is subject to the license described in the License.txt file
included with this software distribution. You may not use this file except in compliance
with this license.

Copyright (c) Dynastream Innovations Inc. 2013
All rights reserved.
*/

package org.homebrew.antpluslistener;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.lang.String;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.antplus.pluginsampler.multidevicesearch.Activity_MultiDeviceSearchSampler;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.AutoZeroStatus;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalculatedWheelDistanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalculatedWheelSpeedReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CalibrationMessage;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CrankLengthSetting;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.CrankParameters;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.DataSource;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IAutoZeroStatusReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedCrankCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedPowerReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalculatedTorqueReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICalibrationMessageReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ICrankParametersReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IInstantaneousCadenceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IPedalPowerBalanceReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IPedalSmoothnessReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawCrankTorqueDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawCtfDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawPowerOnlyDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.IRawWheelTorqueDataReceiver;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikePowerPcc.ITorqueEffectivenessReceiver;
import com.dsi.ant.plugins.antplus.pcc.defines.BatteryStatus;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestStatus;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IDeviceStateChangeReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc.IPluginAccessResultReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IBatteryStatusReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IManufacturerIdentificationReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IProductInformationReceiver;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc.IRequestFinishedReceiver;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

/**
 * Connects to Bike Power Plugin and display all the event data.
 */
public class Activity_BikePowerSampler extends Activity
{
    //NOTE: We're using 2.07m as the wheel circumference to pass to the calculated events
    BigDecimal wheelCircumferenceInMeters = new BigDecimal("1.0");

    AntPlusBikePowerPcc pwrPcc = null;
    PccReleaseHandle<AntPlusBikePowerPcc> releaseHandle = null;

    public HashMap<String, String> resultsMap = new HashMap<>();

    android.app.Activity activity = new android.app.Activity();
    android.content.Context cont = new android.app.Activity();

//    TextView textView_status;
    public double convertValue = 0;
    public long powerEstTimestamp;
    public double powerSysTimestamp = ((double)System.currentTimeMillis())/1000;

    public BigDecimal calculatedPower;
//    TextView textView_CalculatedPowerSource;

    public BigDecimal  calculatedTorque;
//    TextView textView_CalculatedTorqueSource;

    public BigDecimal calculatedCrankCadence;
//    TextView textView_CalculatedCrankCadenceSource;

    public BigDecimal calculatedWheelSpeed;
    public BigDecimal calculatedWheelDistance;
//    TextView textView_CalculatedSpeedSource;
//    TextView textView_CalculatedDistanceSource;

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

    public long crankTorqueEventCount;
    public long accumulatedCrankTicks;
    public double accumulatedCrankPeriod;
    public double accumulatedCrankTorque;

    public long tePsEventCount;
    public double leftTorqueEff;
    public double rightTorqueEff;
    public double separatePedalSmoothnessSupport;
    public double leftCombSmoothness;
    public double rightSmoothness;

    public double CtfEventCount;
    public double InstantaneousSlope;
    public double AccumulatedTimestamp;
    public double AccumulatedTorqueTicks;

//    public String CalibrationId;
//    public double CalibrationData;
//    public double CtfOffset;
//    public double ManufacturerSpecificBytes;

//    public double AutoZeroStatus;

//    public double FullCrankLength;
//    public double CrankLengthStatus;
//    public double SensorSoftwareMismatchStatus;
//    public double SensorAvailabilityStatus;
//    public double CustomCalibrationStatus;
//    public double AutoCrankLengthSupport;

//    public double manufacturerID;
//    public double serialNumber;
//    public double modelNumber;

//    public double hardwareRevision;
//    public double mainSoftwareRevision;
//    public double supplementalSoftwareRevision;

//    public double CumulativeOperatingTime;
//    public double BatteryVoltage;
//    public double BatteryStatus;
//    public double CumulativeOperatingTimeResolution;
//    public double NumberOfBatteries;
//    public double BatteryIdentifier;

//    Button button_requestManualCalibration;
//    Button button_setAutoZero;
//    Button button_requestCrankParameters;
//    Button button_setCrankParameters;
//    Button button_requestCustomCalibrationParameters;
//    Button button_setCustomCalibrationParameters;
//    Button button_setCtfSlope;
//    Button button_commandBurst;

    IPluginAccessResultReceiver<AntPlusBikePowerPcc> mResultReceiver = new IPluginAccessResultReceiver<AntPlusBikePowerPcc>()
    {
        @Override
        public void onResultReceived(AntPlusBikePowerPcc result,
            RequestAccessResult resultCode, DeviceState initialDeviceState)
        {
            resultsMap.put("powerDeviceName", result.getDeviceName());
            resultsMap.put("powerDeviceState", initialDeviceState.name());
            String StrResultCode = resultCode.name();
            resultsMap.put("powerResultCode", StrResultCode);

            if (StrResultCode.equals("SUCCESS")) {
                pwrPcc = result;
                subscribeToEvents();
            }
        }

        private void subscribeToEvents()
        {
//            button_requestManualCalibration.setEnabled(true);
//            button_setAutoZero.setEnabled(true);
//            button_requestCrankParameters.setEnabled(true);
//            button_setCrankParameters.setEnabled(true);
//            button_requestCustomCalibrationParameters.setEnabled(true);
//            button_setCustomCalibrationParameters.setEnabled(true);
//            button_setCtfSlope.setEnabled(true);
//            button_commandBurst.setEnabled(true);

            pwrPcc.subscribeCalculatedPowerEvent(new ICalculatedPowerReceiver()
            {
                @Override
                public void onNewCalculatedPower(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedPower)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            calculatedPower = FcalculatedPower;
                            resultsMap.put("calculatedPower", String.valueOf(calculatedPower));
//                            textView_CalculatedPower.setText(calculatedPower.toString()
//                                + "W");
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
                                    // New data calculated from initial
                                    // value data source
                                case WHEEL_TORQUE_DATA:
                                case INITIAL_VALUE_WHEEL_TORQUE_DATA:
                                    // New data calculated from initial
                                    // value data source
                                case CRANK_TORQUE_DATA:
                                case INITIAL_VALUE_CRANK_TORQUE_DATA:
                                    // New data calculated from initial
                                    // value data source
                                case CTF_DATA:
                                case INITIAL_VALUE_CTF_DATA:
                                    source = dataSource.toString();
                                    break;
                                case INVALID_CTF_CAL_REQ:
                                    // The event cannot calculate power
                                    // from CTF until a zero offset is
                                    // collected from the sensor.
                                case COAST_OR_STOP_DETECTED:
                                    //A coast or stop condition detected by the ANT+ Plugin.
                                    //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                    //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
                                    source = dataSource.toString();
                                    break;
                                case UNRECOGNIZED:
//                                    Toast.makeText(Activity_BikePowerSampler.this,
//                                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
//                                        Toast.LENGTH_SHORT).show();
                                default:
                                    calculatedPower = new BigDecimal("0.0");
//                                    textView_CalculatedPower.setText("N/A");
                                    source = "N/A";
                                    break;
                            }
                            resultsMap.put("calculatedPowerSource", source);
//                            textView_CalculatedPowerSource.setText(source);
                        }
                    });
                }
            });

            pwrPcc.subscribeCalculatedTorqueEvent(new ICalculatedTorqueReceiver()
            {
                @Override
                public void onNewCalculatedTorque(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedTorque)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            calculatedTorque = FcalculatedTorque;
                            resultsMap.put("calculatedTorque", String.valueOf(calculatedTorque));
//                            textView_CalculatedTorque.setText(calculatedTorque.toString()
//                                + "Nm");
                            String source;

                            // NOTE: The calculated torque event will
                            // send an initial value code if it needed
                            // to calculate a NEW average.
                            // This is important if using the calculated
                            // torque event to record user data, as an
                            // initial value indicates an average could
                            // not be guaranteed.
                            switch (dataSource)
                            {
                                case WHEEL_TORQUE_DATA:
                                case INITIAL_VALUE_WHEEL_TORQUE_DATA:
                                    // New data calculated from initial
                                    // value data source
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
                                    // The event cannot calculate torque
                                    // from CTF until a zero offset is
                                    // collected from the sensor.
                                case COAST_OR_STOP_DETECTED:
                                    //A coast or stop condition detected by the ANT+ Plugin.
                                    //This is automatically sent by the plugin after 3 seconds of unchanging events.
                                    //NOTE: This value should be ignored by apps which are archiving the data for accuracy.
                                    source = dataSource.toString();
                                    break;
                                case UNRECOGNIZED:
//                                    Toast.makeText(Activity_BikePowerSampler.this,
//                                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
//                                        Toast.LENGTH_SHORT).show();
                                default:
                                    calculatedTorque = new BigDecimal("0.0");
//                                    textView_CalculatedTorque.setText("N/A");
                                    source = "N/A";
                                    break;
                            }
                            resultsMap.put("calculatedTorqueSource", source);
//                            textView_CalculatedTorqueSource.setText(source);
                        }
                    });
                }
            });

            pwrPcc.subscribeCalculatedCrankCadenceEvent(new ICalculatedCrankCadenceReceiver()
            {
                @Override
                public void onNewCalculatedCrankCadence(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedCrankCadence)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
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
                                    Toast.makeText(Activity_BikePowerSampler.this,
                                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                                        Toast.LENGTH_SHORT).show();
                                default:
                                    calculatedCrankCadence = new BigDecimal("0.0");
//                                    textView_CalculatedCrankCadence.setText("N/A");
                                    source = "N/A";
                                    break;
                            }
                            resultsMap.put("calculatedCrankCadenceSource", source);
//                            textView_CalculatedCrankCadenceSource.setText(source);
                        }
                    });
                }
            });

            pwrPcc.subscribeCalculatedWheelSpeedEvent(new CalculatedWheelSpeedReceiver(
                wheelCircumferenceInMeters)
            {
                @Override
                public void onNewCalculatedWheelSpeed(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedWheelSpeed)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            calculatedWheelSpeed = FcalculatedWheelSpeed;
                            resultsMap.put("calculatedWheelSpeed", String.valueOf(calculatedWheelSpeed));
//                            textView_CalculatedSpeed.setText(calculatedWheelSpeed
//                                .toString() + "km/h");
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
                                    Toast.makeText(Activity_BikePowerSampler.this,
                                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                                        Toast.LENGTH_SHORT).show();
                                default:
                                    calculatedWheelSpeed = new BigDecimal("0.0");
//                                    textView_CalculatedSpeed.setText("N/A");
                                    source = "N/A";
                                    break;
                            }
                            resultsMap.put("calculatedWheelSpeedSource", source);
//                            textView_CalculatedSpeedSource.setText(source);
                        }
                    });
                }
            });

            pwrPcc.subscribeCalculatedWheelDistanceEvent(new CalculatedWheelDistanceReceiver(
                wheelCircumferenceInMeters)
            {
                @Override
                public void onNewCalculatedWheelDistance(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final BigDecimal FcalculatedWheelDistance)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            calculatedWheelDistance = FcalculatedWheelDistance;
                            resultsMap.put("calculatedWheelDistance", String.valueOf(calculatedWheelSpeed));
//                            textView_CalculatedDistance.setText(calculatedWheelDistance
//                                .toString() + "m");
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
                                    Toast.makeText(Activity_BikePowerSampler.this,
                                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                                        Toast.LENGTH_SHORT).show();
                                default:
                                    calculatedWheelDistance = new BigDecimal("0.0");
//                                    textView_CalculatedDistance.setText("N/A");
                                    source = "N/A";
                                    break;
                            }
                            resultsMap.put("calculatedWheelDistanceSource", source);
//                            textView_CalculatedDistanceSource.setText(source);
                        }
                    });
                }
            });

            pwrPcc.subscribeInstantaneousCadenceEvent(new IInstantaneousCadenceReceiver()
            {
                @Override
                public void onNewInstantaneousCadence(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final DataSource dataSource,
                    final int FinstantaneousCadence)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));


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
//                                    Toast.makeText(Activity_BikePowerSampler.this,
//                                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
//                                        Toast.LENGTH_SHORT).show();
                                default:
                                    break;
                            }

                            // Check if the instantaneous cadence field
                            // is valid
                            if (FinstantaneousCadence >= 0)
                                instantaneousCadence = FinstantaneousCadence;
//                                textView_instantaneousCadence.setText(String
//                                    .valueOf(instantaneousCadence) + "RPM" + source);
                            else
                                instantaneousCadence = 0;
//                                textView_instantaneousCadence.setText("--");
                            resultsMap.put("instantaneousCadence", String.valueOf(instantaneousCadence));
                        }
                    });
                }
            });

            pwrPcc.subscribeRawPowerOnlyDataEvent(new IRawPowerOnlyDataReceiver()
            {
                @Override
                public void onNewRawPowerOnlyData(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long FpowerOnlyUpdateEventCount,
                    final int FinstantaneousPower,
                    final long FaccumulatedPower)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            pwrOnlyEventCount = FpowerOnlyUpdateEventCount;
//                            textView_PwrOnlyEventCount.setText(String
//                                .valueOf(powerOnlyUpdateEventCount));
                            instantaneousPower = FinstantaneousPower;
//                            textView_InstantaneousPower.setText(String
//                                .valueOf(instantaneousPower) + "W");
                            accumulatedPower = FaccumulatedPower;
//                            textView_AccumulatedPower.setText(String
//                                .valueOf(accumulatedPower) + "W");
                        }
                    });
                }
            });

            pwrPcc.subscribePedalPowerBalanceEvent(new IPedalPowerBalanceReceiver()
            {
                @Override
                public void onNewPedalPowerBalance(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final boolean FrightPedalIndicator,
                    final int FpedalPowerPercentage)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            resultsMap.put("rightPedalIndicator", String.valueOf(FrightPedalIndicator));
//                            textView_PedalPowerPercentage.setText(String
//                                .valueOf(pedalPowerPercentage) + "%");
                            pedalPowerPercentage = FpedalPowerPercentage;
                            resultsMap.put("pedalPowerPercentage", String.valueOf(FpedalPowerPercentage);
//                            textView_RightPedalPowerIndicator.setText(String
//                                .valueOf(rightPedalIndicator));
                        }
                    });
                }
            });

            pwrPcc.subscribeRawWheelTorqueDataEvent(new IRawWheelTorqueDataReceiver()
            {
                @Override
                public void onNewRawWheelTorqueData(
                    final long FestTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long FwheelTorqueUpdateEventCount,
                    final long FaccumulatedWheelTicks,
                    final BigDecimal FaccumulatedWheelPeriod,
                    final BigDecimal FaccumulatedWheelTorque)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            powerEstTimestamp = FestTimestamp;
                            resultsMap.put("powerEstTimestamp", String.valueOf(powerEstTimestamp));
                            wheelTorqueEventCount = FwheelTorqueUpdateEventCount;
                            resultsMap.put("wheelTorqueUpdateEventCount", String.valueOf(FwheelTorqueUpdateEventCount));
                            accumulatedWheelTicks = FaccumulatedWheelTicks;
                            resultsMap.put("accumulatedWheelTicks", String.valueOf(accumulatedWheelTicks));
//                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            accumulatedWheelPeriod = FaccumulatedWheelPeriod;
                            resultsMap.put("accumulatedWheelPeriod", String.valueOf(accumulatedWheelPeriod));
                            accumulatedWheelTorque = FaccumulatedWheelTorque;
                            resultsMap.put("accumulatedWheelTorque", String.valueOf(accumulatedWheelTorque));
//                            textView_WheelTorqueEventCount.setText(String
//                                .valueOf(wheelTorqueUpdateEventCount));
//                            textView_AccumulatedWheelTicks.setText(String
//                                .valueOf(accumulatedWheelTicks) + " rotations");
//                            textView_AccumulatedWheelPeriod.setText(accumulatedWheelPeriod
//                                .toString() + "s");
//                            textView_AccumulatedWheelTorque.setText(accumulatedWheelTorque
//                                .toString() + "Nm");
                        }
                    });
                }
            });

            pwrPcc.subscribeRawCrankTorqueDataEvent(new IRawCrankTorqueDataReceiver()
            {
                @Override
                public void onNewRawCrankTorqueData(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long crankTorqueUpdateEventCount,
                    final long accumulatedCrankTicks,
                    final BigDecimal accumulatedCrankPeriod,
                    final BigDecimal accumulatedCrankTorque)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

                            textView_CrankTorqueEventCount.setText(String
                                .valueOf(crankTorqueUpdateEventCount));
                            textView_AccumulatedCrankTicks.setText(String
                                .valueOf(accumulatedCrankTicks) + " rotations");
                            textView_AccumulatedCrankPeriod.setText(accumulatedCrankPeriod
                                .toString() + "s");
                            textView_AccumulatedCrankTorque.setText(accumulatedCrankTorque
                                .toString() + "Nm");
                        }
                    });
                }
            });

            pwrPcc.subscribeTorqueEffectivenessEvent(new ITorqueEffectivenessReceiver()
            {
                @Override
                public void onNewTorqueEffectiveness(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long powerOnlyUpdateEventCount,
                    final BigDecimal leftTorqueEffectiveness,
                    final BigDecimal rightTorqueEffectiveness)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

                            textView_TePsEventCount.setText(String
                                .valueOf(powerOnlyUpdateEventCount));

                            if (leftTorqueEffectiveness.intValue() != -1)
                                textView_LeftTorqueEff.setText(leftTorqueEffectiveness
                                    .toString() + "%");
                            else
                                textView_LeftTorqueEff.setText("N/A");

                            if (rightTorqueEffectiveness.intValue() != -1)
                                textView_RightTorqueEff.setText(rightTorqueEffectiveness
                                    .toString() + "%");
                            else
                                textView_RightTorqueEff.setText("N/A");
                        }
                    });
                }

            });

            pwrPcc.subscribePedalSmoothnessEvent(new IPedalSmoothnessReceiver()
            {
                @Override
                public void onNewPedalSmoothness(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final long powerOnlyUpdateEventCount,
                    final boolean separatePedalSmoothnessSupport,
                    final BigDecimal leftOrCombinedPedalSmoothness,
                    final BigDecimal rightPedalSmoothness)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

                            textView_TePsEventCount.setText(String
                                .valueOf(powerOnlyUpdateEventCount));

                            textView_SeparatePedalSmoothnessSupport.setText(String
                                .valueOf(separatePedalSmoothnessSupport));

                            if (leftOrCombinedPedalSmoothness.intValue() != -1)
                                textView_LeftCombSmoothness
                                    .setText(leftOrCombinedPedalSmoothness.toString()
                                        + "%");
                            else
                                textView_LeftCombSmoothness.setText("N/A");

                            if (rightPedalSmoothness.intValue() != -1)
                                textView_RightSmoothness.setText(rightPedalSmoothness
                                    .toString() + "%");
                            else
                                textView_RightSmoothness.setText("N/A");
                        }
                    });
                }
            });

            pwrPcc.subscribeRawCtfDataEvent(new IRawCtfDataReceiver()
            {
                @Override
                public void onNewRawCtfData(final long estTimestamp,
                    final EnumSet<EventFlag> eventFlags,
                    final long ctfUpdateEventCount,
                    final BigDecimal instantaneousSlope,
                    final BigDecimal accumulatedTimeStamp,
                    final long accumulatedTorqueTicksStamp)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

                            textView_CtfEventCount.setText(String
                                .valueOf(ctfUpdateEventCount));
                            textView_InstantaneousSlope.setText(instantaneousSlope
                                .toString() + "Nm/Hz");
                            textView_AccumulatedTimestamp.setText(accumulatedTimeStamp
                                .toString() + "s");
                            textView_AccumulatedTorqueTicks.setText(String
                                .valueOf(accumulatedTorqueTicksStamp));
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
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            textView_CalibrationId.setText(calibrationMessage.calibrationId
                                .toString());

                            switch (calibrationMessage.calibrationId)
                            {
                                case GENERAL_CALIBRATION_FAIL:
                                case GENERAL_CALIBRATION_SUCCESS:
                                    textView_CtfOffset.setText("N/A");
                                    textView_ManufacturerSpecificBytes.setText("N/A");
                                    textView_CalibrationData
                                        .setText(calibrationMessage.calibrationData
                                            .toString());
                                    break;

                                case CUSTOM_CALIBRATION_RESPONSE:
                                case CUSTOM_CALIBRATION_UPDATE_SUCCESS:
                                    textView_CalibrationData.setText("N/A");
                                    textView_CtfOffset.setText("N/A");

                                    String bytes = "";
                                    for (byte manufacturerByte : calibrationMessage.manufacturerSpecificData)
                                        bytes += "[" + manufacturerByte + "]";

                                    textView_ManufacturerSpecificBytes.setText(bytes);
                                    break;

                                case CTF_ZERO_OFFSET:
                                    textView_ManufacturerSpecificBytes.setText("N/A");
                                    textView_CalibrationData.setText("N/A");
                                    textView_CtfOffset.setText(calibrationMessage.ctfOffset
                                        .toString());
                                    break;
                                case UNRECOGNIZED:
                                    Toast.makeText(Activity_BikePowerSampler.this,
                                        "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                                        Toast.LENGTH_SHORT).show();
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
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

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

                            textView_AutoZeroStatus.setText(autoZeroUiString);
                        }
                    });
                }
            });

            pwrPcc.subscribeCrankParametersEvent(new ICrankParametersReceiver()
            {
                @Override
                public void onNewCrankParameters(
                    final long estTimestamp, final EnumSet<EventFlag> eventFlags,
                    final CrankParameters crankParameters)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));
                            textView_CrankLengthStatus.setText(crankParameters
                                .getCrankLengthStatus().toString());
                            textView_SensorSoftwareMismatchStatus.setText(crankParameters
                                .getSensorSoftwareMismatchStatus().toString());
                            textView_SensorAvailabilityStatus.setText(crankParameters
                                .getSensorAvailabilityStatus().toString());
                            textView_CustomCalibrationStatus.setText(crankParameters
                                .getCustomCalibrationStatus().toString());
                            textView_AutoCrankLengthSupport.setText(String
                                .valueOf(crankParameters.isAutoCrankLengthSupported()));

                            switch (crankParameters.getCrankLengthStatus())
                            {
                                case INVALID_CRANK_LENGTH:
                                    textView_FullCrankLength.setText("Invalid");
                                    break;
                                case DEFAULT_USED:
                                case SET_AUTOMATICALLY:
                                case SET_MANUALLY:
                                    textView_FullCrankLength.setText(crankParameters
                                        .getFullCrankLength() + "mm");
                                    break;
                                default:
                                    break;
                            }
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
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

                            textView_hardwareRevision.setText(String
                                .valueOf(hardwareRevision));
                            textView_manufacturerID.setText(String.valueOf(manufacturerID));
                            textView_modelNumber.setText(String.valueOf(modelNumber));
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
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

                            textView_mainSoftwareRevision.setText(String
                                .valueOf(mainSoftwareRevision));

                            if (supplementalSoftwareRevision == -2)
                                // Plugin Service installed does not support supplemental revision
                                textView_supplementalSoftwareRevision.setText("?");
                            else if (supplementalSoftwareRevision == 0xFF)
                                // Invalid supplemental revision
                                textView_supplementalSoftwareRevision.setText("");
                            else
                                // Valid supplemental revision
                                textView_supplementalSoftwareRevision.setText(", " + String
                                    .valueOf(supplementalSoftwareRevision));

                            textView_serialNumber.setText(String.valueOf(serialNumber));
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
                                runOnUiThread(
                                    new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            textView_EstTimestamp.setText(String.valueOf(estTimestamp));

                                            textView_CumulativeOperatingTime.setText(String.valueOf(cumulativeOperatingTime) + "s");
                                            textView_BatteryVoltage.setText(String.valueOf(batteryVoltage) + "V");
                                            textView_BatteryStatus.setText(batteryStatus.toString());
                                            textView_CumulativeOperatingTimeResolution.setText(String.valueOf(cumulativeOperatingTimeResolution) + "s");
                                            textView_NumberOfBatteries.setText(String.valueOf(numberOfBatteries));
                                            textView_BatteryIdentifier.setText(String.valueOf(batteryIdentifier));
                                        }
                                    });
                            }
                        });
        }
    };

    IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new IDeviceStateChangeReceiver()
    {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    textView_status.setText(pwrPcc.getDeviceName() + ": " + newDeviceState);
                }
            });
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_bike_power);
//
//        textView_status = (TextView)findViewById(R.id.textView_Status);
//        textView_EstTimestamp = (TextView)findViewById(R.id.textView_EstTimestamp);
//
//        textView_CalculatedPower = (TextView)findViewById(R.id.textView_CalculatedPower);
//        textView_CalculatedPowerSource = (TextView)findViewById(R.id.textView_CalculatedPowerSource);
//
//        textView_CalculatedTorque = (TextView)findViewById(R.id.textView_CalculatedTorque);
//        textView_CalculatedTorqueSource = (TextView)findViewById(R.id.textView_CalculatedTorqueSource);
//
//        textView_CalculatedCrankCadence = (TextView)findViewById(R.id.textView_CalculatedCrankCadence);
//        textView_CalculatedCrankCadenceSource = (TextView)findViewById(R.id.textView_CalculatedCrankCadenceSource);
//
//        textView_CalculatedSpeed = (TextView)findViewById(R.id.textView_CalculatedSpeed);
//        textView_CalculatedSpeedSource = (TextView)findViewById(R.id.textView_CalculatedSpeedSource);
//        textView_CalculatedDistance = (TextView)findViewById(R.id.textView_CalculatedDistance);
//        textView_CalculatedDistanceSource = (TextView)findViewById(R.id.textView_CalculatedDistanceSource);
//
//        textView_instantaneousCadence = (TextView)findViewById(R.id.textView_InstantaneousCadence);
//        textView_PwrOnlyEventCount = (TextView)findViewById(R.id.textView_PwrOnlyEventCount);
//        textView_InstantaneousPower = (TextView)findViewById(R.id.textView_InstantaneousPower);
//        textView_AccumulatedPower = (TextView)findViewById(R.id.textView_AccumulatedPower);
//        textView_PedalPowerPercentage = (TextView)findViewById(R.id.textView_PedalPowerPercentage);
//        textView_RightPedalPowerIndicator = (TextView)findViewById(R.id.textView_RightPedalPowerIndicator);
//
//        textView_WheelTorqueEventCount = (TextView)findViewById(R.id.textView_WheelTorqueEventCount);
//        textView_AccumulatedWheelTicks = (TextView)findViewById(R.id.textView_AccumulatedWheelTicks);
//        textView_AccumulatedWheelPeriod = (TextView)findViewById(R.id.textView_AccumulatedWheelPeriod);
//        textView_AccumulatedWheelTorque = (TextView)findViewById(R.id.textView_AccumulatedWheelTorque);
//
//        textView_CrankTorqueEventCount = (TextView)findViewById(R.id.textView_CrankTorqueEventCount);
//        textView_AccumulatedCrankTicks = (TextView)findViewById(R.id.textView_AccumulatedCrankTicks);
//        textView_AccumulatedCrankPeriod = (TextView)findViewById(R.id.textView_AccumulatedCrankPeriod);
//        textView_AccumulatedCrankTorque = (TextView)findViewById(R.id.textView_AccumulatedCrankTorque);
//
//        textView_TePsEventCount = (TextView)findViewById(R.id.textView_TePsEventCount);
//        textView_LeftTorqueEff = (TextView)findViewById(R.id.textView_LeftTorqueEff);
//        textView_RightTorqueEff = (TextView)findViewById(R.id.textView_RightTorqueEff);
//        textView_SeparatePedalSmoothnessSupport = (TextView)findViewById(R.id.textView_SeparatePedalSmoothnessSupport);
//        textView_LeftCombSmoothness = (TextView)findViewById(R.id.textView_LeftCombSmoothness);
//        textView_RightSmoothness = (TextView)findViewById(R.id.textView_RightSmoothness);
//
//        textView_CtfEventCount = (TextView)findViewById(R.id.textView_CtfEventCount);
//        textView_InstantaneousSlope = (TextView)findViewById(R.id.textView_InstantaneousSlope);
//        textView_AccumulatedTimestamp = (TextView)findViewById(R.id.textView_AccumulatedTimestamp);
//        textView_AccumulatedTorqueTicks = (TextView)findViewById(R.id.textView_AccumulatedTorqueTicks);
//
//        textView_CalibrationId = (TextView)findViewById(R.id.textView_CalibrationId);
//        textView_CalibrationData = (TextView)findViewById(R.id.textView_CalibrationData);
//        textView_CtfOffset = (TextView)findViewById(R.id.textView_CtfOffset);
//        textView_ManufacturerSpecificBytes = (TextView)findViewById(R.id.textView_ManufacturerSpecificBytes);
//
//        textView_AutoZeroStatus = (TextView)findViewById(R.id.textView_AutoZeroStatus);
//
//        textView_FullCrankLength = (TextView)findViewById(R.id.textView_FullCrankLength);
//        textView_CrankLengthStatus = (TextView)findViewById(R.id.textView_CrankLengthStatus);
//        textView_SensorSoftwareMismatchStatus = (TextView)findViewById(R.id.textView_SensorSoftwareMismatchStatus);
//        textView_SensorAvailabilityStatus = (TextView)findViewById(R.id.textView_SensorAvailabilityStatus);
//        textView_CustomCalibrationStatus = (TextView)findViewById(R.id.textView_CustomCalibrationStatus);
//        textView_AutoCrankLengthSupport = (TextView)findViewById(R.id.textView_AutoCrankLengthSupport);
//
//        textView_manufacturerID = (TextView)findViewById(R.id.textView_ManufacturerID);
//        textView_serialNumber = (TextView)findViewById(R.id.textView_SerialNumber);
//        textView_modelNumber = (TextView)findViewById(R.id.textView_ModelNumber);
//        textView_hardwareRevision = (TextView)findViewById(R.id.textView_HardwareRevision);
//        textView_mainSoftwareRevision = (TextView)findViewById(R.id.textView_MainSoftwareRevision);
//        textView_supplementalSoftwareRevision = (TextView)findViewById(R.id.textView_SupplementalSoftwareRevision);
//
//        button_requestManualCalibration = (Button)findViewById(R.id.button_requestManualCalibration);
//        button_setAutoZero = (Button)findViewById(R.id.button_setAutoZero);
//        button_requestCrankParameters = (Button)findViewById(R.id.button_requestCrankParameters);
//        button_setCrankParameters = (Button)findViewById(R.id.button_setCrankParameters);
//        button_requestCustomCalibrationParameters = (Button)findViewById(R.id.button_requestCustomCalibrationParameters);
//        button_setCustomCalibrationParameters = (Button)findViewById(R.id.button_setCustomCalibrationParameters);
//        button_setCtfSlope = (Button)findViewById(R.id.button_setCtfSlope);
//        button_commandBurst = (Button)findViewById(R.id.button_commandBurst);
//
//        textView_CumulativeOperatingTime = (TextView)findViewById(R.id.textView_CumulativeOperatingTime);
//        textView_BatteryVoltage = (TextView)findViewById(R.id.textView_BatteryVoltage);
//        textView_BatteryStatus = (TextView)findViewById(R.id.textView_BatteryStatus);
//        textView_CumulativeOperatingTimeResolution = (TextView)findViewById(R.id.textView_CumulativeOperatingTimeResolution);
//        textView_NumberOfBatteries = (TextView)findViewById(R.id.textView_NumberOfBatteries);
//        textView_BatteryIdentifier = (TextView)findViewById(R.id.textView_BatteryIdentifier);

        final IRequestFinishedReceiver requestFinishedReceiver =
            new IRequestFinishedReceiver()
        {
            @Override
            public void onNewRequestFinished(final RequestStatus requestStatus)
            {
                runOnUiThread(
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            switch(requestStatus)
                            {
                                case SUCCESS:
                                    Toast.makeText(Activity_BikePowerSampler.this, "Request Successfully Sent", Toast.LENGTH_SHORT).show();
                                    break;
                                case FAIL_PLUGINS_SERVICE_VERSION:
                                    Toast.makeText(Activity_BikePowerSampler.this,
                                        "Plugin Service Upgrade Required?",
                                        Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    Toast.makeText(Activity_BikePowerSampler.this, "Request Failed to be Sent", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    });
            }
        };

        button_requestManualCalibration.setOnClickListener(
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    boolean submitted = pwrPcc.requestManualCalibration(requestFinishedReceiver);

                    if(!submitted)
                        Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
                }
            });

        button_setAutoZero.setOnClickListener(
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_BikePowerSampler.this);
                    builder.setMessage("Enable or Disable Auto-Zero?");
                    builder.setPositiveButton("Enable", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            boolean submitted = pwrPcc.requestSetAutoZero(true, requestFinishedReceiver);

                            if(!submitted)
                                Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();

                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("Disable", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            boolean submitted = pwrPcc.requestSetAutoZero(false, requestFinishedReceiver);

                            if(!submitted)
                                Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();

                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });

//        button_requestCrankParameters.setOnClickListener(
//            new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    boolean submitted = pwrPcc.requestCrankParameters(requestFinishedReceiver);
//
//                    if(!submitted)
//                        Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        button_setCrankParameters.setOnClickListener(
//            new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    //NOTE: Determine if the crank can support setting crank length before attempting to set it
//                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_BikePowerSampler.this);
//                    builder.setMessage("Manual or Auto Crank Length?");
//                    builder.setPositiveButton("Auto", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            boolean submitted = pwrPcc.requestSetCrankParameters(CrankLengthSetting.AUTO_CRANK_LENGTH, null, requestFinishedReceiver);
//
//                            if(!submitted)
//                                Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
//
//                            dialog.cancel();
//                        }
//                    });
//                    builder.setNegativeButton("Manual", new DialogInterface.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which)
//                        {
//                            CrankLengthSetting newSetting = CrankLengthSetting.MANUAL_CRANK_LENGTH;
//                            //TODO UI to allow user to input crank length
//                            BigDecimal newCrankLength = new BigDecimal("172.5");
//
//                            boolean submitted = pwrPcc.requestSetCrankParameters(newSetting, newCrankLength, requestFinishedReceiver);
//
//                            if(!submitted)
//                                Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
//
//                            dialog.cancel();
//                        }
//                    });
//                    builder.show();
//                }
//            });
//
//        button_requestCustomCalibrationParameters.setOnClickListener(
//            new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    //TODO Manufacturer specific data required here
//                    byte[] customParameters = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//
//                    boolean submitted = pwrPcc.requestCustomCalibrationParameters(customParameters, requestFinishedReceiver);
//
//                    if(!submitted)
//                        Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        button_setCustomCalibrationParameters.setOnClickListener(
//            new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    //TODO This is only an example, manufacturer specific data required here
//                    byte[] customParameters = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
//
//                    boolean submitted = pwrPcc.requestSetCustomCalibrationParameters(customParameters, requestFinishedReceiver);
//
//                    if(!submitted)
//                        Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        button_setCtfSlope.setOnClickListener(
//            new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    //TODO UI to allow user to set slope
//                    BigDecimal newSlope = new BigDecimal("10.0");
//
//                    boolean submitted = pwrPcc.requestSetCtfSlope(newSlope,  requestFinishedReceiver);
//
//                    if(!submitted)
//                        Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        button_commandBurst.setOnClickListener(
//                new View.OnClickListener()
//                {
//                    //TODO This is only an example, manufacturer specific data required here
//                    int exampleCommandId = 42;
//                    byte[] exampleCommandData = new byte[8];
//                    @Override
//                    public void onClick(View v)
//                    {
//                        boolean submitted = pwrPcc.requestCommandBurst(exampleCommandId, exampleCommandData, requestFinishedReceiver);
//
//                        if(!submitted)
//                            Toast.makeText(Activity_BikePowerSampler.this, "Request Could not be Made", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
        resetPcc();
    }

    private void resetPcc()
    {
        if (releaseHandle != null) {
            releaseHandle.close();
        }


        if (1 != 1)
        {
            // device has already been selected through the multi-device search
//            MultiDeviceSearchResult result = intent
//                    .getParcelableExtra(Activity_MultiDeviceSearchSampler.EXTRA_KEY_MULTIDEVICE_SEARCH_RESULT);
//            releaseHandle = AntPlusBikePowerPcc.requestAccess(this, result.getAntDeviceNumber(), 0,
//                    mResultReceiver, mDeviceStateChangeReceiver);
        } else {
            // starts the plugins UI search
            releaseHandle = AntPlusBikePowerPcc.requestAccess(activity, cont, mResultReceiver,
                    mDeviceStateChangeReceiver);
        }

        if(releaseHandle != null)
        {
            releaseHandle.close();
        }

//        textView_status.setText("Connecting...");
//        textView_EstTimestamp.setText("---");
//
//        textView_CalculatedPower.setText("---");
//        textView_CalculatedPowerSource.setText("---");
//
//        textView_CalculatedTorque.setText("---");
//        textView_CalculatedTorqueSource.setText("---");
//
//        textView_CalculatedCrankCadence.setText("---");
//        textView_CalculatedCrankCadenceSource.setText("---");
//
//        textView_CalculatedSpeed.setText("---");
//        textView_CalculatedSpeedSource.setText("---");
//        textView_CalculatedDistance.setText("---");
//        textView_CalculatedDistanceSource.setText("---");
//
//        textView_instantaneousCadence.setText("---");
//        textView_PwrOnlyEventCount.setText("---");
//        textView_InstantaneousPower.setText("---");
//        textView_AccumulatedPower.setText("---");
//        textView_PedalPowerPercentage.setText("---");
//        textView_RightPedalPowerIndicator.setText("---");
//
//        textView_WheelTorqueEventCount.setText("---");
//        textView_AccumulatedWheelTicks.setText("---");
//        textView_AccumulatedWheelPeriod.setText("---");
//        textView_AccumulatedWheelTorque.setText("---");
//
//        textView_CrankTorqueEventCount.setText("---");
//        textView_AccumulatedCrankTicks.setText("---");
//        textView_AccumulatedCrankPeriod.setText("---");
//        textView_AccumulatedCrankTorque.setText("---");
//
//        textView_TePsEventCount.setText("---");
//        textView_LeftTorqueEff.setText("---");
//        textView_RightTorqueEff.setText("---");
//        textView_SeparatePedalSmoothnessSupport.setText("---");
//        textView_LeftCombSmoothness.setText("---");
//        textView_RightSmoothness.setText("---");
//
//        textView_CtfEventCount.setText("---");
//        textView_InstantaneousSlope.setText("---");
//        textView_AccumulatedTimestamp.setText("---");
//        textView_AccumulatedTorqueTicks.setText("---");
//
//        textView_CalibrationId.setText("---");
//        textView_CalibrationData.setText("---");
//        textView_CtfOffset.setText("---");
//        textView_ManufacturerSpecificBytes.setText("---");
//
//        textView_AutoZeroStatus.setText("---");
//
//        textView_FullCrankLength.setText("---");
//        textView_CrankLengthStatus.setText("---");
//        textView_SensorSoftwareMismatchStatus.setText("---");
//        textView_SensorAvailabilityStatus.setText("---");
//        textView_CustomCalibrationStatus.setText("---");
//        textView_AutoCrankLengthSupport.setText("---");
//
//        textView_manufacturerID.setText("---");
//        textView_serialNumber.setText("---");
//        textView_modelNumber.setText("---");
//        textView_hardwareRevision.setText("---");
//        textView_mainSoftwareRevision.setText("---");
//        textView_supplementalSoftwareRevision.setText("");
//
//        button_requestManualCalibration.setEnabled(false);
//        button_setAutoZero.setEnabled(false);
//        button_requestCrankParameters.setEnabled(false);
//        button_setCrankParameters.setEnabled(false);
//        button_requestCustomCalibrationParameters.setEnabled(false);
//        button_setCustomCalibrationParameters.setEnabled(false);
//        button_setCtfSlope.setEnabled(false);
//        button_commandBurst.setEnabled(false);
//
//        textView_CumulativeOperatingTime.setText("---");
//        textView_BatteryVoltage.setText("---");
//        textView_BatteryStatus.setText("---");
//        textView_CumulativeOperatingTimeResolution.setText("---");
//        textView_NumberOfBatteries.setText("---");
//        textView_BatteryIdentifier.setText("---");

        Intent intent = getIntent();
        if (intent.hasExtra(Activity_MultiDeviceSearchSampler.EXTRA_KEY_MULTIDEVICE_SEARCH_RESULT))
        {
            // device has already been selected through the multi-device search
            MultiDeviceSearchResult result = intent
                .getParcelableExtra(Activity_MultiDeviceSearchSampler.EXTRA_KEY_MULTIDEVICE_SEARCH_RESULT);
            releaseHandle = AntPlusBikePowerPcc.requestAccess(this, result.getAntDeviceNumber(), 0,
                mResultReceiver, mDeviceStateChangeReceiver);
        } else
        {
            // starts the plugins UI search
            releaseHandle = AntPlusBikePowerPcc.requestAccess(this, this, mResultReceiver,
                mDeviceStateChangeReceiver);
        }

    }

    }

    @Override
    protected void onDestroy()
    {
        releaseHandle.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_heart_rate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_reset:
                resetPcc();
                textView_status.setText("Resetting...");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
