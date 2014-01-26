package com.liuyajun.sensortofile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener,
		OnClickListener {
	private SensorManager mSensorManager;
	private Sensor mLinearSensor, mASensor, mMagneticFieldSensor, mGSensor;
	private EditText fileName;
	private Button ok, start, stop;
	private TextView currData;
	private String file;
	long beginTime;
	boolean hasStart = false;
	String TAG = "方向";

	File realFile, realRAWlFile;
	FileOutputStream out, rawOut;

	float[] linearAccelerometerValues = null;
	float[] accelerometerValues = null;
	float[] magneticFieldValues = null;
	float[] gValues = null;
	float[] RM = new float[16]; // 旋转矩阵
	float[] orac=new float[9];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		fileName = (EditText) findViewById(R.id.fileName);
		ok = (Button) findViewById(R.id.okButton);
		start = (Button) findViewById(R.id.startButton);
		stop = (Button) findViewById(R.id.stopButton);
		currData = (TextView) findViewById(R.id.currData);

		ok.setOnClickListener(this);
		start.setOnClickListener(this);
		stop.setOnClickListener(this);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLinearSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mASensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mMagneticFieldSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		float tempx, tempy, tempz,xw,yw,zw;
		if (arg0.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			linearAccelerometerValues = arg0.values.clone();

			if (accelerometerValues == null || magneticFieldValues == null
					|| linearAccelerometerValues == null || gValues == null
					|| RM == null) {
				return;
			}

			// 写入原始数据
			try {
				rawOut.write((""
						+ (arg0.timestamp - beginTime)
						+ ","
						+ linearAccelerometerValues[0]
						+ ","
						+ linearAccelerometerValues[1]
						+ ","
						+ linearAccelerometerValues[2]
						+ ","
						+ gValues[0]
						+ ","
						+ gValues[1]
						+ ","
						+ gValues[2]
						+ ","
						+ Math.sqrt(accelerometerValues[0]
								* accelerometerValues[0]
								+ accelerometerValues[1]
								* accelerometerValues[1]
								+ accelerometerValues[2]
								* accelerometerValues[2]) + "\n").getBytes());
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			xw=orac[1];
			yw=orac[2];
			zw=orac[0];
			
			
			// z轴变换
			tempx = linearAccelerometerValues[0];
			tempy = linearAccelerometerValues[1];
			tempz = linearAccelerometerValues[2];
			
			linearAccelerometerValues[0] = (float) (tempx
					* Math.cos(zw) - tempy
					* Math.sin(zw));
			linearAccelerometerValues[1] = (float) (tempx
					* Math.sin(zw) + tempy
					* Math.cos(zw));

			tempx = linearAccelerometerValues[0];
			tempy = linearAccelerometerValues[1];
			tempz = linearAccelerometerValues[2];
			// x轴变换
			linearAccelerometerValues[1] = (float) (tempy
					* Math.cos(xw) - tempz
					* Math.sin(xw));
			linearAccelerometerValues[2] = (float) (tempy
					* Math.sin(xw) + tempz
					* Math.cos(xw));

			tempx = linearAccelerometerValues[0];
			tempy = linearAccelerometerValues[1];
			tempz = linearAccelerometerValues[2];
			// y轴变换
			linearAccelerometerValues[2] = (float) (tempz
					* Math.cos(yw) - tempx
					* Math.sin(yw));
			linearAccelerometerValues[0] = (float) (tempz
					* Math.sin(yw) + tempx
					* Math.cos(yw));
			
//			float[] tempLinearValues=new float[16];
//			
//			Matrix.multiplyMV(tempLinearValues, 0, RM, 0, new float[] {
//					linearAccelerometerValues[0], linearAccelerometerValues[1],
//					linearAccelerometerValues[2], 0.0f }, 0);

			// //用旋转矩阵做坐标变换
			// Matrix.multiplyMV(null , 0, null, 0, null, 0);
			// tempx = linearAccelerometerValues[0];
			// tempy = linearAccelerometerValues[1];
			// tempz = linearAccelerometerValues[2];
			//
			// linearAccelerometerValues[0]=RM[0]*tempx+RM[1]*tempy+RM[2]*tempz;
			// linearAccelerometerValues[1]=RM[3]*tempx+RM[4]*tempy+RM[5]*tempz;
			// linearAccelerometerValues[2]=RM[6]*tempx+RM[7]*tempy+RM[8]*tempz;

			try {
				currData.setText("" + linearAccelerometerValues[0] + ","
						+ linearAccelerometerValues[1] + ","
						+ linearAccelerometerValues[2]);

				// 写入转换后的数据
				out.write((""
						+ (arg0.timestamp - beginTime)
						+ ","
						+ linearAccelerometerValues[0]
						+ ","
						+ linearAccelerometerValues[1]
						+ ","
						+ linearAccelerometerValues[2]
						+ ","
						+ gValues[0]
						+ ","
						+ gValues[1]
						+ ","
						+ gValues[2]
						+ ","
						+ Math.sqrt(accelerometerValues[0]
								* accelerometerValues[0]
								+ accelerometerValues[1]
								* accelerometerValues[1]
								+ accelerometerValues[2]
								* accelerometerValues[2]) + "\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (arg0.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				magneticFieldValues = arg0.values.clone();
			} else if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues = arg0.values.clone();
			} else if (arg0.sensor.getType() == Sensor.TYPE_GRAVITY) {
				gValues = arg0.values.clone();
			}

			if (accelerometerValues == null || magneticFieldValues == null
					|| gValues == null) {
				return;
			}

			float[] R = new float[9];

			if (SensorManager.getRotationMatrix(R, null, gValues,
					magneticFieldValues)) {
//				Matrix.transposeM(RM, 0, R, 0);
				SensorManager.getOrientation(R,orac );
			}

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.okButton:
			file = fileName.getText().toString();

			break;
		case R.id.startButton:

			if (hasStart) {
				Toast.makeText(getApplicationContext(), "请先停止",
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (file == null) {
				Toast.makeText(getApplicationContext(), "请先输入文件名",
						Toast.LENGTH_SHORT).show();
				return;
			}

			realFile = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + file + ".csv");
			realRAWlFile = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + file + "(RAW).csv");

			try {
				out = new FileOutputStream(realFile);
				rawOut = new FileOutputStream(realRAWlFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			mSensorManager.registerListener(MainActivity.this, mLinearSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(MainActivity.this,
					mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(MainActivity.this, mASensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			mSensorManager.registerListener(MainActivity.this, mGSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			hasStart = true;
			beginTime = System.currentTimeMillis();
			break;
		case R.id.stopButton:
			mSensorManager.unregisterListener(this);
			Toast.makeText(getApplicationContext(),
					"数据保存在" + realFile.getAbsolutePath(), Toast.LENGTH_LONG)
					.show();
			hasStart = false;
			try {
				out.flush();
				rawOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					out.close();
					rawOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}

	}

}
