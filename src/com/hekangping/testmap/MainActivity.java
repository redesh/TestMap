package com.hekangping.testmap;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfigeration;
import com.baidu.mapapi.map.MyLocationConfigeration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class MainActivity extends ActionBarActivity {
	public static final String TAG = "MainActivity";
	// ��λ���
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	// �ϴζ�λ���
	private BDLocation lastPosition = null;

	BitmapDescriptor mCurrentMarker;

	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI���
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton;
	boolean isFirstLoc = true;// �Ƿ��״ζ�λ

	// init Toast
	private ToastShow objToast = new ToastShow(MainActivity.this);

	// ��ʼ��ȫ�� bitmap ��Ϣ������ʱ��ʱ recycle
	BitmapDescriptor bdA = null;

	private Marker mMarkerA;
	private InfoWindow mInfoWindow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
		requestLocButton = (Button) findViewById(R.id.button1);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("��ͨ");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("����");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("��ͨ");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("����");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		// ��ͼ��ʼ��
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mBaiduMap = mMapView.getMap();
		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);

		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(30000); // ÿ��30s��λһ��
		option.setAddrType("all"); // �粻�����,location.getAddrStr()�õ��ķ�������ַ����null
		mLocClient.setLocOption(option);
		mLocClient.start();

		// ��ͼ�ϵ���¼�
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
			public void onMapClick(LatLng point) {
				// �ڴ˴������¼�
				objToast.toastShow("λ��\n����:" + point.latitude + "  γ��:"
						+ point.longitude);
			}

			public boolean onMapPoiClick(MapPoi poi) {
				// �ڴ˴����ͼ��ע����¼�
				return false;
			}
		});

		// ��ͼ��ע����¼�
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Button button = new Button(getApplicationContext());
				button.setBackgroundResource(R.drawable.popup);
				final LatLng objMarkerPosition = marker.getPosition();
				Point p = mBaiduMap.getProjection().toScreenLocation(
						objMarkerPosition);
				p.y -= 47;
				LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
				OnInfoWindowClickListener listener = null;
				if (marker == mMarkerA) {
					button.setTextColor(Color.BLACK);
					button.setText("λ��\n����:" + objMarkerPosition.latitude
							+ "  γ��:" + objMarkerPosition.longitude);
					listener = new OnInfoWindowClickListener() {
						public void onInfoWindowClick() {
							mBaiduMap.hideInfoWindow();
						}
					};
				}
				mInfoWindow = new InfoWindow(button, llInfo, listener);
				mBaiduMap.showInfoWindow(mInfoWindow);
				return true;
			}
		});
	}

	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null)
				return;
			objToast.toastShow("���ڶ�λ");
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();

			LatLng objNewPosition = new LatLng(location.getLatitude(),
					location.getLongitude());

			if (lastPosition != null) {
				LatLng objLastPosition = new LatLng(lastPosition.getLatitude(),
						lastPosition.getLongitude());
				double distance = DistanceUtil.getDistance(objLastPosition,
						objNewPosition);
				Log.d(TAG, "�ϴ�λ��:" + objLastPosition + "\n��λ�ã�"
						+ objNewPosition + "\nλ�ƣ�" + distance);
				// location.isCellChangeFlag() ???
				// �ж��µĶ�λ�����֮ǰ��λ����ľ����,С��50�Ͳ����¶�λ���.
				if (distance > 50) {
					mBaiduMap.setMyLocationData(locData);
					lastPosition = location;
					OverlayOptions ooA = new MarkerOptions()
							.position(objNewPosition).icon(bdA).zIndex(9);
					mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
				}
			} else {
				mBaiduMap.setMyLocationData(locData);
				lastPosition = location;
				OverlayOptions ooA = new MarkerOptions()
						.position(objNewPosition).icon(bdA).zIndex(9);
				mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
			}
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
				mBaiduMap.setMapStatus(msu);
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}

			// ��λ���
			int iErrorCode = location.getLocType();

			switch (iErrorCode) {
			case BDLocation.TypeGpsLocation:
				objToast.toastShow("GPS��λ�ɹ�");
				break;
			case BDLocation.TypeCriteriaException:
				objToast.toastShow("ɨ�����϶�λ����ʧ�ܡ���λ�����Ч��");
				break;
			case BDLocation.TypeNetWorkException:
				objToast.toastShow("�����쳣��û�гɹ���������������󡣶�λʧ��");
				break;
			case BDLocation.TypeOffLineLocation:
				objToast.toastShow("���߶�λ�ɹ�");
				break;
			case BDLocation.TypeOffLineLocationFail:
				objToast.toastShow("���߶�λʧ��");
				break;
			case BDLocation.TypeOffLineLocationNetworkFail:
				objToast.toastShow("��������ʧ�ܣ�ʹ�����߶�λ");
				break;
			case BDLocation.TypeNetWorkLocation:
				objToast.toastShow("���綨λ�ɹ�\n" + "��ǰλ��:" + location.getAddrStr());
				break;
			case BDLocation.TypeCacheLocation:
				objToast.toastShow("����Ķ�λ���");
				break;
			case BDLocation.TypeServerError:
				objToast.toastShow("����˶�λʧ��");
				break;
			}

			// ��λ����ϸ��Ϣ
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
			}

			Log.d(TAG, sb.toString());

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// �˳�ʱ���ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
		// ���� bitmap ��Դ
		bdA.recycle();
	}

}
