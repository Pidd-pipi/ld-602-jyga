package com.generated.rescueStock.constants;

public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "missing token";
  public static final String RBAC_DENIED = "role denied";
  public static final String INTERNAL_ERROR = "服务器内部错误";
  public static final String SHELTER_NOT_FOUND = "避难点不存在";
  public static final String SHELTER_NOT_OPEN = "避难点已关闭或停用，不能接收";
  public static final String CAPACITY_EXCEEDED = "接收后超出核定容量";
  public static final String INSUFFICIENT_POPULATION = "转出人数超过现有人数";
  public static final String SHELTER_NOT_EMPTY = "避难点尚未迁空，不能关闭";
  public static final String INVALID_COUNT = "人数必须为正整数";
  public static final String INVALID_OPERATOR = "经办人不能为空";
  public static final String INVALID_STATUS = "目标状态无效";
  public static final String SAME_SHELTER = "不能在同一避难点之间转移";

  private ErrorMessages() {}
}
