package com.hekangping.testmap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.baidu.mapapi.cloud.CloudPoiInfo;
import com.baidu.mapapi.cloud.CloudSearchResult;
import com.baidu.mapapi.model.LatLng;

public class DataConvertUtil {

	private static String[] arrColumns = new String[] { "longitude",
			"latitude", "address", "warehouse_id", "warehouse_name",
			"warehouse_size", "warehouse_manager_name",
			"warehouse_manager_phone" };

	private static Set<String> lbsOtherColumns = new HashSet<String>(10);

	static {
		for (String columns : arrColumns) {
			lbsOtherColumns.add(columns);
		}
	}

	/**
	 * 将距离转换为字符串描述
	 * 
	 * @param distance
	 *            距离的数值
	 * @return 距离的字符串描述：如2.3335显示为1米；152333.232显示为152Km
	 */
	public static String getDistanceString(double distance) {
		String strDistance = "";
		if (distance < 1000) {
			strDistance += strDistance + Math.round(distance) + "m";
		} else {
			strDistance += strDistance + Math.round(distance / 1000) + "Km";
		}
		return strDistance;
	}

	/**
	 * 将位置信息转换为字符串：经度,纬度
	 * 
	 * @param objLatLng
	 *            位置信息
	 * @return 位置信息的字符串表示: 经度,纬度
	 */
	public static String getLatLngString(LatLng objLatLng) {
		return objLatLng.longitude + "," + objLatLng.latitude;
	}

	/**
	 * 将LBS结果重新组装
	 * 
	 * @param result
	 *            LBS检索结果
	 * @return 检索结果重新组装后的map
	 */
	public static Map<String, String> resolveLBSResult(CloudSearchResult result) {
		Map<String, String> objRtnMap = new HashMap<String, String>(result.size);
		LatLng ll = null;
		String strKey = "";
		Map<String, Object> objOtherColumns = null;
		for (CloudPoiInfo info : result.poiList) {
			StringBuffer sbValue = new StringBuffer(128);
			ll = new LatLng(info.latitude, info.longitude);
			strKey = info.longitude + "," + info.latitude;
			objOtherColumns = info.extras;
			String strColumnName = "";
			int iCount = 0;
			if (objOtherColumns != null) {
				for (Map.Entry<String, Object> entry : objOtherColumns
						.entrySet()) {
					strColumnName = "";
					if (lbsOtherColumns.contains(entry.getKey())) {
						if ("longitude".equals(entry.getKey())) {
							strColumnName = "经度";
						} else if ("latitude".equals(entry.getKey())) {
							strColumnName = "纬度";
						} else if ("address".equals(entry.getKey())) {
							strColumnName = "地址";
						} else if ("warehouse_id".equals(entry.getKey())) {
							strColumnName = "仓库编号";
						} else if ("warehouse_name".equals(entry.getKey())) {
							strColumnName = "仓库名称";
						} else if ("warehouse_size".equals(entry.getKey())) {
							strColumnName = "仓库面积(平米)";
						} else if ("warehouse_manager_phone".equals(entry
								.getKey())) {
							strColumnName = "仓库管理员";
						} else if ("warehouse_manager_name".equals(entry
								.getKey())) {
							strColumnName = "联系电话";
						}
						sbValue.append(strColumnName).append(":")
								.append(entry.getValue()).append("    ");
						if (iCount != 0 && iCount % 2 == 0) {
							sbValue.append("\n");
						}
						iCount++;
					}
				}
			}
			objRtnMap.put(strKey, sbValue.toString());
		}
		return objRtnMap;
	}
}
