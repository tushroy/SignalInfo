package com.cc.signalinfo.signals;

import android.telephony.TelephonyManager;
import com.cc.signalinfo.enums.NetworkType;
import com.cc.signalinfo.enums.Signal;
import com.cc.signalinfo.util.StringUtils;

import java.util.EnumSet;
import java.util.Map;

/**
 * Stores all signal info related to CDMA
 *
 * @author Wes Lanning
 * @version 2013 -04-29
 */
public class CdmaInfo extends SignalInfo
{
    /**
     * Instantiates a new Cdma info.
     *
     * @param tm - instance of telephonyManager
     * @param signals the signals
     */
    public CdmaInfo(TelephonyManager tm, Map<Signal, String> signals)
    {
        super(NetworkType.CDMA, tm, signals);
        possibleValues = EnumSet.range(Signal.CDMA_RSSI, Signal.EVDO_SNR);
    }

    /**
     * Instantiates a new Cdma info.
     *
     * @param tm - instance of telephonyManager
     * @param signals the signals
     * @param preferDb - if true, convert all non-decibel readings (centibels) to decibels
     */
    public CdmaInfo(TelephonyManager tm, Map<Signal, String> signals, boolean preferDb)
    {
        super(NetworkType.CDMA, tm, signals, preferDb);
        possibleValues = EnumSet.range(Signal.CDMA_RSSI, Signal.EVDO_SNR);
    }

    /**
     * Instantiates a new Cdma info.
     *
     * @param tm - instance of telephonyManager
     */
    public CdmaInfo(TelephonyManager tm)
    {
        this(tm, null);
    }

    /**
     * Is the current network type being used on the device?
     * Return of false means there's no signal currently, not that
     * the device cannot receive signals of this type of network.
     *
     * @return true if enabled
     */
    @Override
    public boolean enabled()
    {
        return !StringUtils.isNullOrEmpty(signals.get(Signal.CDMA_RSSI))
            || !StringUtils.isNullOrEmpty(signals.get(Signal.EVDO_RSSI));
    }
}
