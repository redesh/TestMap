package com.hekangping.testmap;

import com.baidu.mapapi.model.LatLng;

public class DataConvertUtil {

	/**
	 * ������ת��Ϊ�ַ�������
	 * 
	 * @param distance
	 *            �������ֵ
	 * @return ������ַ�����������2.3335��ʾΪ1�ף�152333.232��ʾΪ152Km
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
	 * ��λ����Ϣת��Ϊ�ַ���������,γ��
	 * 
	 * @param objLatLng
	 *            λ����Ϣ
	 * @return λ����Ϣ���ַ�����ʾ: ����,γ��
	 */
	public static String getLatLngString(LatLng objLatLng) {
		return objLatLng.longitude + "," + objLatLng.latitude;
	}

}
