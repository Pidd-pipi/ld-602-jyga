export const ERROR_MESSAGES = {
  AUTH_REQUIRED: "请先登录后再继续操作",
  RBAC_DENIED: "当前角色没有执行该动作的权限",
  VALIDATION_FAILED: "表单字段缺失或格式错误",
  RATE_LIMITED: "请求过于频繁，请稍后再试",
  SHELTER_NOT_FOUND: "避难点不存在或已被移除",
  SHELTER_NOT_OPEN: "避难点关闭或停用期间不能接收人员",
  CAPACITY_EXCEEDED: "接收人数超出核定容量，剩余名额不足",
  INSUFFICIENT_POPULATION: "转出人数不能高于该点现有人数",
  SHELTER_NOT_EMPTY: "尚有人员未迁空，不能关闭该避难点",
  INVALID_AMOUNT: "人数必须为正整数",
  SAME_SHELTER: "转出与接收不能是同一个避难点"
};
