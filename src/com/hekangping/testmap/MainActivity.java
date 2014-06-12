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
	private LatLng objNewPosition = null;

	// init Toast
	private ToastShow objToast = new ToastShow(MainActivity.this);

	// ��ʼ��ȫ�� bitmap ��Ϣ������ʱ��ʱ recycle
	BitmapDescriptor bdA = null;

	private Marker mMarkerA;
	private InfoWindow mInfoWindow;
	// ��λ��
	private List<LatLng> points = new ArrayList<LatLng>();

	// POI���
	private PoiSearch mPoiSearch = null;
	private SuggestionSearch mSuggestionSearch = null;
	/**
	 * �����ؼ������봰��
	 */
	private AutoCompleteTextView keyWorldsView = null;
	private ArrayAdapter<String> sugAdapter = null;
	private int load_Index = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ʹ��SDK�����֮ǰ��ʼ��context��Ϣ������ApplicationContext
		// ע��÷���Ҫ��setContentView����֮ǰʵ��
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.search_layout);
		// ��ʼ������ģ�飬ע�������¼�����
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

		CloudManager.getInstance().init(MainActivity.this);

		// ��ͼ��ʼ��
		mMapView = (MapView) findViewById(R.id.bmapsView);
		mBaiduMap = mMapView.getMap();
		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);

		// ��ȡmapview�е����ſؼ�
		ZoomControls zoomControls = (ZoomControls) mMapView.getChildAt(2);
		mMapView.removeViewAt(2);
		// �������ſؼ���λ��
		// zoomControls.setPadding(0, 0, 0, 100);
		// ��ȡmapview�еİٶȵ�ͼͼ��
		ImageView iv = (ImageView) mMapView.getChildAt(1);
		mMapView.removeViewAt(1);
		// �����ٶȵ�ͼͼ���λ��
		// iv.setPadding(0, 0, 0, 100);

		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(10000); // ÿ��10s��λһ��
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
					button.setText("λ��\n����:" + objMarkerPosition.longitude
							+ "  γ��:" + objMarkerPosition.latitude);
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
		 * ������ؼ��ֱ仯ʱ����̬���½����б�
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
				 * ʹ�ý������������ȡ�����б������onSuggestionResult()�и���
				 */
				mSuggestionSearch
						.requestSuggestion((new SuggestionSearchOption())
								.keyword(cs.toString()).city("����"));
			}
		});
	}

	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.d(TAG, " onReceiveLocation()");
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null)
				return;
			objToast.toastShow("���ڶ�λ");
			double dx = 0 * Math.random();
			double dy = 0 * Math.random();
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100)
					// TODO: + dx ��+ dy ��Ϊ��ģ��λ�øı�
					.latitude(location.getLatitude() + dx)
					.longitude(location.getLongitude() + dy).build();

			objNewPosition = new LatLng(location.getLatitude() + dx,
					location.getLongitude() + dy);

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
					// ͼ���Ƕ�λ��
					OverlayOptions ooA = new MarkerOptions()
							.position(objNewPosition).icon(bdA).zIndex(9);
					mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
					// ����·��
					points.add(objNewPosition);
					OverlayOptions ooPolyline = new PolylineOptions().width(2)
							.color(0xAAFF0000).points(points);
					mBaiduMap.addOverlay(ooPolyline); //
					// ��������Option���������ڵ�ͼ���������,��ʾ����
					OverlayOptions textOption = new TextOptions()
							.fontSize(24)
							.fontColor(0xFFFF00FF)
							.text("   "
									+ DataConvertUtil
											.getDistanceString(distance))
							.rotate(-30).position(objNewPosition); // �ڵ�ͼ����Ӹ����ֶ�����ʾ
					mBaiduMap.addOverlay(textOption);

				}
			} else {
				mBaiduMap.setMyLocationData(locData);
				lastPosition = location;
				OverlayOptions ooA = new MarkerOptions()
						.position(objNewPosition).icon(bdA).zIndex(9);
				mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
				// �����㵽·����
				points.add(objNewPosition);
			}
			if (isFirstLoc) {
				isFirstLoc = false;
				// ��һ�ζ�λ����λ��Ŵ��ͼ
				MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
				mBaiduMap.setMapStatus(msu);
				// ��һ�ζ�λ������λ���ƶ�����Ļ����λ��
				MapStatusUpdate u = MapStatusUpdateFactory
						.newLatLng(objNewPosition);
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
				Log.d(TAG, "���綨λ�ɹ�\n" + "��ǰλ��:" + location.getAddrStr());
				// objToast.toastShow("���綨λ�ɹ�\n" + "��ǰλ��:" +
				// location.getAddrStr());
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

	/**
	 * Ӱ��������ť����¼�
	 * 
	 * @param v
	 */
	public void searchButtonProcess(View v) {

		EditText editSearchKey = (EditText) findViewById(R.id.searchkeytext);
		String strKeyWord = editSearchKey.getText().toString();

		Log.d(TAG, "searchButtonProcess() " + strKeyWord);

		/**
		 * Poi���� mPoiSearch.searchInCity((new PoiCitySearchOption()).city("����")
		 * .keyword(strKeyWord) .pageNum(load_Index));
		 */

		// TODO : LBS
		// ����Ѿ���λ,ʹ�ø�������
		if (objNewPosition != null) {
			Log.d(TAG, " LBS�ƶ��������� " + strKeyWord);
			// LBS�ƶ���������
			NearbySearchInfo info = new NearbySearchInfo();
			info.ak = "QCx9Ygu535vMOrqDXwBw1r6r";
			info.geoTableId = 68053;
			info.tags = strKeyWord;
			info.radius = 5000; // �����뾶��5km
			info.location = DataConvertUtil.getLatLngString(objNewPosition);
			CloudManager.getInstance().nearbySearch(info);
		} else {
			// ��û�ж�λʱ��ʹ����������
			Log.d(TAG, " LBS�������� " + strKeyWord);
			LocalSearchInfo info = new LocalSearchInfo();
			info.ak = "QCx9Ygu535vMOrqDXwBw1r6r";
			info.geoTableId = 68053;
			info.tags = "";
			info.q = "������";
			info.region = "������";
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
		// �˳�ʱ���ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mPoiSearch.destroy();
		mSuggestionSearch.destroy();
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
		// ���� bitmap ��Դ
		bdA.recycle();
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
		Log.d(TAG, "POI: onGetPoiDetailResult()");
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "�ɹ����鿴����ҳ��", Toast.LENGTH_SHORT)
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
			// ������ؼ����ڱ���û���ҵ����������������ҵ�ʱ�����ذ����ùؼ�����Ϣ�ĳ����б�
			String strInfo = "��";
			for (CityInfo cityInfo : result.getSuggestCityList()) {
				strInfo += cityInfo.city;
				strInfo += ",";
			}
			strInfo += "�ҵ����";
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
					"POI���  onPoiClick()");
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
