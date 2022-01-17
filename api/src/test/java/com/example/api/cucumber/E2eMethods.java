package com.example.api.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class E2eMethods {
  public static String contentType = "application/json";
  public static String BASE_URL = "http://localhost:8080";
  private static E2eMethods e2eMethods = null;
  private static Response response;
  private static String authorizationHeader = "";

  public static E2eMethods getInstance() {
    if (e2eMethods == null) {
      e2eMethods = new E2eMethods();
    }

    return e2eMethods;
  }

  public static RequestSpecification initializeRequest() {
    RestAssured.baseURI = BASE_URL;
    RequestSpecification request = RestAssured.given();
    request.header("Content-Type", contentType);
    request.header("Authorization", authorizationHeader);
    return request;
  }

  public Response post(String endPointURL, String body, String userId) {
    authorizationHeader = userId;
    RequestSpecification request = initializeRequest();
    response = request.body(body).post(endPointURL);
    return response;
  }

  public Response postUser(String endPointURL, String body) {
    RequestSpecification request = initializeRequest();
    response = request.body(body).post(endPointURL);
    return response;
  }

  public Response put(String endPointURL, String body, String userId) {
    authorizationHeader = userId;
    RequestSpecification request = initializeRequest();
    response = request.body(body).put(endPointURL);

    return response;
  }

  public Response deleteGroupUser(String endPointURL, String userId) {
    authorizationHeader = userId;
    RequestSpecification request = initializeRequest();
    response = request.put(endPointURL);

    return response;
  }

  public Response get(String getFormat) {
    RequestSpecification request = initializeRequest();
    response = request.get(getFormat);
    return response;
  }

  public Response delete(String endPointURL, String userId) {
    authorizationHeader = userId;
    RequestSpecification request = initializeRequest();
    response = request.delete(endPointURL);

    return response;
  }

  public Response getWithUserId(String endPointURL, String userId) {
    authorizationHeader = userId;
    RequestSpecification request = initializeRequest();
    response = request.get(endPointURL);
    return response;
  }

  public String getJsonString(Object object) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while parsing response body json object", e);
    }
  }

  public <T> T convertJsonStrToDataObject(String itemsJson, Class<T> valueType) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(itemsJson, valueType);
    } catch (Exception e) {
      throw new RuntimeException("Error while convert response body data object", e);
    }
  }

  public <T> List<T> convertJsonStrToDataObjectList(String itemsJson, Class<T[]> valueType) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return new ArrayList<T>(Arrays.asList(mapper.readValue(itemsJson, valueType)));
    } catch (Exception e) {
      throw new RuntimeException("Error while convert response body data object list", e);
    }
  }
}
