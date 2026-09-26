export const ERROR_MESSAGES = {
  AUTH_REQUIRED: "请先登录后再继续操作",
  RBAC_DENIED: "当前角色没有执行该动作的权限",
  VALIDATION_FAILED: "表单字段缺失或格式错误",
  RATE_LIMITED: "请求过于频繁，请稍后再试",
  REQUEST_FAILED: "请求失败，请稍后再试",
  SHELTER_NOT_FOUND: "避难点不存在",
  SHELTER_NOT_OPEN: "避难点已关闭或停用，不能接收",
  CAPACITY_EXCEEDED: "接收后超出核定容量",
  INSUFFICIENT_POPULATION: "转出人数超过现有人数",
  SHELTER_NOT_EMPTY: "避难点尚未迁空，不能关闭",
  INVALID_COUNT: "人数必须为正整数",
  INVALID_OPERATOR: "经办人不能为空",
  INVALID_STATUS: "目标状态无效",
  SAME_SHELTER: "不能在同一避难点之间转移"
};
