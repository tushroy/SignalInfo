/*
 *
 * Copyright (c) 2013 Wes Lanning, http://codingcreation.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * http://www.opensource.org/licenses/mit-license.php
 */

package com.cc.signalinfo.util;

import android.os.AsyncTask;
import android.telephony.SignalStrength;
import android.util.Log;
import com.cc.signalinfo.config.AppSetup;
import com.cc.signalinfo.listeners.SignalListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wraps the raw signal data in order to filter
 * out invalid values as well as make the array compatible with ICS+
 * for shitty old devices from 2.3 and before
 * (though HSPA+ was not officially added until ICS, so technically 3.0 too).
 *
 * @author Wes Lanning
 * @version 2013-05-10
 */
public class SignalArrayWrapper
{
    public static final  String[] EMPTY_SIGNAL_ARRAY    = new String[0];
    // filter out any readings matching this regex as they're invalid
    private static final Pattern  FILTER_INVALID_SIGNAL = Pattern.compile("-1\\b|-?99\\b|0x[\\d]+|-?[4-9][0-9]{3,}|-?[0-9]{4,}");
    private static final Pattern  FILTER_NON_NUM        = Pattern.compile("\\s?[^- \\d]+", Pattern.CASE_INSENSITIVE);
    // the normal system size for signal readings on ics
    private static final int      ICS_ARRAY_SIZE        = 12;
    // some devices (looking at you Huawei) add GSM ECIO, LTE RSSI and GSM RSSI
    private static final int      ICS_BIG_ARRAY_SIZE    = 14;
    // old devices half assed the LTE RIL to have some stuff, but not all (looking at you LG pre ICS)
    private static final int      LEGACY_BIG_ARRAY_SIZE = 10;
    private static final Pattern  SPACE_STR             = Pattern.compile(" ");
    private static final String   TAG                   = SignalArrayWrapper.class.getSimpleName();
    // keep a copy of the raw data for debugging purposes mostly
    private String rawData;
    private        String[]           filteredArray = EMPTY_SIGNAL_ARRAY;
    private        FilterSignalTask   task          = null;
    private static SignalArrayWrapper instance      = null;
    private SignalListener.UpdateSignal listener;

    /**
     * Constructor mainly for testing (passing in a mock object
     * for signals)
     *
     * @param signalArray - contains the raw signal info reported from the system
     */
    public SignalArrayWrapper(String signalArray, SignalListener.UpdateSignal listener)
    {
        rawData = signalArray;
        this.listener = listener;
        filterSignals(signalArray);
    }

    public final void filterSignals(String signalArray)
    {
        Log.d("Raw Signal Data", rawData);
        FilterSignalTask task = new FilterSignalTask();
        task.execute(signalArray, listener);
    }

    /**
     * Returns a copy (not a reference) of the signal array after
     * being reformatted to meet ICS+ expectations.
     *
     * @return the processed signal array in the form one expects in ICS+
     */
    public String[] getFilteredArray()
    {
        return Arrays.copyOf(filteredArray, filteredArray.length);
    }

    /**
     * Gets the pre-filtered data from the signal array.
     * Useful really only for debugging/testing purposes.
     *
     * @return raw signal data
     */
    public String getRawData()
    {
        return rawData;
    }

    private class FilterSignalTask extends AsyncTask<Object, Void, Object[]>
    {
        @Override
        protected Object[] doInBackground(Object... params)
        {
            // remove all invalid signals and put in our default string instead to make life easier
            String rawData = (String) params[0];
            Log.d(TAG, String.format("rawData: %s", rawData));

            SignalListener.UpdateSignal listener = (SignalListener.UpdateSignal) params[1];
            String filteredData = FILTER_NON_NUM.matcher(rawData).replaceAll("").trim();
            Log.d(TAG, String.format("filtered after 1st regex: %s", filteredData));

            filteredData = FILTER_INVALID_SIGNAL.matcher(filteredData).replaceAll(AppSetup.INVALID_TXT);
            Log.d(TAG, String.format("filtered after 2nd regex: %s", filteredData));

            String[] splitSignals = SPACE_STR.split(filteredData);
            Log.d(TAG, String.format("splitsignals: %s", java.util.Arrays.toString(splitSignals)));

            // TODO: fix stupid devices like Huawai and LG that do LTE_RSSI = LTE_Signal_Strength
            String[] extendedSignalData = new String[ICS_BIG_ARRAY_SIZE];
            extendedSignalData = Arrays.copyOf(splitSignals, extendedSignalData.length);


            if (splitSignals.length < extendedSignalData.length) {
                // adjust array to account for devices that might have more readings than standard
                java.util.Arrays.fill(extendedSignalData, splitSignals.length, extendedSignalData.length, AppSetup.INVALID_TXT);
            }
            Log.d("Extended Filtered Signal Data", java.util.Arrays.toString(extendedSignalData));

            // not sure this is needed for crap devices so ignoring for now
/*            if (splitSignals.length == extendedSignalData.length || splitSignals.length == LEGACY_BIG_ARRAY_SIZE) {
                // fucked up devices that don't implement any correct standard for the RIL
                // thankfully, it's only a handful of older devices made by LG and Huawei

                int endPos = splitSignals.length == extendedSignalData.length
                    ? ICS_ARRAY_SIZE
                    : LEGACY_BIG_ARRAY_SIZE;


                for (int i = 7; i < endPos - 1; ++i) {
                    String temp = extendedSignalData[i];
                    extendedSignalData[i] = extendedSignalData[i + 1];
                    extendedSignalData[i + 1] = temp;
                }
                Log.d(TAG, "Device had extended signal data.");
            }*/
            Log.d("Filtered Signal Data", java.util.Arrays.toString(splitSignals));
            return new Object[]{extendedSignalData, listener};
        }

        @Override
        protected void onPostExecute(Object... result)
        {
            filteredArray = (String[]) result[0];
            SignalListener.UpdateSignal listener = (SignalListener.UpdateSignal) result[1];
            listener.setData(SignalArrayWrapper.this);
        }
    }

    private static int[] processSignalInfo(CharSequence signalStrength)
    {
        Matcher signalMatches = FILTER_INVALID_SIGNAL.matcher(signalStrength);
        int[] extendedSignalData = new int[ICS_BIG_ARRAY_SIZE];
        int signalCount;

        for (signalCount = 0; signalMatches.find(); ++signalCount) {
            extendedSignalData[signalCount] = Integer.parseInt(signalMatches.group());
        }
        java.util.Arrays.fill(extendedSignalData, signalCount, extendedSignalData.length, AppSetup.INVALID);

        return extendedSignalData;
    }
}