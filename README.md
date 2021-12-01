# ByteToAlphaTest
![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/0e43065f-bb9b-4a04-8107-4f091fe24754/Untitled.png)

ReadFile Button 클릭 시 내부의 res 폴더에 있는 bytes.txt 파일을 읽고 Relax 값을 반환한다.

Stress 값을 받고 싶은 경우 또는 Attention 값을 받고 싶은 경우

```csharp
double[] dFft1 = FFT(raw1);
double[] dFft2 = FFT(raw2);
double[] abs1 = BandAbs(dFft1);
double[] abs2 = BandAbs(dFft2);
//boolean result = Relax2(abs1, abs2);        -> 변경
boolean result = Stress(abs1, abs2);          <- 으로 수정
// boolean result = Attention(abs1, abs2);     <- 으로 수정 
Log.d(TAG, "Relax2 : " + result +  "      "  + count2);
Ch1Buffer.clear();
Ch2Buffer.clear();
count2++;
```

## 함수 Test 결과값 확인

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/cd129e2b-5ba4-436a-95bc-b0ccd07ae665/Untitled.png)

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/53632d18-cfd7-48f8-91ff-d33dc775bbee/Untitled.png)

C++ 결과값과 똑같이 나오는것을 확인 할 수 있다.

## 중요 함수

boolean Relax2(double[] band1, double[] band2) : Relax 값을 return

boolean Attention(double[] band1, double[] band2) : Attention 값을 return

boolean Stress(double[] band1, double[] band2) : Stress 값 return

## 사용 방법

블루투스 수신 callback으로부터 Byte값을 받는다. 

callback 함수에 아래 코드를 적용 시킬 경우  Relax 값을 받을 수 있다.

```csharp
String data = readData.get((count));   // txt 값을 읽었기 때문에 String 변수 이다.
int[] IPC_buffer = hexToIntArray2(data); // 이를 62 int형 배열로 변경
//byteArray 여도 int형으로 읽는것이 가능.
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
filteredCh1Data = notch60lowpass50(Ch1DataArrayToFiltering);
//Log.d(TAG, "filtered : "  + filteredCh1Data[9]);
double[] filteredCh2Data = new double[10];
filteredCh2Data = notch60lowpass50(Ch2DataArrayToFiltering);
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
    //Log.d(TAG, "abs1 : "  + abs1[0] + ", " + abs1[1] + ", " + abs1[2] + ", " + abs1[3] + ", " + abs1[4] + ", " + abs1[5] + ", " + abs1[6] + ", " + abs1[7]);
    double[] abs2 = BandAbs(dFft2);
    boolean result = Relax2(abs1, abs2);
    Log.d(TAG, "Relax2 : " + result );
    Ch1Buffer.clear();
    Ch2Buffer.clear();
}
```
