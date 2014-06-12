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

	/**
	 * ��LBS���������װ
	 * 
	 * @param result
	 *            LBS�������
	 * @return �������������װ���map
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
							strColumnName = "����";
						} else if ("latitude".equals(entry.getKey())) {
							strColumnName = "γ��";
						} else if ("address".equals(entry.getKey())) {
							strColumnName = "��ַ";
						} else if ("warehouse_id".equals(entry.getKey())) {
							strColumnName = "�ֿ���";
						} else if ("warehouse_name".equals(entry.getKey())) {
							strColumnName = "�ֿ�����";
						} else if ("warehouse_size".equals(entry.getKey())) {
							strColumnName = "�ֿ����(ƽ��)";
						} else if ("warehouse_manager_phone".equals(entry
								.getKey())) {
							strColumnName = "�ֿ����Ա";
						} else if ("warehouse_manager_name".equals(entry
								.getKey())) {
							strColumnName = "��ϵ�绰";
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
