package com.hekangping.testmap;

import java.util.ArrayList;
import java.util.List;

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
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class MainActivity extends ActionBarActivity {
	public static final String TAG = "MainActivity";
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	// 上次定位结果
	private BDLocation lastPosition = null;

	BitmapDescriptor mCurrentMarker;

	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI相关
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位

	// init Toast
	private ToastShow objToast = new ToastShow(MainActivity.this);

	// 初始化全局 bitmap 信息，不用时及时 recycle
	BitmapDescriptor bdA = null;

	private Marker mMarkerA;
	private InfoWindow mInfoWindow;
	// 定位点
	private List<LatLng> points = new ArrayList<LatLng>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
		requestLocButton = (Button) findViewById(R.id.button1);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("普通");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfigeration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(10000); // 每隔10s定位一次
		option.setAddrType("all"); // 如不加这个,location.getAddrStr()得到的反向地理地址就是null
		mLocClient.setLocOption(option);
		mLocClient.start();

		// 地图上点击事件
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
			public void onMapClick(LatLng point) {
				// 在此处理点击事件
				objToast.toastShow("位置\n经度:" + point.latitude + "  纬度:"
						+ point.longitude);
			}

			public boolean onMapPoiClick(MapPoi poi) {
				// 在此处理底图标注点击事件
				return false;
			}
		});

		// 地图标注点击事件
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
					button.setText("位置\n经度:" + objMarkerPosition.latitude
							+ "  纬度:" + objMarkerPosition.longitude);
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
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			objToast.toastShow("正在定位");
			double dx = Math.random();
			double dy = Math.random();
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100)
					// TODO: + dx ; + dy是调试代码，随机改变定位位置
					.latitude(location.getLatitude() + dx)
					.longitude(location.getLongitude() + dy).build();
			// TODO: + dx ; + dy是调试代码，随机改变定位位置
			LatLng objNewPosition = new LatLng(location.getLatitude() + dx,
					location.getLongitude() + dy);

			if (lastPosition != null) {
				LatLng objLastPosition = new LatLng(lastPosition.getLatitude(),
						lastPosition.getLongitude());
				double distance = DistanceUtil.getDistance(objLastPosition,
						objNewPosition);
				Log.d(TAG, "上次位置:" + objLastPosition + "\n新位置："
						+ objNewPosition + "\n位移：" + distance);
				// location.isCellChangeFlag() ???
				// 判断新的定位结果和之前定位结果的距离差,小于50就不更新定位结果.
				if (distance > 50) {
					// 定位,显示定位图标
					mBaiduMap.setMyLocationData(locData);
					lastPosition = location;
					OverlayOptions ooA = new MarkerOptions()
							.position(objNewPosition).icon(bdA).zIndex(9);
					mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
					// 绘制路线
					points.add(objNewPosition);
					OverlayOptions ooPolyline = new PolylineOptions().width(2)
							.color(0xAAFF0000).points(points);
					mBaiduMap.addOverlay(ooPolyline);
					// 构建文字Option对象，用于在地图上添加文字,显示距离
					OverlayOptions textOption = new TextOptions()
							.bgColor(0xAAFFFF00).fontSize(24)
							.fontColor(0xFFFF00FF)
							.text("   " + Math.round(distance) + "米")
							.rotate(-30).position(objNewPosition);
					// 在地图上添加该文字对象并显示
					mBaiduMap.addOverlay(textOption);
				}
			} else {
				mBaiduMap.setMyLocationData(locData);
				lastPosition = location;
				OverlayOptions ooA = new MarkerOptions()
						.position(objNewPosition).icon(bdA).zIndex(9);
				mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
				// 添加起点到路线中
				points.add(objNewPosition);
			}
			if (isFirstLoc) {
				isFirstLoc = false;
				// 第一次定位，定位后放大地图
				MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
				mBaiduMap.setMapStatus(msu);
				// 第一次定位，将定位点移动到屏幕中心位置
				MapStatusUpdate u = MapStatusUpdateFactory
						.newLatLng(objNewPosition);
				mBaiduMap.animateMapStatus(u);
			}

			// 定位结果
			int iErrorCode = location.getLocType();

			switch (iErrorCode) {
			case BDLocation.TypeGpsLocation:
				objToast.toastShow("GPS定位成功");
				break;
			case BDLocation.TypeCriteriaException:
				objToast.toastShow("扫描整合定位依据失败。定位结果无效。");
				break;
			case BDLocation.TypeNetWorkException:
				objToast.toastShow("网络异常，没有成功向服务器发起请求。定位失败");
				break;
			case BDLocation.TypeOffLineLocation:
				objToast.toastShow("离线定位成功");
				break;
			case BDLocation.TypeOffLineLocationFail:
				objToast.toastShow("离线定位失败");
				break;
			case BDLocation.TypeOffLineLocationNetworkFail:
				objToast.toastShow("网络连接失败，使用离线定位");
				break;
			case BDLocation.TypeNetWorkLocation:
				objToast.toastShow("网络定位成功\n" + "当前位置:" + location.getAddrStr());
				break;
			case BDLocation.TypeCacheLocation:
				objToast.toastShow("缓存的定位结果");
				break;
			case BDLocation.TypeServerError:
				objToast.toastShow("服务端定位失败");
				break;
			}

			// 定位的详细信息
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
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
		// 回收 bitmap 资源
		bdA.recycle();
	}

}
