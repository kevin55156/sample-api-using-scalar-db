package com.example.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalar.db.api.Result;
import com.scalar.db.io.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScalarUtil {

  /**
   * Resultから指定したカラム名にマッピングされているTextValueの値を取得し、String型で返却する
   *
   * @param result {@link Result}
   * @param columnName カラム名
   * @return columnNameで指定したTextValueの値をString型に変換した値
   */
  public static String getTextValue(Result result, String columnName) {
    Optional<Value<?>> value = result.getValue(columnName);
    return value
        .filter(o -> o.getAsString().isPresent())
        .map(o -> o.getAsString().get())
        .orElse("");
  }

  /**
   * String型のJSONの配列をデータオブジェクトに変換
   *
   * @param itemsJson String型のJSONの配列
   * @param valueType 変換したいデータオブジェクトクラス
   * @return valueTypeで指定した型に変換して返却
   */
  public static <T> T convertJsonStrToDataObject(String itemsJson, Class<T> valueType) {
    if (itemsJson == null || itemsJson.isEmpty()) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(itemsJson, valueType);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * データオブジェクトをString型のJSONの配列に変換
   *
   * @param items データオブジェクト
   * @return String型のJSONの配列
   */
  public static <T> String convertDataObjectToJsonStr(T items) {
    if (items == null) {
      return "";
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(items);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * String型のJSONの配列をデータオブジェクトリストに変換
   *
   * @param itemsJson String型のJSONの配列に変換
   * @param valueType 変換したいデータオブジェクトクラス
   * @return valueTypeで指定した型に変換して返却
   */
  public static <T> List<T> convertJsonStrToDataObjectList(String itemsJson, Class<T[]> valueType) {
    if (itemsJson == null || itemsJson.isEmpty()) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      return new ArrayList<T>(Arrays.asList(mapper.readValue(itemsJson, valueType)));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * データオブジェクトリストを別のデータオブジェクトリストに変換
   *
   * @param objectList データオブジェクトリスト
   * @param valueType 変換したいデータオブジェクトクラス
   * @return valueTypeで指定した型に変換して返却
   */
  public static <T, U> List<T> convertDataObjectListToAnotherDataObjectList(
      List<U> objectList, Class<T[]> valueType) {
    if (objectList == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      String items = mapper.writeValueAsString(objectList);
      return new ArrayList<T>(Arrays.asList(mapper.readValue(items, valueType)));
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * データオブジェクトを別のデータオブジェクトに変換
   *
   * @param object データオブジェクト
   * @param valueType 変換したいデータオブジェクトクラス
   * @return valueTypeで指定した型に変換して返却
   */
  public static <T, U> T convertDataObjectToAnotherDataObject(U object, Class<T> valueType) {
    if (object == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      String items = mapper.writeValueAsString(object);
      return mapper.readValue(items, valueType);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage(), e);
    }
    return null;
  }
}
