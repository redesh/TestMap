package com.hekangping.testmap;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.cloud.CloudListener;
import com.baidu.mapapi.cloud.CloudManager;
import com.baidu.mapapi.cloud.CloudPoiInfo;
import com.baidu.mapapi.cloud.CloudSearchResult;
import com.baidu.mapapi.cloud.DetailSearchResult;
import com.baidu.mapapi.cloud.LocalSearchInfo;
import com.baidu.mapapi.cloud.NearbySearchInfo;
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
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.LatLngBounds.Builder;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;

public class MainActivity extends ActionBarActivity implements
		OnGetPoiSearchResultListener, OnGetSuggestionResultListener,
		CloudListener {
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
	private LatLng objNewPosition = null;

	// init Toast
	private ToastShow objToast = new ToastShow(MainActivity.this);

	// 初始化全局 bitmap 信息，不用时及时 recycle
	BitmapDescriptor bdA = null;

	private Marker mMarkerA;
	private InfoWindow mInfoWindow;
	// 定位点
	private List<LatLng> points = new ArrayList<LatLng>();

	// POI相关
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	/**
	 * 搜索关键字输入窗口
	 */
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_Index = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.search_layout);
		// 初始化搜索模块，注册搜索事件监听
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this);
		keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkeytext);
		sugAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		keyWorldsView.setAdapter(sugAdapter);

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

		CloudManager.getInstance().init(MainActivity.this);

		// 地图初始化
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);

		// 获取mapview中的缩放控件
		ZoomControls zoomControls = (ZoomControls) mMapView.getChildAt(2);
		mMapView.removeViewAt(2);
		// 调整缩放控件的位置
		// zoomControls.setPadding(0, 0, 0, 100);
		// 获取mapview中的百度地图图标
		ImageView iv = (ImageView) mMapView.getChildAt(1);
		mMapView.removeViewAt(1);
		// 调整百度地图图标的位置
		// iv.setPadding(0, 0, 0, 100);

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
					button.setText("位置\n经度:" + objMarkerPosition.longitude
							+ "  纬度:" + objMarkerPosition.latitude);
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

		/**
		 * 当输入关键字变化时，动态更新建议列表
		 */
		keyWorldsView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				if (cs.length() <= 0) {
					return;
				}
				/**
				 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
				 */
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city("深圳"));
			}
		});
	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.d(TAG, " onReceiveLocation()");
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			objToast.toastShow("正在定位");
			double dx = 0 * Math.random();
			double dy = 0 * Math.random();
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100)
					// TODO: + dx 和+ dy 是为了模拟位置改变
					.latitude(location.getLatitude() + dx)
					.longitude(location.getLongitude() + dy).build();

			objNewPosition = new LatLng(location.getLatitude() + dx,
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
					mBaiduMap.setMyLocationData(locData);
					lastPosition = location;
					// 图标标记定位点
					OverlayOptions ooA = new MarkerOptions()
							.position(objNewPosition).icon(bdA).zIndex(9);
					mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
					// 绘制路线
					points.add(objNewPosition);
					OverlayOptions ooPolyline = new PolylineOptions().width(2)
							.color(0xAAFF0000).points(points);
					mBaiduMap.addOverlay(ooPolyline); //
					// 构建文字Option对象，用于在地图上添加文字,显示距离
					OverlayOptions textOption = new TextOptions()
							.fontSize(24)
							.fontColor(0xFFFF00FF)
							.text("   "
									+ DataConvertUtil
											.getDistanceString(distance))
							.rotate(-30).position(objNewPosition); // 在地图上添加该文字对象并显示
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
				Log.d(TAG, "网络定位成功\n" + "当前位置:" + location.getAddrStr());
				// objToast.toastShow("网络定位成功\n" + "当前位置:" +
				// location.getAddrStr());
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

	/**
	 * 影响搜索按钮点击事件
	 * 
	 * @param v
	 */
	public void searchButtonProcess(View v) {

		EditText editSearchKey = (EditText) findViewById(R.id.searchkeytext);
		String strKeyWord = editSearchKey.getText().toString();

		Log.d(TAG, "searchButtonProcess() " + strKeyWord);

		/**
		 * Poi搜索 mPoiSearch.searchInCity((new PoiCitySearchOption()).city("深圳")
		 * .keyword(strKeyWord) .pageNum(load_Index));
		 */

		// TODO : LBS
		// 如果已经定位,使用附近搜索
		if (objNewPosition != null) {
			Log.d(TAG, " LBS云端搜索附近 " + strKeyWord);
			// LBS云端搜索附近
			NearbySearchInfo info = new NearbySearchInfo();
			info.ak = "QCx9Ygu535vMOrqDXwBw1r6r";
			info.geoTableId = 68053;
			info.tags = strKeyWord;
			info.radius = 5000; // 搜索半径是5km
			info.location = DataConvertUtil.getLatLngString(objNewPosition);
			CloudManager.getInstance().nearbySearch(info);
		} else {
			// 还没有定位时就使用市内搜索
			Log.d(TAG, " LBS市内搜索 " + strKeyWord);
			LocalSearchInfo info = new LocalSearchInfo();
			info.ak = "QCx9Ygu535vMOrqDXwBw1r6r";
			info.geoTableId = 68053;
			info.tags = "";
			info.q = "福田区";
			info.region = "深圳市";
			CloudManager.getInstance().localSearch(info);
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
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
		// 回收 bitmap 资源
		bdA.recycle();
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
		Log.d(TAG, "POI: onGetPoiDetailResult()");
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "成功，查看详情页面", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		Log.d(TAG, "POI: onGetPoiResult()");
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			mBaiduMap.clear();
			PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
			// 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
			String strInfo = "在";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "找到结果";
			Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
		Log.d(TAG, "POI: onGetSuggestionResult()");
		if (res == null || res.getAllSuggestions() == null) {
			return;
		}
		sugAdapter.clear();
		for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
			if (info.key != null)
				sugAdapter.add(info.key);
		}
		sugAdapter.notifyDataSetChanged();
	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			Log.d("com.hekangping.testmap.MainActivity.MyPoiOverlay",
					"POI点击  onPoiClick()");
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			if (poi.hasCaterDetails) {
				mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
						.poiUid(poi.uid));
			}
			return true;
		}
	}

	@Override
	public void onGetSearchResult(CloudSearchResult result, int eror) {
		Log.d(TAG, "LBS: onGetSearchResult() status=" + result.status);
		if (result != null && result.poiList != null
				&& result.poiList.size() > 0) {
			Log.d(TAG,
					"onGetSearchResult, result length: "
							+ result.poiList.size());
			mBaiduMap.clear();
			BitmapDescriptor bd = BitmapDescriptorFactory
					.fromResource(R.drawable.icon_gcoding);
			LatLng ll;
			LatLngBounds.Builder builder = new Builder();
			for (CloudPoiInfo info : result.poiList) {
				ll = new LatLng(info.latitude, info.longitude);
				OverlayOptions oo = new MarkerOptions().icon(bd).position(ll);
				mBaiduMap.addOverlay(oo);
				builder.include(ll);
			}
			LatLngBounds bounds = builder.build();
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(bounds);
			mBaiduMap.animateMapStatus(u);
		}
	}

	@Override
	public void onGetDetailSearchResult(DetailSearchResult result, int error) {
		Log.d(TAG, "LBS: onGetDetailSearchResult() status=" + result.status);
		if (result != null) {
			Log.d(TAG, "onGetSearchResult, result length: " + result.size);
			if (result.poiInfo != null) {
				Toast.makeText(MainActivity.this, result.poiInfo.title,
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(MainActivity.this, "status:" + result.status,
						Toast.LENGTH_SHORT).show();
			}
		}

	}

}
