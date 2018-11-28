package com.llhy.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {
	/**
	 * <p>将list集合均分成若干份。</p>
	 * @param threadNum 被分解的份数
	 * @param list 被分解集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<List> limitListByThreadNum(int threadNum,List list){
		List<List> portionsList = new ArrayList<List>();
		if(null != list && list.size() > 0){
			switch (threadNum) {
			case 1:
				portionsList.add(list);
				break;
			default:
				int pageSize = list.size()/threadNum;
				int begin = 0;
				int end = 0;
				for(int i = 0;i < threadNum; i++){
					begin = end;
					if(begin+((threadNum-i)*pageSize) < list.size()){
						end = begin + pageSize + 1;
					}
					else{
						end = begin + pageSize;
					}
					List ls = list.subList(begin,end );
					portionsList.add(ls);
				}
				break;
			}
		}
		else{
			for (int i = 0;i < threadNum; i++){
				portionsList.add(null);
			}
		}
		return portionsList;
	}
	
}
