package org.opennms.core.utils;

import java.util.HashMap;
import java.util.Map;

public class IpasolinkUtil {
	private static Map<Integer, Integer> ifIndexSlotmap = new HashMap<Integer, Integer>();

	static {
		ifIndexSlotmap.put(8454144, 0);
		ifIndexSlotmap.put(8519680, 0);
		ifIndexSlotmap.put(8585216, 0);
		ifIndexSlotmap.put(8650752, 0);
		ifIndexSlotmap.put(8716288, 0);
		ifIndexSlotmap.put(8781824, 0);
		ifIndexSlotmap.put(16842752, 1);
		ifIndexSlotmap.put(16908288, 1);
		ifIndexSlotmap.put(16973824, 1);
		ifIndexSlotmap.put(17039360, 1);
		ifIndexSlotmap.put(25231360, 2);
		ifIndexSlotmap.put(25296896, 2);
		ifIndexSlotmap.put(25362432, 2);
		ifIndexSlotmap.put(25427968, 2);
		ifIndexSlotmap.put(33619968, 3);
		ifIndexSlotmap.put(33685504, 3);
		ifIndexSlotmap.put(33751040, 3);
		ifIndexSlotmap.put(33816576, 3);
		ifIndexSlotmap.put(42008576, 4);
		ifIndexSlotmap.put(42074112, 4);
		ifIndexSlotmap.put(42139648, 4);
		ifIndexSlotmap.put(42205184, 4);
		ifIndexSlotmap.put(50397184, 5);
		ifIndexSlotmap.put(50462720, 5);
		ifIndexSlotmap.put(50528256, 5);
		ifIndexSlotmap.put(50593792, 5);
		ifIndexSlotmap.put(58785792, 6);
		ifIndexSlotmap.put(58851328, 6);
		ifIndexSlotmap.put(58916864, 6);
		ifIndexSlotmap.put(58982400, 6);
		ifIndexSlotmap.put(67174400, 7);
		ifIndexSlotmap.put(67239936, 7);
		ifIndexSlotmap.put(67305472, 7);
		ifIndexSlotmap.put(67371008, 7);
		ifIndexSlotmap.put(75563008, 8);
		ifIndexSlotmap.put(75628544, 8);
		ifIndexSlotmap.put(75694080, 8);
		ifIndexSlotmap.put(75759616, 8);
		ifIndexSlotmap.put(83951616, 9);
		ifIndexSlotmap.put(84017152, 9);
		ifIndexSlotmap.put(84082688, 9);
		ifIndexSlotmap.put(84148224, 9);
		ifIndexSlotmap.put(92340224, 10);
		ifIndexSlotmap.put(92405760, 10);
		ifIndexSlotmap.put(92471296, 10);
		ifIndexSlotmap.put(92536832, 10);
		ifIndexSlotmap.put(100728832, 11);
		ifIndexSlotmap.put(100794368, 11);
		ifIndexSlotmap.put(100859904, 11);
		ifIndexSlotmap.put(100925440, 11);
		ifIndexSlotmap.put(109117440, 12);
		ifIndexSlotmap.put(109182976, 12);
		ifIndexSlotmap.put(109248512, 12);
		ifIndexSlotmap.put(109314048, 12);
		ifIndexSlotmap.put(117506048, 13);
		ifIndexSlotmap.put(117571584, 13);
		ifIndexSlotmap.put(117637120, 13);
		ifIndexSlotmap.put(117702656, 13);
		ifIndexSlotmap.put(125894656, 14);
		ifIndexSlotmap.put(125960192, 14);
		ifIndexSlotmap.put(126025728, 14);
		ifIndexSlotmap.put(126091264, 14);
		ifIndexSlotmap.put(142671872, 16);
		ifIndexSlotmap.put(142737408, 16);
		ifIndexSlotmap.put(142802944, 16);
		ifIndexSlotmap.put(142868480, 16);
		ifIndexSlotmap.put(41, 1);
		ifIndexSlotmap.put(42, 2);
		ifIndexSlotmap.put(43, 3);
		ifIndexSlotmap.put(44, 4);
		ifIndexSlotmap.put(45, 5);
		ifIndexSlotmap.put(46, 6);
		ifIndexSlotmap.put(47, 7);
		ifIndexSlotmap.put(48, 8);
		ifIndexSlotmap.put(49, 9);
		ifIndexSlotmap.put(50, 10);
		ifIndexSlotmap.put(51, 11);
		ifIndexSlotmap.put(52, 12);
		ifIndexSlotmap.put(53, 13);
		ifIndexSlotmap.put(54, 14);

	};

	public static Integer getSlotNumberByIfIndex(Integer ifindex) {
		return ifIndexSlotmap.get(ifindex);
	}

}
