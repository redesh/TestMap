package com.hekangping.testmap;

import com.baidu.mapapi.model.LatLng;

public class DataConvertUtil {

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

}
