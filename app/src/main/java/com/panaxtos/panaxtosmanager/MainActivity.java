package com.panaxtos.panaxtosmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    private TextView text2;
    private Button btn_read;
    private static final String TAG = "MainActivity";
    public static ArrayList<Double> Ch1Buffer = new ArrayList<Double>();
    public static ArrayList<Double> Ch2Buffer = new ArrayList<Double>();
    public static double[] Ch1DataArrayToFiltering = new double[250];
    public static double[] Ch2DataArrayToFiltering = new double[250];
    double ch1_deltaTotal = 0, ch1_smrTotal = 0, ch1_thetaTotal = 0, ch1_betaHighTotal = 0, ch1_alphaTotal = 0;
    double ch1_deltaAvr = 0, ch1_smrAvr = 0, ch1_thetaAvr = 0, ch1_betaHighAvr = 0, ch1_alphaAvr = 0;
    double ch2_deltaTotal = 0, ch2_smrTotal = 0, ch2_thetaTotal = 0, ch2_betaHighTotal = 0, ch2_alphaTotal = 0;
    double ch2_deltaAvr = 0, ch2_smrAvr = 0, ch2_thetaAvr = 0, ch2_betaHighAvr = 0, ch2_alphaAvr = 0;
    public static String saveStorage = "";
    static{
        System.loadLibrary("pFilter");
    }
    private static int[] hexToIntArray2(String hex_string)
    {
        int[] byteInt = new int[62];
        for (int i = 0; i < hex_string.length() / 2; i++)
        {
            byteInt[i] = Integer.parseInt(hex_string.substring(2 * i, 2 * i + 2), 16);
        }

        return byteInt;
    }
    public native double[] Ch1ByteTomV(byte[] bytes);
    public native double[] Ch2ByteTomV(byte[] bytes);
    public native double parsingData(int first, int second, int third);
    public native double[] notch60lowpass50high1(double[] data);
    public native double[] FFT(double[] raw);
    public native double[] BandAbs(double[] fft);
    public native boolean Relax(double[] band1, double[]  band2);
    public native boolean Attention(double[] band1, double[] band2);
    public native boolean Stress(double[] band1, double[] band2);
    public native double pBandAlpha(double[] filteredData);
    public native double AttValue(double theta, double smr, double delta, double betah);
    public native double RelaxValue(double alpha, double delta);
    public native double StressValue(double delta, double betah);
    public native double[] MentalState(double[] band1, double[] band2);


    public ArrayList<String> readFile()
    {
        InputStream inputData = getResources().openRawResource(R.raw.bytes3);
        ArrayList<String> arraylist = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputData));
            while(true)
            {
                String string = bufferedReader.readLine();
                if(string != null)
                {
                    arraylist.add(string);
                }
                else
                {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text2 = (TextView)findViewById(R.id.text3);
        btn_read = (Button)findViewById(R.id.btn1) ;
        for(int i=0; i<250; i++)
        {
            Ch1DataArrayToFiltering[i] = 0;
            Ch2DataArrayToFiltering[i] = 0;
        }

        btn_read.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                ArrayList<String> readData = readFile();
                int count = 0;
                int count2 = 1;
                String printData = "";
                while(count < readData.size()  )
                {
                    String data = readData.get((count));
                    count +=1;
                    int[] IPC_buffer = hexToIntArray2(data);
                    int ch1_first, ch1_second, ch1_third;
                    int ch2_first, ch2_second, ch2_third;
                    double ch1_double, ch2_double;
                    double[] ch1_mvData = new double[10];
                    double[] ch2_mvData = new double[10];
                    for(int i =1 ; i< 31; i+=3)
                    {
                        ch1_first = IPC_buffer[i];
                        ch1_second = IPC_buffer[i+1];
                        ch1_third = IPC_buffer[i+2];
                        ch1_double = parsingData(ch1_first, ch1_second, ch1_third);
                        ch1_mvData[i/3] = ch1_double;
                    }
                    for(int i = 32; i < 62; i+=3)
                    {
                        ch2_first = IPC_buffer[i];
                        ch2_second = IPC_buffer[i+1];
                        ch2_third = IPC_buffer[i+2];
                        ch2_double = parsingData(ch2_first, ch2_second, ch2_third);
                        ch2_mvData[i/3 - 10] = ch2_double;
                    }
                    //Log.d(TAG, "ch1 : "  + ch1_mvData[0]);
                    for(int i=0; i< 240; i++)
                    {
                        Ch1DataArrayToFiltering[i] = Ch1DataArrayToFiltering[i + 10];
                        Ch2DataArrayToFiltering[i] = Ch2DataArrayToFiltering[i + 10];
                    }
                    for(int i=0; i<10; i++)
                    {
                        Ch1DataArrayToFiltering[240 + i] = ch1_mvData[i];
                        Ch2DataArrayToFiltering[240 + i] = ch2_mvData[i];
                    }
                    double[] filteredCh1Data = new double[10];
                    filteredCh1Data = notch60lowpass50high1(Ch1DataArrayToFiltering);
                    //Log.d(TAG, "filtered : "  + filteredCh1Data[9]);
                    double[] filteredCh2Data = new double[10];
                    filteredCh2Data = notch60lowpass50high1(Ch2DataArrayToFiltering);
                    //Log.d(TAG, "notch : "  + filteredCh1Data[9]);
                    for(int i=0; i < 10; i++)
                    {
                        Ch1Buffer.add(filteredCh1Data[i]);
                        Ch2Buffer.add(filteredCh2Data[i]);
                    }
                    if(Ch1Buffer.size() >= 250)
                    {
                        double[] raw1 = new double[250];
                        double[] raw2 = new double[250];
                        for(int i=0; i<250; i++)
                        {
                            raw1[i] = Ch1Buffer.get(i);
                            raw2[i] = Ch2Buffer.get(i);
                        }
                        double[] dFft1 = FFT(raw1);
                        double[] dFft2 = FFT(raw2);
                        double[] abs1 = BandAbs(dFft1);
                        //Log.d(TAG, "Alpha : " + abs1[11]);
                        double[] abs2 = BandAbs(dFft2);
                        double ch1_theta = abs1[1];
                        double ch1_smr = abs1[4];
                        double ch1_delta = abs1[0];
                        double ch1_betah = abs1[7];
                        double ch1_alpha = abs1[11];
                        double ch1_lowBeta = abs1[5];
                        double ch2_theta = abs2[1];
                        double ch2_smr = abs2[4];
                        double ch2_delta = abs2[0];
                        double ch2_betah = abs2[7];
                        double ch2_alpha = abs2[11];
                        double ch2_lowBeta = abs2[5];
                        ch1_thetaTotal += ch1_theta;
                        ch1_smrTotal += ch1_smr;
                        ch1_deltaTotal += ch1_delta;
                        ch1_betaHighTotal += ch1_betah;
                        ch1_alphaTotal += ch1_alpha;
                        ch2_thetaTotal += ch2_theta;
                        ch2_smrTotal += ch2_smr;
                        ch2_deltaTotal += ch2_delta;
                        ch2_betaHighTotal += ch2_betah;
                        ch2_alphaTotal += ch2_alpha;
                        double[] mental1 = new double[2];
                        mental1 = MentalState(abs1, abs2);
                        double mentalValue1= mental1[0];
                        double mentalValue2= mental1[1];
                        //boolean result = Stress(abs1, abs2);
                        //Log.d(TAG, "ch1_StressValue : " + ch1_StressValue +  "      "  + count2);
                        Ch1Buffer.clear();
                        Ch2Buffer.clear();
                        count2++;
                    }
                }
                ch1_thetaAvr = ch1_thetaTotal / (count2 - 1);
                ch1_smrAvr = ch1_smrTotal / (count2 - 1);
                ch1_betaHighAvr = ch1_betaHighTotal / (count2 - 1);
                ch1_deltaAvr = ch1_deltaTotal / (count2 - 1);
                ch1_alphaAvr = ch1_alphaTotal / (count2 - 1);
                ch2_thetaAvr = ch2_thetaTotal / (count2 - 1);
                ch2_smrAvr = ch2_smrTotal / (count2 - 1);
                ch2_betaHighAvr = ch2_betaHighTotal / (count2 - 1);
                ch2_deltaAvr = ch2_deltaTotal / (count2 - 1);
                ch2_alphaAvr = ch2_alphaTotal / (count2 - 1);
                double Ch1_attValue = AttValue(ch1_thetaAvr, ch1_smrAvr, ch1_deltaAvr, ch1_betaHighAvr);
                double Ch1_relaxValue = RelaxValue(ch1_alphaAvr, ch1_deltaAvr);
                double Ch1_stressValue = StressValue(ch1_deltaAvr, ch1_betaHighAvr);
                double Ch2_attValue = AttValue(ch2_thetaAvr, ch2_smrAvr, ch2_deltaAvr, ch2_betaHighAvr);
                double Ch2_relaxValue = RelaxValue(ch2_alphaAvr, ch2_deltaAvr);
                double Ch2_stressValue = StressValue(ch2_deltaAvr, ch2_betaHighAvr);
                Log.d(TAG, "Ch1_relaxValue : " + Ch1_relaxValue +  "      "  + count2);
            }

        });
    }
}