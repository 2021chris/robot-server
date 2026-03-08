package com.chris.robot_server.util;

import com.chris.robot_server.vo.KeyValueVO;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class KeyValueUtil {

    public static List<KeyValueVO> conver(List<?> list) {
        List<KeyValueVO> retList = new ArrayList<>();
        Object result = "";
        try {
            for (Object obj : list
                    ) {
                KeyValueVO kv = new KeyValueVO();
                Method[] methods = obj.getClass().getMethods();
                for (Method method : methods) {
                    if (method.getName().equalsIgnoreCase("getId")) {
                        result = method.invoke(obj);
                        kv.setId((Integer) result);
                    } else if (method.getName().equalsIgnoreCase("getName")) {
                        result = method.invoke(obj);
                        kv.setName(result.toString());
                    }
                }
                retList.add(kv);
            }
        } catch (Exception e){
            return null;
        }

        return retList;
    }

    public static List<KeyValueVO> MapToKeyValueList(Map<?, String> map) {
        List<KeyValueVO> retList = new ArrayList<>();
        for (Map.Entry<?, String> entry : map.entrySet()) {
            KeyValueVO kv = new KeyValueVO();
            kv.setId(entry.getKey());
            kv.setName(entry.getValue());
            retList.add(kv);
        }
        return retList;
    }
}
